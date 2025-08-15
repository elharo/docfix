package com.elharo.docfix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
  public void testNoExtraSpaceInBlankLines() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " * Represents a complex number and provides methods for common\n"
            + " * arithmetic operations.\n"
            + " *\n"
            + " * <p>\n"
            + " * This class provides methods to add, subtract, multiply, and divide\n"
            + " * phase of a complex number.\n"
            + " * </p>\n"
            + " */\n");
    String java = docComment.toJava();
    assertFalse(java.contains("* \n"));
    assertTrue(java.contains("*\n"));
  }

  @Test
  public void testParse_blockTags() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real The real part.\n"
            + "     * @param imaginary The imaginary part.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue(docComment.toString(), java.contains("\n     * Constructs"));
    assertTrue(docComment.toString(), java.contains("@param real "));
    assertTrue(docComment.toString(), java.contains(" *\n"));
    assertEquals("Constructs a complex number with the specified real and imaginary parts.\n", docComment.getDescription());
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
  public void testParse_alignedBlockTags() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real      The real part.\n"
            + "     * @param imaginary The imaginary part.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue(java, java.contains("@param real      the real part"));
    assertTrue(java, java.contains("@param imaginary the imaginary part"));
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
    assertEquals("Constructs a complex number with the specified real and imaginary parts.\n", docComment.getDescription());
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
    assertEquals("Returns a hash code value for this complex number.\n", docComment.getDescription());
    assertEquals(Kind.METHOD, docComment.getKind());

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(1, tags.size());
    assertEquals("return", tags.get(0).getType());
    assertEquals("a hash code value", tags.get(0).getText());
  }

  @Test
  public void testParse_multilineBlockTag() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Does something.\n"
            + "     *\n"
            + "     * @throws IllegalArgumentException some exception\n"
            + "     *     if something goes wrong\n"
            + "     */\n");
    assertEquals(Kind.METHOD, docComment.getKind());

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(1, tags.size());
    assertEquals("throws", tags.get(0).getType());
    String java = tags.get(0).toJava();
    assertEquals(java, "     * @throws IllegalArgumentException some exception\n"
        + "     *     if something goes wrong", java);
  }

  @Test
  public void testParse_methodCommentWithBlankLines() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "     /**\n"
            + "      * Parse output timestamp configured for Reproducible Builds' archive entries.\n"
            + "      *\n"
            + "      * <p>Either as {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE_TIME} or as a number representing seconds\n"
            + "      * since the epoch (like <a href=\"https://reproducible-builds.org/docs/source-date-epoch/\">SOURCE_DATE_EPOCH</a>).\n"
            + "      *\n"
            + "      * <p>Since 3.6.4, if not configured or disabled, the {@code SOURCE_DATE_EPOCH} environment variable is used as\n"
            + "      * a fallback value, to ease forcing Reproducible Build externally when the build has not enabled it natively in POM.\n"
            + "      *\n"
            + "      * @param outputTimestamp the value of {@code project.build.outputTimestamp} (may be {@code null})\n"
            + "      * @since 3.6.0\n"
            + "      * @see #parseBuildOutputTimestamp(String)\n"
            + "      */\n");
    String description = docComment.getDescription();
    assertTrue(description, description.startsWith("Parse output timestamp configured for Reproducible Builds' archive entries.\n\n"));
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
    assertTrue(javaCode, javaCode.contains("@param real the real part\n"));
    assertTrue(javaCode.contains("@param imaginary the imaginary part\n"));
  }
}
