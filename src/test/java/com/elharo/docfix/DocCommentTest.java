package com.elharo.docfix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.elharo.docfix.DocComment.Kind;
import java.util.List;
import org.junit.Test;

public class DocCommentTest {

  @Test
  public void testParse_description() {
    DocComment docComment = DocComment.parse(Kind.FIELD, "    /**\n     * The real part of the complex number.\n    */");
    assertEquals("The real part of the complex number.", docComment.getDescription());
    assertEquals(Kind.FIELD, docComment.getKind());
  }

  @Test
  public void testMultiParagraphClassComment() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " * Represents a complex number and provides methods for common\n"
            + " * arithmetic operations.\n"
            + " * <p>\n"
            + " * A complex number is a number that can be expressed in the form a + bi,\n"
            + " * where a and b are real numbers, and i is the imaginary unit, satisfying\n"
            + " * the equation i² = -1.\n"
            + " * </p>\n"
            + " * <p>\n"
            + " * This class provides methods to add, subtract, multiply, and divide\n"
            + " * complex numbers, as well as methods to compute the magnitude and\n"
            + " * phase of a complex number.\n"
            + " * </p>\n"
            + " *\n"
            + " * @author John Doe\n"
            + " * @version 1.0\n"
            + " */\n");
    String java = docComment.toJava();
    assertTrue(docComment.toString(), java.contains("\n * Represents a complex number and provides methods for common\n"));
    assertTrue(docComment.toString(), java.contains("\n * <p>\n"));
    assertTrue(java.endsWith(" */"));
  }

  @Test
  public void testParse_blockTags() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real The real part\n"
            + "     * @param imaginary The imaginary part\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue(docComment.toString(), java.contains("\n     * Constructs"));
    assertTrue(docComment.toString(), java.contains("@param real "));
    assertTrue(docComment.toString(), java.contains(" *\n"));
    assertEquals("Constructs a complex number with the specified real and imaginary parts.", docComment.getDescription());
    assertEquals(Kind.METHOD, docComment.getKind());

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(2, tags.size());
    assertEquals("param", tags.get(0).getType());
    assertEquals("the real part", tags.get(0).getText());
    assertEquals("real", tags.get(0).getArgument());
    assertEquals("param", tags.get(1).getType());
    assertEquals("the imaginary part", tags.get(1).getText());
    assertEquals("imaginary", tags.get(1).getArgument());
  }

  @Test
  public void testParse_capitalizes() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real the real part\n"
            + "     * @param imaginary the imaginary part\n"
            + "     */");
    assertEquals("Constructs a complex number with the specified real and imaginary parts.", docComment.getDescription());
    assertEquals(Kind.METHOD, docComment.getKind());

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(2, tags.size());
    assertEquals("param", tags.get(0).getType());
    assertEquals("the real part", tags.get(0).getText());
    assertEquals("real", tags.get(0).getArgument());
    assertEquals("param", tags.get(1).getType());
    assertEquals("the imaginary part", tags.get(1).getText());
    assertEquals("imaginary", tags.get(1).getArgument());
  }

  // https://github.com/elharo/docfix/issues/46
  @Test
  public void testParse_doesntLowerCaseAcronyms() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real the real part\n"
            + "     * @throws IOException IO exception\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(2, tags.size());
    assertEquals("throws", tags.get(1).getType());
    assertEquals("IO exception", tags.get(1).getText());
    assertEquals("IOException", tags.get(1).getArgument());
  }

  @Test
  public void testParse_return() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Returns a hash code value for this complex number.\n"
            + "     *\n"
            + "     * @return a hash code value\n"
            + "     */\n");
    assertEquals("Returns a hash code value for this complex number.", docComment.getDescription());
    assertEquals(Kind.METHOD, docComment.getKind());

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(1, tags.size());
    assertEquals("return", tags.get(0).getType());
    assertEquals("a hash code value", tags.get(0).getText());
  }

  @Test
  public void testToJava() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + "     * constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real The real part\n"
            + "     * @param imaginary the imaginary part\n"
            + "     */");
    String javaCode = docComment.toJava();
    assertTrue(javaCode.contains("Constructs a complex number with the specified real and imaginary parts.\n"));
    assertTrue(javaCode.contains("@param real the real part\n"));
    assertTrue(javaCode.contains("@param imaginary the imaginary part\n"));
  }
}
