package com.elharo.docfix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.List;

/**
 * Tests for edge cases in parsing Javadoc comments when they appear
 * alongside code on the same line or in files with no line breaks.
 */
public class EdgeCaseTest {

  @Test
  public void testSingleLineWithCodeBefore() {
    // Test when code appears before /** on the same line
    // The comment should be moved to its own line before the code,
    // fixed (capitalized and period added), with proper indentation
    String[] input = {
        "public class Test {",
        "    private int field; /** field comment */",
        "}"
    };
    
    String[] expected = {
        "public class Test {",
        "    /** Field comment. */",
        "    private int field;",
        "}"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testSingleLineCommentWithLowercase() {
    // Test that lowercase comments are properly capitalized
    String[] input = {
        "public class Test {",
        "    /** lowercase comment */",
        "    public void method() {}",
        "}"
    };
    
    String[] expected = {
        "public class Test {",
        "    /** Lowercase comment. */",
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
  public void testNoLineBreaks() {
    // Test when an entire Java file is on one line with lowercase comments
    // Comments should be moved to their own lines, capitalized, and have periods added
    String[] input = {
        "public class Test { /** first comment */ public void method() {} /** second comment */ private int x; }"
    };
    
    String[] expected = {
        "/** First comment. */",
        "/** Second comment. */",
        "public class Test { public void method() {} private int x; }"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testMultipleCommentsOnSameLine() {
    // Test multiple Javadoc comments on the same line with lowercase starts
    // Comments should be moved to their own lines, capitalized, and have periods added
    String[] input = {
        "/** first comment */ public void method() { } /** second comment */ private int field;"
    };
    
    String[] expected = {
        "/** First comment. */",
        "/** Second comment. */",
        "public void method() { } private int field;"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testCommentWithPunctuation() {
    // Test comment that already has proper punctuation and capitalization
    // Should still be moved to its own line before the code
    String[] input = {
        "private int x; /** This comment already has a period. */"
    };
    
    String[] expected = {
        "/** This comment already has a period. */",
        "private int x;"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }
}