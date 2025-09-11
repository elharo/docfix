# Releasing DocFix to Maven Central

This document explains how to upload the DocFix library and Maven plugin to Maven Central.

DocFix is a multi-module Maven project with two artifacts:
- `docfix` - The core library (JAR)
- `docfix-maven-plugin` - The Maven plugin

## Prerequisites

### 1. Sonatype OSSRH Account

Create a Sonatype OSSRH account and request access to the `com.elharo.docfix` groupId:

1. Create a [Sonatype JIRA account](https://issues.sonatype.org/secure/Signup!default.jspa)
2. Create a ticket to request access to the `com.elharo.docfix` groupId
3. Wait for approval (usually takes 1-2 business days)

### 2. GPG Key Setup

Generate a GPG key pair for signing artifacts:

```bash
# Generate a new GPG key
gpg --gen-key

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format LONG

# Export the public key to a key server
gpg --keyserver hkp://pool.sks-keyservers.net --send-keys YOUR_KEY_ID
gpg --keyserver hkp://keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. Maven Settings Configuration

Configure your `~/.m2/settings.xml` with Sonatype credentials and GPG settings:

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-sonatype-username</username>
      <password>your-sonatype-password</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname>YOUR_KEY_ID</gpg.keyname>
      </properties>
    </profile>
  </profiles>
</settings>
```

## Release Process

### 1. Prepare the Release

Before releasing, ensure all tests pass and the project is ready:

```bash
# Clean build and test
mvn clean test

# Verify everything compiles and plugins work
mvn clean package

# Test the CLI tool
java -cp core/target/classes com.elharo.docfix.DocFix --dryrun core/src/test/resources/

# Test the Maven plugin
mvn com.elharo.docfix:docfix-maven-plugin:fix -Ddocfix.dryrun=true
```

### 2. Update Version Numbers

Update the version in the parent POM from `1.0-SNAPSHOT` to the release version:

```bash
# Use Maven versions plugin to update all modules consistently
mvn versions:set -DnewVersion=1.0

# Commit the version change
git add .
git commit -m "Release version 1.0"
git tag v1.0
```

### 3. Configure POM for Release

Add the following configuration to the parent `pom.xml` for Maven Central deployment:

```xml
<distributionManagement>
  <snapshotRepository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
  </snapshotRepository>
  <repository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
  </repository>
</distributionManagement>

<build>
  <plugins>
    <!-- Source plugin -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-source-plugin</artifactId>
      <version>3.3.0</version>
      <executions>
        <execution>
          <id>attach-sources</id>
          <goals>
            <goal>jar-no-fork</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    
    <!-- Javadoc plugin -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-javadoc-plugin</artifactId>
      <version>3.6.3</version>
      <executions>
        <execution>
          <id>attach-javadocs</id>
          <goals>
            <goal>jar</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    
    <!-- GPG plugin -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-gpg-plugin</artifactId>
      <version>3.1.0</version>
      <executions>
        <execution>
          <id>sign-artifacts</id>
          <phase>verify</phase>
          <goals>
            <goal>sign</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    
    <!-- Nexus staging plugin -->
    <plugin>
      <groupId>org.sonatype.plugins</groupId>
      <artifactId>nexus-staging-maven-plugin</artifactId>
      <version>1.6.13</version>
      <extensions>true</extensions>
      <configuration>
        <serverId>ossrh</serverId>
        <nexusUrl>https://oss.sonatype.org/</nexusUrl>
        <autoReleaseAfterClose>true</autoReleaseAfterClose>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### 4. Deploy to Maven Central

Deploy the artifacts to the staging repository:

```bash
# Deploy to staging repository
mvn clean deploy

# If you want to manually control the release, use:
# mvn clean deploy -P release -Dautorelease=false
```

### 5. Release from Staging

If you disabled auto-release, manually release from Sonatype Nexus:

1. Go to [Sonatype Nexus](https://oss.sonatype.org/)
2. Log in with your Sonatype credentials
3. Click "Staging Repositories" in the left sidebar
4. Find your staging repository (usually `comelharodocfix-XXXX`)
5. Select it and click "Close"
6. Wait for validation to complete
7. Select it again and click "Release"

### 6. Prepare for Next Development Iteration

Update to the next SNAPSHOT version:

```bash
# Update to next development version
mvn versions:set -DnewVersion=1.1-SNAPSHOT

# Commit the version change
git add .
git commit -m "Prepare for next development iteration: 1.1-SNAPSHOT"
git push origin main
git push origin v1.0
```

## Alternative: Using Maven Release Plugin

For a more automated approach, you can use the Maven Release Plugin:

### Configure Release Plugin

Add to parent `pom.xml`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-release-plugin</artifactId>
  <version>3.0.1</version>
  <configuration>
    <autoVersionSubmodules>true</autoVersionSubmodules>
    <useReleaseProfile>false</useReleaseProfile>
    <releaseProfiles>release</releaseProfiles>
    <goals>deploy</goals>
    <tagNameFormat>v@{project.version}</tagNameFormat>
  </configuration>
</plugin>
```

### Perform Release

```bash
# Prepare the release (updates versions, creates tag)
mvn release:prepare

# Perform the release (builds and deploys)
mvn release:perform
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
  <version>1.0</version>
</dependency>
```

### Maven Plugin

```xml
<plugin>
  <groupId>com.elharo.docfix</groupId>
  <artifactId>docfix-maven-plugin</artifactId>
  <version>1.0</version>
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

- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Maven Central Documentation](https://maven.apache.org/repository/guide-central-repository-upload.html)
- [Sonatype Support](https://issues.sonatype.org/)