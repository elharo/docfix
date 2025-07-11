package com.elharo.docfix;

import static org.junit.Assert.assertEquals;

import com.elharo.docfix.DocComment.Kind;
import org.junit.Test;

public class DocCommentTest {

    @Test
    public void testParse() {
      DocComment docComment = DocComment.parse(Kind.FIELD, "    /**\n     * The real part of the complex number.\n");
      assertEquals("The real part of the complex number.", docComment.getDescription());
      assertEquals(Kind.FIELD, docComment.getKind());
    }
}
