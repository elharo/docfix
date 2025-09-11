# DocFix - Javadoc Comment Fixer
DocFix is a Java utility that automatically fixes common Javadoc formatting issues to conform to Oracle Javadoc conventions. It processes Java source files and applies fixes while preserving overall structure and content.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively
- Bootstrap, build, and test the repository:
  - `mvn clean compile` -- takes ~3 seconds. NEVER CANCEL. Set timeout to 60+ seconds.
  - `mvn test` -- takes ~4 seconds, runs 68 tests. NEVER CANCEL. Set timeout to 30+ seconds.
  - `mvn clean package` -- takes ~4 seconds total. NEVER CANCEL. Set timeout to 60+ seconds.
- Run the DocFix CLI tool:
  - ALWAYS build first with `mvn clean compile`
  - From compiled classes: `java -cp target/classes com.elharo.docfix.DocFix [--dryrun] <file-or-directory>`
  - From JAR (after `mvn package`): `java -cp target/docfix-1.0-SNAPSHOT.jar com.elharo.docfix.DocFix [--dryrun] <file-or-directory>`
  - Use `--dryrun` to preview changes without modifying files
  - Tool processes single Java files or entire directories recursively

## Validation
- Always run the complete test suite after making changes: `mvn test`
- ALWAYS test the CLI functionality manually when changing core logic:
  - Test single file: `java -cp target/classes com.elharo.docfix.DocFix --dryrun src/test/resources/com/elharo/math/ComplexNumber.java`
  - Test directory: `java -cp target/classes com.elharo.docfix.DocFix --dryrun src/test/resources/com/elharo/math/`
- ALWAYS build and run a complete scenario after making changes
- Check that the tool correctly fixes Javadoc issues without breaking compilation

### Complete Validation Scenario
After making changes, run this complete validation workflow:
```bash
# 1. Clean build and test
mvn clean package

# 2. Create test file with Javadoc issues
cp src/test/resources/com/elharo/math/ComplexNumber.java /tmp/ComplexNumber.java

# 3. Preview fixes
java -cp target/classes com.elharo.docfix.DocFix --dryrun /tmp/ComplexNumber.java

# 4. Apply fixes
java -cp target/classes com.elharo.docfix.DocFix /tmp/ComplexNumber.java

# 5. Verify file still compiles
cd /tmp && javac ComplexNumber.java && echo "SUCCESS!"
```
Expected behavior: Tool fixes @param/@return capitalization (e.g., "The real part" → "the real part"), adds/removes periods appropriately, maintains proper formatting.

## Environment and Dependencies
- Java 11 or higher (Java 17 is installed and works)
- Maven 3.9.x
- JUnit 4.13.2 for testing
- No external runtime dependencies
- Build downloads Maven dependencies on first run (~30 seconds)

## Common Tasks
The following are outputs from frequently run commands. Reference them instead of viewing, searching, or running bash commands to save time.

### Repository Structure
```
src/
├── main/java/com/elharo/docfix/
│   ├── DocFix.java          # Main CLI class and core API
│   ├── DocComment.java      # Javadoc comment parser and formatter
│   ├── BlockTag.java        # Handles @param, @return, @throws tags
│   ├── FileParser.java      # Java source file parser
│   ├── SingleLineComment.java # Single line comment handling
│   └── Strings.java         # String utility methods
└── test/
    ├── java/com/elharo/docfix/  # Unit tests
    └── resources/com/elharo/math/ComplexNumber.java  # Test data
```

### Key Files and What They Do
- `DocFix.java`: Main entry point with CLI and public API methods
- `DocComment.java`: Core logic for parsing and fixing Javadoc comments
- `ComplexNumber.java`: Test resource with various Javadoc issues to fix
- Tests validate fixes for: capitalization, periods, @param/@return formatting, whitespace

### What DocFix Fixes
- Converts first letter of Javadoc descriptions to uppercase
- Fixes @param, @return, @throws descriptions to start with lowercase
- Adds missing periods to comment descriptions
- Removes extraneous periods from block tag descriptions
- Removes blank lines in block tag sections
- Ensures proper spacing and line breaks
- Reorders block tags in conventional order

### Build Times (NEVER CANCEL - use these timeouts)
- `mvn clean compile`: ~3 seconds (timeout: 60+ seconds)
- `mvn test`: ~4 seconds (timeout: 30+ seconds)  
- `mvn clean package`: ~4 seconds (timeout: 60+ seconds)
- First build downloads dependencies: +30 seconds additional time

### Testing Examples
```bash
# Build project
mvn clean compile

# Run all tests (68 tests)
mvn test

# Preview changes without modifying files
java -cp target/classes com.elharo.docfix.DocFix --dryrun src/test/resources/com/elharo/math/ComplexNumber.java

# Apply fixes to a file
java -cp target/classes com.elharo.docfix.DocFix src/test/resources/com/elharo/math/ComplexNumber.java

# Process entire directory
java -cp target/classes com.elharo.docfix.DocFix src/main/java
```

## Project Standards
- Use Java 11+ language features
- Follow Google Java code style
- Include a linefeed as the final character of each source code file
- Package structure: com.elharo.docfix
- Write JUnit 4 tests for new functionality
- Maintain immutability where possible
- Handle edge cases in Javadoc parsing gracefully
- Do not use reflection to test. Unit test through public and package private APIs.
- Do not catch raw java.lang.Exception or java.lang.RuntimeException unless absolutely required by a third party method that throws an undifferentiated exception. Catch only more specific subclasses. Assume most runtime exceptions indicate bugs that should be fixed by preventing the exception from being thrown rather than catching it.
- When writing a PR description include a link to the issue that is being fixed such as "fixes #146" assuming the PR completely resolves the issue.
