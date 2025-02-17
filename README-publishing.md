# Publishing the library

As of February 1, 2024, Sonatype requires new accounts be created in [Maven Central](https://central.sonatype.com).
This means artifacts for new accounts must be published via the Central Portal, and they cannot be published via OSSRH.
Furthermore, ["there is no official Gradle plugin for publishing to Maven Central via the Central Publishing Portal"](https://central.sonatype.org/publish/publish-portal-gradle/)
yet. You may publish with a Maven plugin, the Publisher API, manually, or via third party Gradle plugins.

## Install GPG

Create public and private keys for signing.

	brew install gnupg
	gpg --full-gen-key
	gpg --keyserver keyserver.ubuntu.com --send-keys MY_PUBLIC_KEY_ID

## Increment the version

Update the `version` in `build.gradle`.

## Manual Upload

### Generate the artifacts

We could make a separate publication, but we're reusing the pom artifact that we're generating for GPR (GitHub Packages Repository).
Then copy the artifacts to a separate directory.
Finally, sign the artifacts with ASCII signature files (.asc) and generate checksums (.sha1 and .md5):

	./gradlew clean build generatePomFileForGprPublication
	GROUPDIR=io/github/baylorpaul
	LIBNAME=micronaut-json-api
	VERSION=$(ls $LIBNAME/build/libs/*-sources.jar | xargs -n 1 basename | sed 's/.*-\([\.0-9A-Za-z]*\)-sources.*/\1/')
	mkdir -p $LIBNAME/build/publications/maven-central/artifacts
	find $LIBNAME/build/libs -type f -name "$LIBNAME-$VERSION.jar" -or -name "$LIBNAME-$VERSION-javadoc.jar" -or -name "$LIBNAME-$VERSION-sources.jar" | xargs -I {} cp {} $LIBNAME/build/publications/maven-central/artifacts/
	cp $LIBNAME/build/publications/gpr/pom-default.xml $LIBNAME/build/publications/maven-central/artifacts/$LIBNAME-$VERSION.pom
	pushd $LIBNAME/build/publications/maven-central/artifacts
	for FILE in *; do
		gpg --armor --detach-sign $FILE
		sha1sum $FILE | cut -d ' ' -f 1 > $FILE.sha1
		md5sum $FILE | cut -d ' ' -f 1 > $FILE.md5
	; done
	cd ..
	mkdir -p $GROUPDIR/$LIBNAME/$VERSION
	mv artifacts/* $GROUPDIR/$LIBNAME/$VERSION/
	rm -rf artifacts
	zip $LIBNAME-$VERSION.zip * $GROUPDIR/$LIBNAME/$VERSION/*
	rm -rf io
	popd

### Upload to Maven Central

Login to https://central.sonatype.com/

If you haven't already, "[Register/Add a Namespace](https://blog.samzhu.dev/2024/04/20/Publishing-Your-Package-to-Maven-Central-in-2024/#Register-a-Namespace)".

Now, "Publish Component". For the "Deployment Name", use e.g. `io.github.baylorpaul:micronaut-json-api:1.0.0`
Upload the zip file in `micronaut-json-api/build/publications/maven-central`

## Alternative Notes

For signing in Gradle, you may add to the plugins:
```groovy
plugins {
	id("signing")
}
```

Then also add:
```groovy
signing {
    sign publishing.publications
}
```

You may also provide credentials by temporarily adding keys to `gradle.properties` such as:
```properties
# Last 8 characters in your GPG public key
signing.keyId=ABCD1234
# Your signing passphrase
signing.password=topSecretPassphrase
# private key secring.gpg file path
signing.secretKeyRingFile=/Users/johndoe/.gnupg/secring.gpg
```

Generate and sign the JAR artifacts

	./gradlew sign
