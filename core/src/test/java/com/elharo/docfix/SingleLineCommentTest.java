package com.elharo.docfix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SingleLineCommentTest {

  @Test
  public void testParseSingleLineComment() {
    String raw = "/** This is a single line comment */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals(DocComment.Kind.METHOD, comment.getKind());
    assertEquals("This is a single line comment.", comment.getDescription());
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

  @Test
  public void testDontAddPeriodAfterHttpsURL() {
    String raw = "/** See https://example.com/docs */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** See https://example.com/docs */", comment.toJava());
  }

  @Test
  public void testDontAddPeriodAfterHttpURL() {
    String raw = "/** Visit http://www.example.com */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** Visit http://www.example.com */", comment.toJava());
  }

  @Test
  public void testAddPeriodAfterWwwURL() {
    String raw = "/** Check www.example.com */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** Check www.example.com. */", comment.toJava());
  }

  @Test
  public void testDontAddPeriodAfterFtpURL() {
    String raw = "/** Download from ftp://ftp.example.com/files/ */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** Download from ftp://ftp.example.com/files/ */", comment.toJava());
  }

  @Test
  public void testAddPeriodForNormalText() {
    String raw = "/** This is normal text */";
    DocComment comment = DocComment.parse(DocComment.Kind.METHOD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** This is normal text. */", comment.toJava());
  }

  @Test
  public void testSerialVersionUIDNotCapitalized() {
    String raw = "/** serialVersionUID. */";
    DocComment comment = DocComment.parse(DocComment.Kind.FIELD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** serialVersionUID. */", comment.toJava());
  }

  @Test
  public void testSerialVersionUIDWithoutPeriod() {
    String raw = "/** serialVersionUID */";
    DocComment comment = DocComment.parse(DocComment.Kind.FIELD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** serialVersionUID. */", comment.toJava());
  }

  @Test
  public void testSerialPersistentFieldsNotCapitalized() {
    String raw = "/** serialPersistentFields for this class */";
    DocComment comment = DocComment.parse(DocComment.Kind.FIELD, raw);

    assertTrue("Should return a SingleLineComment instance", comment instanceof SingleLineComment);
    assertEquals("/** serialPersistentFields for this class. */", comment.toJava());
  }
}