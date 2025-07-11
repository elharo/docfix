package com.elharo.docfix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DocCommentTest {

    @Test
    public void testParse() {
      DocComment docComment = DocComment.parse("    /**\n     * The real part of the complex number.\n");
      assertEquals("The real part of the complex number.", docComment.getDescription());
    }
}
