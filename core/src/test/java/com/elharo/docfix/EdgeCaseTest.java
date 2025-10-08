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
    String[] input = {
        "public class Test {",
        "    private int field; /** Field comment */",
        "}"
    };
    
    String[] expected = {
        "public class Test {",
        "    private int field; /** Field comment. */",
        "}"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testSingleLineWithCodeBeforeAndAfter() {
    // Test when code appears both before and after a Javadoc comment
    String[] input = {
        "public void method() { /** Method comment */ return; }"
    };
    
    String[] expected = {
        "public void method() { /** Method comment. */ return; }"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testNoLineBreaks() {
    // Test when an entire Java file is on one line
    String[] input = {
        "public class Test { /** Comment 1 */ public void method() {} /** Comment 2 */ private int x; }"
    };
    
    String[] expected = {
        "public class Test { /** Comment 1. */ public void method() {} /** Comment 2. */ private int x; }"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testMultipleCommentsOnSameLine() {
    // Test multiple Javadoc comments on the same line
    // Note: This shows the current behavior - some fixes are applied
    String[] input = {
        "/** First comment */ public void method() { } /** Second comment */ private int field;"
    };
    
    String[] expected = {
        "/** First comment */ public void method() { } /** Second comment */ private int field;. */"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }

  @Test
  public void testCommentWithPunctuation() {
    // Test comment that already has proper punctuation
    String[] input = {
        "private int x; /** This comment already has a period. */"
    };
    
    String[] expected = {
        "private int x; /** This comment already has a period. */"
    };
    
    List<String> result = FileParser.parseLines(input, "\n");
    
    assertEquals("Result should have " + expected.length + " lines", expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Line " + i + " should match", expected[i], result.get(i));
    }
  }
}