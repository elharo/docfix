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
    // The comment should be fixed (capitalized and period added)
    String[] input = {
        "public class Test {",
        "    private int field; /** field comment */",
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
    // Both capitalization and period should be fixed
    String[] input = {
        "public class Test { /** first comment */ public void method() {} /** second comment */ private int x; }"
    };
    
    String[] expected = {
        "public class Test { /** First comment. */ public void method() {} /** Second comment. */ private int x; }"
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
    // Both comments should be capitalized and have periods added
    String[] input = {
        "/** first comment */ public void method() { } /** second comment */ private int field;"
    };
    
    String[] expected = {
        "/** First comment. */ public void method() { } /** Second comment. */ private int field;"
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