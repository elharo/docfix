package com.elharo.docfix;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class FileParserTest {

  /**
   * Reads a Java file line by line and returns a list of strings.
   * If a line starts a Javadoc comment, reads all lines until the end
   * of the Javadoc comment, fixes the comment, and stores that as a single string.
   * Otherwise, stores each individual line as a separate string.
   *
   * @param path the path to the Java file to read
   * @return a list of strings representing the file content
   * @throws IOException if an I/O error occurs reading the file
   */
  private static List<String> parseFile(Path path) throws IOException {
    String code = Files.readString(path, StandardCharsets.UTF_8);
    String lineEnding = Strings.detectLineEnding(code);
    String[] lines = code.split("\\R");
    return FileParser.parseLines(lines, lineEnding);
  }

  /**
   * Test that FileParser.parseFile correctly parses the ComplexNumber.java file,
   * applying fixes to Javadoc comments while keeping other lines unchanged.
   */
  @Test
  public void testParseComplexNumberFile() throws IOException {
    Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
    List<String> result = parseFile(file);

    // Verify we got some results
    assertFalse("Result should not be empty", result.isEmpty());

    // Check that the class-level Javadoc comment is combined into a single string
    boolean foundClassJavadoc = false;
    for (String line : result) {
      if (line.trim().startsWith("/**") && line.contains("Represents a complex number")) {
        foundClassJavadoc = true;
        // Verify it's a complete Javadoc comment (starts with /** and ends with */)
        assertTrue("Class Javadoc should start with /**", line.trim().startsWith("/**"));
        assertTrue("Class Javadoc should end with */", line.trim().endsWith("*/"));
        // Verify it contains multiple lines of the comment
        assertTrue("Class Javadoc should contain author info", line.contains("@author"));
        assertTrue("Class Javadoc should contain description",
            line.contains("arithmetic operations"));
        break;
      }
    }
    assertTrue("Should find class-level Javadoc comment", foundClassJavadoc);

    // Check that method Javadoc comments are combined
    boolean foundMethodJavadoc = false;
    for (String line : result) {
      if (line.contains("Returns the real part of this complex number")) {
        foundMethodJavadoc = true;
        assertTrue("Method Javadoc should start with /**", line.trim().startsWith("/**"));
        assertTrue("Method Javadoc should end with */", line.trim().endsWith("*/"));
        assertTrue("Method Javadoc should contain @return tag", line.contains("@return"));
        break;
      }
    }
    assertTrue("Should find method Javadoc comment", foundMethodJavadoc);

    // Check that regular code lines are separate
    boolean foundPackageDeclaration = false;
    boolean foundClassDeclaration = false;
    for (String line : result) {
      if (line.trim().equals("package com.elharo.math;")) {
        foundPackageDeclaration = true;
        // Should be just the package line, not combined with anything else
        assertFalse("Package line should not contain Javadoc", line.contains("/**"));
      }
      if (line.trim().startsWith("public class ComplexNumber")) {
        foundClassDeclaration = true;
        // Should be just the class declaration line
        assertFalse("Class declaration should not contain Javadoc", line.contains("/**"));
      }
    }
    assertTrue("Should find package declaration", foundPackageDeclaration);
    assertTrue("Should find class declaration", foundClassDeclaration);

    // Verify that single-line comments (if any) are handled correctly
    // and that we don't accidentally combine non-Javadoc comments
    for (String line : result) {
      if (line.trim().startsWith("//")) {
        // Regular single-line comments should remain as individual lines
        assertFalse("Single-line comments should not be combined with other lines",
            line.contains("\n"));
      }
    }
  }

  /**
   * Test that the parser correctly handles a file with various Javadoc comment patterns.
   */
  @Test
  public void testParseFileWithDifferentJavadocPatterns() throws IOException {
    Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
    List<String> result = parseFile(file);

    // Count how many Javadoc blocks we find
    int javadocCount = 0;
    for (String line : result) {
      if (line.trim().startsWith("/**")) {
        javadocCount++;
        // Each Javadoc should be complete
        assertTrue("Javadoc should end with */", line.trim().endsWith("*/"));
      }
    }

    // ComplexNumber.java should have multiple Javadoc comments
    // (class comment + multiple method comments)
    assertTrue("Should find multiple Javadoc comments", javadocCount > 5);
  }

  /**
   * Test that the parser preserves the exact content of Javadoc comments.
   */
  @Test
  public void testJavadocContentPreservation() throws IOException {
    Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
    List<String> result = parseFile(file);

    // Find the constructor Javadoc and verify its content
    boolean foundConstructorJavadoc = false;
    for (String line : result) {
      if (line.contains(
          "Constructs a complex number with the specified real and imaginary parts")) {
        foundConstructorJavadoc = true;
        assertTrue("Should contain @param real", line.contains("@param real"));
        assertTrue("Should contain @param imaginary", line.contains("@param imaginary"));
        assertTrue("Should preserve parameter descriptions", line.contains("the real part"));
        assertTrue("Should preserve parameter descriptions", line.contains("the imaginary part"));
        break;
      }
    }
    assertTrue("Should find constructor Javadoc", foundConstructorJavadoc);
  }

  @Test
  public void testBlankCommentIsRemoved() {
    // Test that empty Javadoc comments are completely removed, not replaced with blank lines
    String[] input = {
        "package test;",
        "",
        "/**",
        " */",
        "class TestClass {",
        "    public void method() {}",
        "}"
    };
    
    String[] expected = {
        "package test;",
        "",
        "class TestClass {",
        "    public void method() {}",
        "}"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testBlankCommentSingleLineIsRemoved() {
    // Test that empty single-line Javadoc comments are completely removed
    String[] input = {
        "package test;",
        "",
        "/** */",
        "class TestClass {",
        "    public void method() {}",
        "}"
    };
    
    String[] expected = {
        "package test;",
        "",
        "class TestClass {",
        "    public void method() {}",
        "}"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testBlankCommentWithIndentationIsRemoved() {
    // Test that empty indented Javadoc comments are completely removed
    String[] input = {
        "package test;",
        "",
        "class TestClass {",
        "    /**",
        "     */",
        "    void method() {}",
        "}"
    };
    
    String[] expected = {
        "package test;",
        "",
        "class TestClass {",
        "    void method() {}",
        "}"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testNonEmptyCommentIsKept() {
    // Test that non-empty comments are still processed normally
    String[] input = {
        "package test;",
        "",
        "/**",
        " * This is a comment",
        " */",
        "class TestClass {",
        "}"
    };
    
    String[] expected = {
        "package test;",
        "",
        "/**\n * This is a comment.\n */",
        "class TestClass {",
        "}"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  // Tests for extractChunks method
  
  @Test
  public void testExtractChunksSimpleClass() throws IOException, JavaParseException {
    String source = "package test;\n" +
                   "\n" +
                   "/**\n" +
                   " * A simple class.\n" +
                   " */\n" +
                   "public class Test {\n" +
                   "}\n";
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    assertEquals("Should have 4 chunks", 4, chunks.size());
    assertEquals("package test;", chunks.get(0));
    assertEquals("", chunks.get(1)); // empty line
    assertEquals("/**\n * A simple class.\n */", chunks.get(2));
    assertEquals("public class Test {\n}", chunks.get(3));
  }
  
  @Test
  public void testExtractChunksWithMultipleJavadocs() throws IOException, JavaParseException {
    String source = "/**\n" +
                   " * Class comment.\n" +
                   " */\n" +
                   "class Test {\n" +
                   "  /**\n" +
                   "   * Method comment.\n" +
                   "   */\n" +
                   "  void method() {\n" +
                   "  }\n" +
                   "}\n";
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    assertEquals("Should have 5 chunks", 5, chunks.size());
    assertEquals("/**\n * Class comment.\n */", chunks.get(0));
    assertEquals("class Test {", chunks.get(1));
    assertEquals("/**\n   * Method comment.\n   */", chunks.get(2));
    assertEquals("void method() {\n  }", chunks.get(3));
    assertEquals("", chunks.get(4)); // trailing empty line
  }
  
  @Test
  public void testExtractChunksRemovesLeadingTrailingLineTerminators() throws IOException, JavaParseException {
    String source = "\npackage test;\n\n" +
                   "/**\n" +
                   " * Comment.\n" +
                   " */\n" +
                   "\nclass Test {\n}\n\n";
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    assertEquals("Should have 4 chunks", 4, chunks.size());
    assertEquals("package test;\n", chunks.get(0)); // leading \n removed
    assertEquals("/**\n * Comment.\n */", chunks.get(1));
    assertEquals("class Test {\n}", chunks.get(2)); // leading \n removed  
    assertEquals("", chunks.get(3)); // trailing \n\n becomes empty
  }
  
  @Test
  public void testExtractChunksPreservesOneLineTerminator() throws IOException, JavaParseException {
    String source = "\n\npackage test;\n\n\n" +
                   "/**\n" +
                   " * Comment.\n" +
                   " */\n\n" +
                   "class Test {\n}\n\n\n";
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    assertEquals("Should have 4 chunks", 4, chunks.size());
    assertEquals("\npackage test;\n\n", chunks.get(0)); // only one leading/trailing \n removed
    assertEquals("/**\n * Comment.\n */", chunks.get(1));
    assertEquals("class Test {\n}\n\n", chunks.get(2)); // only one leading/trailing \n removed
    assertEquals("", chunks.get(3));
  }
  
  @Test
  public void testExtractChunksEmptyInput() throws IOException, JavaParseException {
    List<String> chunks = FileParser.extractChunks(new StringReader(""));
    assertEquals("Empty input should produce empty list", 0, chunks.size());
  }
  
  @Test
  public void testExtractChunksOnlyJavadoc() throws IOException, JavaParseException {
    String source = "/**\n * Only comment.\n */";
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    assertEquals("Should have 1 chunk", 1, chunks.size());
    assertEquals("/**\n * Only comment.\n */", chunks.get(0));
  }
  
  @Test
  public void testExtractChunksNoJavadoc() throws IOException, JavaParseException {
    String source = "package test;\nclass Test {}\n";
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    assertEquals("Should have 1 chunk", 1, chunks.size());
    assertEquals("package test;\nclass Test {}", chunks.get(0));
  }
}