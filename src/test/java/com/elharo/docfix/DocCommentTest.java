package com.elharo.docfix;

import static org.junit.Assert.assertEquals;

import com.elharo.docfix.DocComment.Kind;
import org.junit.Test;

public class DocCommentTest {

    @Test
    public void testParse_description() {
      DocComment docComment = DocComment.parse(Kind.FIELD, "    /**\n     * The real part of the complex number.\n    */");
      assertEquals("The real part of the complex number.", docComment.getDescription());
      assertEquals(Kind.FIELD, docComment.getKind());
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
    assertEquals("Constructs a complex number with the specified real and imaginary parts.", docComment.getDescription());
    assertEquals(Kind.METHOD, docComment.getKind());
  }
}
