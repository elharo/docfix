package com.elharo.docfix;

import org.junit.Test;

import static org.junit.Assert.*;

public class SingleLineCommentTest {

  @Test
  public void testParseSingleLineComment() {
    String raw = "/** This is a single line comment */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals(DocComment.Kind.METHOD, comment.getKind());
    assertEquals("This is a single line comment", comment.getDescription());
    assertTrue(comment.getBlockTags().isEmpty());
  }

  @Test
  public void testToJava() {
    String raw = "/** This is a single line comment */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** This is a single line comment. */", comment.toJava());
  }

  @Test
  public void testWithIndentation() {
    String raw = "    /** Indented single line comment */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("    /** Indented single line comment. */", comment.toJava());
  }

  @Test
  public void testMultiLineCommentReturnsDocComment() {
    String raw = "/**\n * This is a multi-line comment\n */";
    DocComment comment = DocComment.parse(DocComment.Kind.CLASS, raw);

    assertFalse("Should NOT return a SingleLineComment instance",
        comment instanceof SingleLineComment);
  }

  @Test
  public void testCommentWithBlockTagsReturnsDocComment() {
    String raw = "/** This has @param tag */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertFalse("Should NOT return a SingleLineComment instance",
        comment instanceof SingleLineComment);
  }

  @Test
  public void testDoesNotDuplicatePeriod() {
    String raw = "/** This already has a period. */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** This already has a period. */", comment.toJava());
  }

  @Test
  public void testHandlesExclamationMark() {
    String raw = "/** This has an exclamation! */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** This has an exclamation! */", comment.toJava());
  }

  @Test
  public void testHandlesQuestionMark() {
    String raw = "/** Is this a question? */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** Is this a question? */", comment.toJava());
  }
}