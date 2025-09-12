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
git tag v<VERSION>
```

### 3. Deploy to Maven Central

Deploy the artifacts to Maven Central:

```bash
# Deploy to Maven Central
mvn deploy -Prelease -DskipRemoteStaging -DaltStagingDirectory=/tmp/docfix-deploy -Dmaven.install.skip
```

### 4. Monitor and Verify Deployment

Monitor the deployment through the Central Portal:

1. Go to [Central Portal](https://central.sonatype.com/)
2. Log in with your Sonatype credentials
3. Navigate to "Deployments" to view deployment status
4. Artifacts are automatically validated and published to Maven Central
5. Publication typically takes 10-30 minutes after successful deployment

### 5. Prepare for Next Development Iteration

Update to the next SNAPSHOT version:

```bash
# Update to next development version
mvn versions:set -DnewVersion=<NEXT-VERSION>-SNAPSHOT

# Commit the version change
git add .
git commit -m "Prepare for next development iteration: <NEXT-VERSION>-SNAPSHOT"
git push origin main
git push origin v<VERSION>
```

## Verification

After release, verify the artifacts are available:

1. Check [Maven Central Search](https://search.maven.org/search?q=g:com.elharo.docfix)
2. Verify both artifacts are available:
   - `com.elharo.docfix:docfix`
   - `com.elharo.docfix:docfix-maven-plugin`

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