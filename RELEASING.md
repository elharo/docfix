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

### 0. Set Version Environment Variables

Set environment variables for the release and next development versions:

```bash
# Set the version number for this release (e.g., 1.2.3)
export VERSION=<your-version-number>

# Set the next development version (e.g., 1.3.0)
export NEXT_VERSION=<next-version-number>
```

For example:
```bash
export VERSION=1.2.3
export NEXT_VERSION=1.3.0
```

After setting these variables, all subsequent commands can be copy-pasted without editing.

### 1. Update the Reproducible Build Timestamp

The project uses [Maven reproducible builds](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) to ensure that identical source code produces identical artifacts. Before each release, update the `project.build.outputTimestamp` property in the root `pom.xml` to the current date:

1. Ensure you're on the main branch:
   ```bash
   git checkout main
   git pull origin main
   ```

2. Create a branch for the timestamp update:
   ```bash
   git checkout -b update-timestamp-$VERSION
   ```

3. Open `pom.xml` in the root directory
4. Locate the `<project.build.outputTimestamp>` property in the `<properties>` section
5. Update it to the current date in ISO 8601 format: `YYYY-MM-DDTHH:MM:SSZ`
   - Example: `2025-10-13T00:00:00Z`
   - Use `00:00:00` for the time component
   - Always use UTC timezone (indicated by the `Z` suffix)

6. Commit and push the timestamp update:
   ```bash
   git add pom.xml
   git commit -m "Update reproducible build timestamp for version $VERSION"
   git push origin update-timestamp-$VERSION
   ```

7. Create a pull request from `update-timestamp-$VERSION` to `main` with:
   - Title: "Update reproducible build timestamp for version $VERSION"
   - Description: Sets the reproducible build timestamp for the upcoming release

8. Once the pull request is approved and merged, update your local main branch:
   ```bash
   git checkout main
   git pull origin main
   ```

This timestamp will be embedded in all build artifacts (JARs, etc.) for this release, ensuring reproducibility.

### 2. Create a release branch

After the timestamp update is merged to main, create the release branch:

```bash
git checkout -b release/$VERSION
```

### 3. Update Version Numbers

Update the version in the parent POM from SNAPSHOT to the release version:

```bash
# Use Maven versions plugin to update all modules consistently
mvn versions:set -DnewVersion=$VERSION -DgenerateBackupPoms=false

# Commit the version change
git commit -m "Release version $VERSION"
```

### 4. Prepare the Release

Before releasing, ensure the project is ready:

```bash
# Verify everything compiles and plugins work
mvn clean package
```

### 5. Tag the Release

Create the release tag directly on the release branch:

```bash
# Ensure you're on the release branch
git checkout release/$VERSION

# Create and push the release tag
git tag v$VERSION
git push origin v$VERSION
```

### 6. Deploy to Maven Central

Deploy the artifacts to Maven Central from the tagged release branch:

```bash
# Ensure you're on the correct tag
git checkout v$VERSION

# Deploy to Maven Central
mvn deploy -Prelease -DskipRemoteStaging -DaltStagingDirectory=/tmp/docfix-deploy -Dmaven.install.skip
```

### 7. Monitor and Publish Deployment

Monitor and publish the deployment through the Central Portal:

1. Go to [Central Portal](https://central.sonatype.com/)
2. Log in with your Sonatype credentials
3. Click the Publish link at the top right of the page. 
4. If necessary, wait for artifacts to be validated.
5. Once validation is complete, click the "Publish" button to release artifacts to Maven Central
6. Publication typically takes 10-30 minutes after clicking publish

### 8. Publish GitHub Release

After the Maven Central release is published, create a GitHub release:

1. Navigate to the [Releases page](https://github.com/elharo/docfix/releases) on GitHub
2. Click "Draft a new release"
3. Choose the tag created in step 5 (e.g., `v$VERSION`)
4. Set the release title to the version number (e.g., `$VERSION`)
5. In the release description, include:
   - A brief summary of what's new in this release
   - Major features or bug fixes
   - Any breaking changes or upgrade notes
6. Click "Publish release"

The GitHub release will be associated with the tag and will be visible on the repository's releases page.

### 9. Prepare for Next Development Iteration

Update main branch for the next development version:

```bash
# Switch to main branch and create a new branch for the version bump
git checkout main
git checkout -b prepare-next-development-$NEXT_VERSION

# Update to next development version
mvn versions:set -DnewVersion=$NEXT_VERSION-SNAPSHOT -DgenerateBackupPoms=false

# Commit the version change
git add .
git commit -m "Prepare for next development iteration: $NEXT_VERSION-SNAPSHOT"

# Push the branch and create a pull request
git push origin prepare-next-development-$NEXT_VERSION
```

Then create a pull request from `prepare-next-development-$NEXT_VERSION` to `main` with:
- Title: "Prepare for next development iteration: $NEXT_VERSION-SNAPSHOT"
- Description: Updates version numbers for continued development

Once the pull request is approved and merged, main will be updated with the next SNAPSHOT version.

Note: This keeps main branch always on a SNAPSHOT version and never contains release versions.

### 10. Abandoning a Release

If you need to abandon a release before publishing (e.g., critical issues discovered during deployment), remove the tag:

```bash
# Delete the local tag
git tag -d v$VERSION

# Delete the remote tag
git push origin :refs/tags/v$VERSION
```

After removing the tag:
1. Fix any issues on the release branch or main branch as appropriate
2. If needed, restart the release process from step 0 with the same or different version number
3. The release branch can be deleted if no longer needed: `git branch -D release/$VERSION`

Note: Only remove tags for releases that have not been published to Maven Central. Once published, versions are immutable and a new version must be released instead.

## Verification

After release, verify the artifacts are available for download:

1. **Direct repository check** (available immediately):
   ```bash
   # Test downloading the core library
   mvn dependency:get -Dartifact=com.elharo.docfix:docfix:$VERSION
   
   # Test downloading the Maven plugin
   mvn dependency:get -Dartifact=com.elharo.docfix:docfix-maven-plugin:$VERSION
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
