# docfix
Fix Common Javadoc Problems

DocFix is a Java utility that automatically fixes common Javadoc
formatting issues to conform to [Oracle Javadoc
conventions](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
and the [JavaDoc Documentation Comment Specification for the Standard Doclet](https://docs.oracle.com/en/java/javase/25/docs/specs/javadoc/doc-comment-spec.html).
It only edits traditional documentation comments (the ones that begin `/**`).
It does not make any edits to the newer markdown comments introduced in Java 23.

The goal is to correctly format most real world Javadoc. 
It maintains proper formatting while applying fixes
and preserving the overall structure and content of the documentation.
This tool is opinionated, so it will reformat some idiosyncratic Javadoc
into more conventional styles.

There are malformed edge conditions that this tool does not handle.
These are typically cases where the code doesn't compile or the Javadoc is
so malformed that it cannot be handled by the javadoc tool.

## What it fixes

- Converts the first letter of Javadoc class, method, and field comments to uppercase
- Fixes @param, @return, and @throws tag comment descriptions to start with lowercase
- Removes redundant "return" or "returns" at the start of @return tag descriptions
- Adds missing periods to the end of comment descriptions
- Removes extraneous periods from the end of block tag descriptions
- Removes extraneous hyphens from the start of block tag descriptions
- Removes blank @param, @return, and @throws tags (tags with no argument and no description)
- Removes blank lines in and after the block tag sections
- Removes excess white space after the asterisks
- Removes trailing white space from Javadoc comments
- Ensures there's a blank line between the description and the first block tag
- Ensures there's a line break after /** in multi-line comments
- Reorders block tags in order @author, @version, @param, @return, @throws, @see, @since, @serial, @deprecated

## Requirements

- Java 11 or higher
- Maven (for building from source)

## Building

```bash
mvn clean compile
```

This project uses [Maven reproducible builds](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) with a fixed timestamp. This ensures that building the same source code produces identical artifacts regardless of when or where the build is executed.

## Usage

### Command Line

If the project you want to fix is built with Maven, in the directory containing the files you want to fix, run: 

```bash
mvn com.elharo.docfix:docfix-maven-plugin:1.0.4:fix
```

This should fix all the .java files in src/main/java. It won't touch files in src/test or non-Java files.

You can run DocFix on non-Maven projects using Java 11 or later with the compiled cli jar:

```bash
mvn clean package
java -jar core/target/docfix-1.0.5-SNAPSHOT-cli.jar [--dryrun] <file-or-directory>
```

This enables you to process any particular file or directory regardless of location or build system. 

### Options

- `--dryrun`: Preview changes without modifying files. Shows what would be changed.
- `<file-or-directory>`: Path to a single Java file or directory to process

### Examples

**Fix a single Java file:**
```bash
java -jar core/target/docfix-1.0.5-SNAPSHOT-cli.jar src/main/java/MyClass.java
```

**Fix all Java files in a directory (recursively):**
```bash
java -jar core/target/docfix-1.0.5-SNAPSHOT-cli.jar src/main/java
```


**Preview changes without modifying files:**
```bash
java -jar core/target/docfix-1.0.5-SNAPSHOT-cli.jar --dryrun src/main/java
```

### Maven Plugin

You can also use DocFix as a Maven plugin to fix Javadoc comments during your build process:

```bash
mvn com.elharo.docfix:docfix-maven-plugin:fix
```

The plugin processes all Java files in `src/main/java` by default and does not touch files in `src/test/java`.

#### Maven Plugin Options

- **Dry-run mode:** Preview changes without modifying files:
  ```bash
  mvn com.elharo.docfix:docfix-maven-plugin:fix -Ddocfix.dryrun=true
  ```

- **Custom encoding:** Specify character encoding:
  ```bash
  mvn com.elharo.docfix:docfix-maven-plugin:fix -Dencoding=ISO-8859-1
  ```

#### Adding to Your Project

To include the plugin in your project's build process, add it to your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.elharo.docfix</groupId>
      <artifactId>docfix</artifactId>
      <version>1.0.1</version>
      <executions>
        <execution>
          <goals>
            <goal>fix</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Programmatic Usage

You can also use DocFix programmatically in your Java code:

```java
import com.elharo.docfix.DocFix;
import java.nio.file.Paths;

// Fix a string containing Java code
String originalCode = "..."; // Your Java code with Javadoc
String fixedCode = DocFix.fix(originalCode);

// Fix a file directly
DocFix.fix(Paths.get("path/to/MyClass.java"));
```

## Example

**Before:**
```java
/**
 *  The real part of the complex number
 */
private final double real;

/**
 * @param real The real part.
 * @return The result.
 */
public double getReal(double real) {
    return real;
}
```

**After:**
```java
/**
 * The real part of the complex number.
 */
private final double real;

/**
 * @param real the real part
 * @return the result
 */
public double getReal(double real) {
    return real;
}
```


## Reporting Bugs

Use the [GitHub Issue Tracker](https://github.com/elharo/docfix/issues).
Please include a sample Javadoc comment
that this tool can't handle or handles incorrectly.

## How it works

DocFix parses Java source files, locates Javadoc comments, and applies formatting fixes
according to Oracle's Javadoc conventions. The tool processes files recursively when given
a directory and only modifies `.java` files, leaving other file types unchanged.
