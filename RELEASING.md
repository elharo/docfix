# Releasing DocFix to Maven Central

This document explains how to upload the DocFix library and Maven plugin to Maven Central.

DocFix is a multi-module Maven project with two artifacts:
- `docfix` - The core library (JAR)
- `docfix-maven-plugin` - The Maven plugin

## Prerequisites

Before releasing, ensure you have completed the one-time setup requirements:

- Sonatype Central account with access to `com.elharo.docfix` groupId
- GPG key for artifact signing
- Maven settings.xml configured with credentials

For detailed setup instructions, see the [Sonatype Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/).

## Release Process

The release process follows a tag-based approach where:

- **Main branch always contains SNAPSHOT versions only**
- **Releases are cut from tags on release branches, never from main**
- **Release branches are not merged back to main**

This ensures main branch remains in active development with snapshot versions while releases are immutable tags.

### 0. Create a release branch

```bash
git checkout -b release/<VERSION>
```

### 1. Prepare the Release

Before releasing, ensure the project is ready:

```bash
# Verify everything compiles and plugins work
mvn clean package
```

### 2. Update Version Numbers

Update the version in the parent POM from SNAPSHOT to the release version:

```bash
# Use Maven versions plugin to update all modules consistently
mvn versions:set -DnewVersion=<VERSION>

# Commit the version change
git add .
git commit -m "Release version <VERSION>"
```

### 3. Tag the Release

Create the release tag directly on the release branch:

```bash
# Ensure you're on the release branch
git checkout release/<VERSION>

# Create and push the release tag
git tag v<VERSION>
git push origin v<VERSION>
```

### 4. Deploy to Maven Central

Deploy the artifacts to Maven Central from the tagged release branch:

```bash
# Ensure you're on the correct tag
git checkout v<VERSION>

# Deploy to Maven Central
mvn deploy -Prelease -DskipRemoteStaging -DaltStagingDirectory=/tmp/docfix-deploy -Dmaven.install.skip
```

### 5. Monitor and Publish Deployment

Monitor and publish the deployment through the Central Portal:

1. Go to [Central Portal](https://central.sonatype.com/)
2. Log in with your Sonatype credentials
3. Navigate to "Deployments" to view deployment status
4. Wait for artifacts to be validated (typically takes a few minutes)
5. Once validation is complete, click the "Publish" button to release artifacts to Maven Central
6. Publication typically takes 10-30 minutes after clicking publish

### 6. Prepare for Next Development Iteration

Update main branch for the next development version:

```bash
# Switch to main branch
git checkout main

# Update to next development version
mvn versions:set -DnewVersion=<NEXT-VERSION>-SNAPSHOT

# Commit the version change
git add .
git commit -m "Prepare for next development iteration: <NEXT-VERSION>-SNAPSHOT"

# Push directly to main
git push origin main
```

Note: This keeps main branch always on a SNAPSHOT version and never contains release versions.

## Verification

After release, verify the artifacts are available for download:

1. **Direct repository check** (available immediately):
   ```bash
   # Test downloading the core library
   mvn dependency:get -Dartifact=com.elharo.docfix:docfix:<VERSION>
   
   # Test downloading the Maven plugin
   mvn dependency:get -Dartifact=com.elharo.docfix:docfix-maven-plugin:<VERSION>
   ```

2. **Direct URL check** (available immediately):
   - Core library: `https://repo1.maven.org/maven2/com/elharo/docfix/docfix/<VERSION>/`
   - Maven plugin: `https://repo1.maven.org/maven2/com/elharo/docfix/docfix-maven-plugin/<VERSION>/`

3. **Maven Central Search** (may take several hours to update):
   - [Search results](https://search.maven.org/search?q=g:com.elharo.docfix)
   - Note: Search indexing can lag behind artifact availability by many hours

## Usage After Release

Once released, users can use the library and plugin:

### Library Dependency

```xml
<dependency>
  <groupId>com.elharo.docfix</groupId>
  <artifactId>docfix</artifactId>
  <version>VERSION</version>
</dependency>
```

### Maven Plugin

```xml
<plugin>
  <groupId>com.elharo.docfix</groupId>
  <artifactId>docfix-maven-plugin</artifactId>
  <version>VERSION</version>
  <executions>
    <execution>
      <goals>
        <goal>fix</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## Troubleshooting

### Common Issues

1. **GPG signing fails**: Ensure your GPG key is properly configured and the keyname matches your settings.xml
2. **401 Unauthorized**: Check your Sonatype credentials in settings.xml
3. **Validation errors**: Ensure all required metadata is present (name, description, url, licenses, developers, SCM)
4. **Key server timeout**: Try uploading to multiple key servers

### Debugging

```bash
# Debug Maven deployment
mvn clean deploy -X

# Test GPG signing
mvn clean verify -Dgpg.skip=false

# Validate POM completeness
mvn help:effective-pom
```

### Support

- [Sonatype Central Documentation](https://central.sonatype.org/publish/publish-guide/)
- [Maven Central Documentation](https://maven.apache.org/repository/guide-central-repository-upload.html)
- [Sonatype Support](https://issues.sonatype.org/)
