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
    // Test when code appears before /** on the same line with code after the comment
    // The comment should be moved to its own line before the following code element
    String[] input = {
        "public class Test {",
        "    private int field1; /** field comment */ private int field2;",
        "}"
    };
    
    String[] expected = {
        "public class Test {",
        "    private int field1;",
        "    /** Field comment. */",
        "    private int field2;",
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
    // Each comment should be on its own line immediately before the element it documents
    String[] input = {
        "public class Test { /** first comment */ public void method() {} /** second comment */ private int x; }"
    };
    
    String[] expected = {
        "public class Test {",
        "/** First comment. */",
        "public void method() {}",
        "/** Second comment. */",
        "private int x; }"
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
    // Each comment should be on its own line immediately before the element it documents
    String[] input = {
        "/** first comment */ public void method() { } /** second comment */ private int field;"
    };
    
    String[] expected = {
        "/** First comment. */",
        "public void method() { }",
        "/** Second comment. */",
        "private int field;"
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
    // Comment documents the element that follows it
    String[] input = {
        "/** This comment already has a period. */ private int x;"
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