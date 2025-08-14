package com.elharo.docfix;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class FileParserTest {

  /**
   * Test that FileParser.parseFile correctly parses the ComplexNumber.java file,
   * applying fixes to Javadoc comments while keeping other lines unchanged.
   */
  @Test
  public void testParseComplexNumberFile() throws IOException {
    Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
    List<String> result = FileParser.parseFile(file);

    // Verify we got some results
    assertTrue("Result should not be empty", !result.isEmpty());

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
    List<String> result = FileParser.parseFile(file);

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
    List<String> result = FileParser.parseFile(file);

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
}