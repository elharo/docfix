# DocFix Gradle Plugin

Gradle plugin that fixes Javadoc comments to conform to Oracle Javadoc guidelines.

## Usage

### Zero-Install Execution (Recommended)

For projects that use Gradle but may not have the plugin configured, use the init script for zero-install execution:

```bash
gradle docfix --init-script init-docfix.gradle
```

The `init-docfix.gradle` file is included in this directory and can be used directly, or you can download it from the repository.

### Adding to Your Build

Add the plugin to your `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'com.elharo.docfix' version '1.0.6-SNAPSHOT'
}
```

Or for Kotlin DSL (`build.gradle.kts`):

```kotlin
plugins {
    java
    id("com.elharo.docfix") version "1.0.6-SNAPSHOT"
}
```

Then run:

```bash
gradle docfix
```

### Configuration

You can configure the plugin in your build file:

```groovy
docfix {
    sourceDirectory = file('src/main/java')  // Default: src/main/java
    encoding = 'UTF-8'                       // Default: UTF-8
    dryrun = false                           // Default: false
}
```

Or configure via command-line properties:

```bash
gradle docfix -Pdocfix.dryrun=true -Pdocfix.encoding=ISO-8859-1
```

## Building

To build the plugin:

```bash
gradle build
```

To publish to Maven Local:

```bash
gradle publishToMavenLocal
```

## Testing

The plugin includes functional tests that can be run with:

```bash
gradle functionalTest
```

## Requirements

- Gradle 6.0 or higher
- Java 11 or higher
- DocFix core library (included as dependency)
