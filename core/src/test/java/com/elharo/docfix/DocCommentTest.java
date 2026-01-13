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
  public void testAuthorTag() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " * Represents a complex number and provides methods for common\n"
            + " * arithmetic operations.\n"
            + " *\n"
            + " * @author John Doe\n"
            + " * @version 1.0\n"
            + " */\n");
    String java = docComment.toJava();
    assertTrue(java, java.contains(" * @author John Doe\n"));
  }

  @Test
  public void testEmptyComment() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " *\n"
            + " *    \n"
            + " *\n"
            + " */\n");
    String java = docComment.toJava();
    assertEquals("", java);
  }

  @Test
  public void testEmptySingleLineComment() {
    DocComment docComment = DocComment.parse(Kind.CLASS, "   /**   */");
    String java = docComment.toJava();
    assertEquals("", java);
  }

  @Test
  public void testInlineReturn() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " * {@return the name of the object}\n"
            + " */");
    String java = docComment.toJava();
    assertTrue(java, java.contains(" * {@return the name of the object}\n"));
  }

  @Test
  public void testRemovePeriod() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "  /**\n"
            + "   * Returns a String containing the scheme name of the PointerPart \n"
            + "   * or the name of the ShortHand Pointer.       \n"
            + "   *\n"
            + "   * @return A String containing the scheme name of the PointerPart. \n"
            + "   *\n"
            + "   */");
    String java = docComment.toJava();
    assertTrue(java, java.contains("@return a String containing the scheme name of the PointerPart\n"));
  }

  @Test
  public void testAddPeriod() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "  /**\n"
            + "   * Returns the scheme name\n"
            + "   *\n"
            + "   * @return A String containing the scheme name of the PointerPart. \n"
            + "   *\n"
            + "   */");
    String java = docComment.toJava();
    assertTrue(java, java.contains("Returns the scheme name.\n"));
  }

  @Test
  public void testExtraSpaceBeforeTag() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /** Create a new context.\n"
            + "     *\n"
            + "     *  @param contextSupport the context-support\n"
            + "     *  @param prefix the prefix\n"
            + "     */");
    String java = docComment.toJava();
    assertFalse(java, java.contains("context-support."));
    assertTrue(java, java.contains("* @param contextSupport the context-support\n"));
    assertTrue(java, java.contains("* @param prefix the prefix\n"));
  }

  @Test
  public void testDontAddPeriod() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "  /**\n"
            + "   * Returns the scheme name<p>\n"
            + "   * @return A String containing the scheme name of the PointerPart. \n"
            + "   */");
    String java = docComment.toJava();
    assertTrue(java, java.contains("Returns the scheme name<p>\n"));
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
  public void testDeindent() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/** Represents a complex number and provides methods for common\n"
            + " *  arithmetic operations.\n"
            + " *\n"
            + " *  This class provides methods to add, subtract, multiply, and divide\n"
            + " *  phase of a complex number.\n"
            + " */\n");
    String java = docComment.toJava();
    assertFalse(java, java.contains("*  "));
    assertTrue(java.contains(" * Represents"));
    assertTrue(java.contains(" * arithmetic operations."));
  }

  @Test
  public void testFindPostAsteriskIndent() {
    String docComment =
        "/** Represents a complex number and provides methods for common\n"
            + " *  arithmetic operations.\n"
            + " *\n"
            + " *  This class provides methods to add, subtract, multiply, and divide\n"
            + " *  phase of a complex number.\n"
            + " */\n";
    assertEquals(2, DocComment.findPostAsteriskIndent(docComment));
  }

  @Test
  public void testFindPostAsteriskIndentWithBlockTags() {
    String docComment =
        "  /**\n"
            + "   *  List all the nodes selected by this XPath\n"
            + "   *  expression. If multiple nodes match, multiple nodes\n"
            + "   *  are returned. Nodes are returned\n"
            + "   *  in document-order, as defined by the XPath\n"
            + "   *  specification. If the expression selects a non-node-set\n"
            + "   *  (i.e. a number, boolean, or string) then a List\n"
            + "   *  containing just that one object is returned.\n"
            + "   *\n"
            + "   * @param node the node, node-set or Context object for evaluation. \n"
            + "   *     This value can be null.\n"
            + "   */\n";
    assertEquals(2, DocComment.findPostAsteriskIndent(docComment));
  }

  @Test
  public void testSingleLine() {
    DocComment docComment = DocComment.parse(Kind.FIELD,
        "/** a single line comment */");
    String java = docComment.toJava();
    assertEquals("/** A single line comment. */", java);
  }

  @Test
  public void testExtraAsterisks() {
    DocComment docComment = DocComment.parse(Kind.FIELD,
        "/** a single line comment **/");
    String java = docComment.toJava();
    assertEquals("/** A single line comment. */", java);
  }

  @Test
  public void testTagsOnly() {
    String raw = "    /**\n"
        + "     * @param config  {@link ManifestConfiguration}\n"
        + "     * @param entries the entries\n"
        + "     */";
    DocComment docComment = DocComment.parse(Kind.FIELD,
        raw);
    String java = docComment.toJava();
    assertEquals(raw, java);
  }

  @Test
  public void testExtraAsterisksMultiLine() {
    DocComment docComment = DocComment.parse(Kind.FIELD,
        "/** \n"
            + "* a longer comment\n"
            + "**/\n");
    String java = docComment.toJava();
    assertEquals("/**\n * A longer comment.\n */", java);
  }

  @Test
  public void testDontLowercaseSee() {
    DocComment docComment = DocComment.parse(Kind.FIELD,
        "    /**\n"
            + "     * Override default behavior so that if deep is true, children are also\n"
            + "     * toggled.\n"
            + "     * @see Node\n"
            + "     */");
    String java = docComment.toJava();
    assertEquals(        "    /**\n"
        + "     * Override default behavior so that if deep is true, children are also\n"
        + "     * toggled.\n"
        + "     *\n"
        + "     * @see Node\n"
        + "     */", java);
  }

  @Test
  public void testEmpty() {
    DocComment docComment = DocComment.parse(Kind.FIELD, "");
    String java = docComment.toJava();
    assertEquals("", java);
  }

  @Test
  public void testPreserveDoubleSpace() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * <p>getManifest.</p>\n"
            + "     *\n"
            + "     * @param config  {@link ManifestConfiguration}\n"
            + "     * @param entries The entries.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue(java.contains("config  {@link"));
    assertFalse(java.contains("config {@link"));
  }

  @Test
  public void testRemoveTrailingBlankLine() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "  /**\n"
            + "   * Attribute represents an XML attribute.\n"
            + "   *\n"
            + "   * @author Joe Smith\n"
            + "   * @since PR-DOM-Level-1-19980818\n"
            + "   * @xerces.internal\n"
            + "   *\n"
            + "   */");

    BlockTag lastTag = docComment.getBlockTags().get(2);
    assertEquals("xerces.internal", lastTag.getType());
    assertEquals("", lastTag.getText());
    String java = docComment.toJava();
    assertTrue(java, java.endsWith("@xerces.internal\n   */"));
  }

  @Test
  public void testRemoveMultipleTrailingBlankLine() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " * \n"
            + " * <p>\n"
            + " * A read-only list of elements for traversal purposes.\n"
            + " * Changes to the document from which this list was generated\n"
            + " * are not reflected in this list. Changes to the individual \n"
            + " * <code>Element</code> objects in the list are reflected.\n"
            + " * </p>\n"
            + " * \n"
            + " * @author Elliotte Rusty Harold\n"
            + " * @version 1.3.0\n"
            + " * \n"
            + " *\n"
            + " */");

    String java = docComment.toJava();
    assertTrue(java, java.endsWith("@version 1.3.0\n */"));
  }

  @Test
  public void testCustomTags() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * <p>getManifest.</p>\n"
            + "     *\n"
            + "     * @param config  {@link ManifestConfiguration}\n"
            + "     * @param entries The entries.\n"
            + "     * @custom.foo    something  something\n"
            + "     * @bar  something else\n"
            + "     *    again\n"
            + "     */");
    List<BlockTag> blockTags = docComment.getBlockTags();
    assertEquals(4, blockTags.size());
    String java = docComment.toJava();
    assertTrue(java, java.contains("     * @custom.foo   something  something\n"));
    assertTrue(java, java.contains("     * @bar something else\n     *    again\n"));
  }

  @Test
  public void testPreserveTrailingWhiteSpaceInDescription() {
    // as long as nothing else in the line is changed
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * <p>\n"
            + "     * Sets the name the document type declaration specifies \n"
            + "     * for the root element. In an invalid document, this may \n"
            + "     * not be the same as the actual root element name. \n"
            + "     * </p>\n"
            + "     *\n"
            + "     * @param name the root element name given by\n"
            + "     *     the document type declaration\n"
            + "     * @throws IllegalNameException if the root element name is not\n"
            + "     *     a legal XML 1.0 name\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue(java, java.contains("Sets the name the document type declaration specifies \n"));
    assertTrue(java, java.contains(" not be the same as the actual root element name. \n"));
  }

  @Test
  public void testSortTags() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * a class\n"
            + "     *\n"
            + "     * @version 1.0\n"
            + "     * @author Barney Google\n"
            + "     */");
    List<BlockTag> blockTags = docComment.getBlockTags();
    assertEquals(2, blockTags.size());
    assertEquals("author", blockTags.get(0).getType());
    assertEquals("version", blockTags.get(1).getType());
  }

  @Test
  public void testSortTagsComprehensive() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method with many tags\n"
            + "     *\n"
            + "     * @custom.tag custom tag\n"
            + "     * @deprecated this is deprecated\n"
            + "     * @see SomeClass\n"
            + "     * @throws IOException if IO fails\n"
            + "     * @param name the name parameter\n"
            + "     * @since 1.0\n"
            + "     * @throws IllegalArgumentException if argument is invalid\n"
            + "     * @return the result\n"
            + "     * @param age the age parameter\n"
            + "     * @author John Doe\n"
            + "     * @version 2.0\n"
            + "     * @serial serial info\n"
            + "     */");
    List<BlockTag> blockTags = docComment.getBlockTags();
    
    // Expected order: @author, @version, @param (name), @param (age), @return, 
    // @throws (IllegalArgumentException), @throws (IOException), @see, @since, @serial, @deprecated, @custom.tag
    assertEquals(12, blockTags.size());
    assertEquals("author", blockTags.get(0).getType());
    assertEquals("version", blockTags.get(1).getType());
    assertEquals("param", blockTags.get(2).getType());
    assertEquals("name", blockTags.get(2).getArgument());
    assertEquals("param", blockTags.get(3).getType());
    assertEquals("age", blockTags.get(3).getArgument());
    assertEquals("return", blockTags.get(4).getType());
    assertEquals("throws", blockTags.get(5).getType());
    assertEquals("IllegalArgumentException", blockTags.get(5).getArgument());
    assertEquals("throws", blockTags.get(6).getType());
    assertEquals("IOException", blockTags.get(6).getArgument());
    assertEquals("see", blockTags.get(7).getType());
    assertEquals("since", blockTags.get(8).getType());
    assertEquals("serial", blockTags.get(9).getType());
    assertEquals("deprecated", blockTags.get(10).getType());
    assertEquals("custom.tag", blockTags.get(11).getType());
  }

  @Test
  public void testSortTagsThrowsAlphabetical() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method with multiple throws tags\n"
            + "     *\n"
            + "     * @throws ZException last exception\n"
            + "     * @throws IOException io exception\n"
            + "     * @throws IllegalArgumentException illegal arg\n"
            + "     * @throws AException first exception\n"
            + "     */");
    List<BlockTag> blockTags = docComment.getBlockTags();
    
    // @throws tags should be sorted alphabetically by exception name (case-insensitive)
    assertEquals(4, blockTags.size());
    assertEquals("throws", blockTags.get(0).getType());
    assertEquals("AException", blockTags.get(0).getArgument());
    assertEquals("throws", blockTags.get(1).getType());
    assertEquals("IllegalArgumentException", blockTags.get(1).getArgument());
    assertEquals("throws", blockTags.get(2).getType());
    assertEquals("IOException", blockTags.get(2).getArgument());
    assertEquals("throws", blockTags.get(3).getType());
    assertEquals("ZException", blockTags.get(3).getArgument());
  }

  @Test
  public void testSortTagsPreserveOrderWithinType() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method with multiple same-type tags\n"
            + "     *\n"
            + "     * @see SecondClass\n"
            + "     * @see FirstClass\n"
            + "     * @custom.foo second custom\n"
            + "     * @custom.foo first custom\n"
            + "     */");
    List<BlockTag> blockTags = docComment.getBlockTags();
    
    // Non-throws tags should preserve original order within same type
    assertEquals(4, blockTags.size());
    assertEquals("see", blockTags.get(0).getType());
    assertEquals("SecondClass", blockTags.get(0).getText());
    assertEquals("see", blockTags.get(1).getType());
    assertEquals("FirstClass", blockTags.get(1).getText());
    assertEquals("custom.foo", blockTags.get(2).getType());
    assertEquals("second", blockTags.get(2).getArgument());
    assertEquals("custom", blockTags.get(2).getText());
    assertEquals("custom.foo", blockTags.get(3).getType());
    assertEquals("first", blockTags.get(3).getArgument());
    assertEquals("custom", blockTags.get(3).getText());
  }

  @Test
  public void testPreserveIndentationAfterAsterisk() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " * This class represents an XML element. Each\n"
            + " * element has the following properties:\n"
            + " *\n"
            + " * <ul>\n"
            + " *   <li>Local name</li>\n"
            + " *   <li>Prefix (which may be null or the empty string)</li>\n"
            + " *   <li>Namespace URI (which may be null or the empty string)</li>\n"
            + " *   <li>A list of attributes</li>\n"
            + " *   <li>A list of namespace declarations for this element\n"
            + " *       (not including those inherited from its parent)</li>\n"
            + " *   <li>A list of child nodes</li>\n"
            + " * </ul>\n"
            + " */");
    
    String description = docComment.getDescription();
    String java = docComment.toJava();
    
    // Check that indentation is preserved in the description
    assertTrue("Should preserve indentation for list items", 
        description.contains("  <li>Local name</li>"));
    assertTrue("Should preserve indentation for continuation lines", 
        description.contains("      (not including those inherited from its parent)"));
    
    // Check that the generated Java output preserves indentation
    assertTrue("Generated Java should preserve list item indentation", 
        java.contains(" *   <li>Local name</li>"));
    assertTrue("Generated Java should preserve continuation line indentation", 
        java.contains(" *       (not including those inherited from its parent)"));
  }

  @Test
  public void testAddSpaceAfterAsteriskWhenMissing() {
    // Test that a space is added after asterisk when missing but content exists
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " *This is a comment with no space after asterisk\n"
            + " *   But this line has indentation\n"
            + " */");
    
    String description = docComment.getDescription();
    String java = docComment.toJava();
    
    // The description should parse the content correctly
    assertTrue("Should handle content with no space after asterisk", 
        description.contains("This is a comment with no space after asterisk"));
    
    // The second line should preserve its indentation
    assertTrue("Should preserve indentation even when first line has no space", 
        description.contains("  But this line has indentation"));
    
    // The generated Java output should add the missing space after asterisk
    assertTrue("Generated Java should add space after asterisk for first line", 
        java.contains(" * This is a comment with no space after asterisk"));
    
    // The generated Java output should preserve indentation for the second line
    assertTrue("Generated Java should preserve indentation for second line", 
        java.contains(" *   But this line has indentation"));
  }

  @Test
  public void testPreserveMultipleSpacesAfterAsterisk() {
    // Test that multiple spaces are preserved for code formatting
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + " * Example code:\n"
            + " *     int x = 5;\n"
            + " *     if (x > 0) {\n"
            + " *         System.out.println(\"positive\");\n"
            + " *     }\n"
            + " */");
    
    String description = docComment.getDescription();
    String java = docComment.toJava();
    
    // Check that code indentation is preserved
    assertTrue("Should preserve code indentation", 
        description.contains("    int x = 5;"));
    assertTrue("Should preserve nested code indentation", 
        description.contains("        System.out.println(\"positive\");"));
    
    // Check in generated output
    assertTrue("Generated Java should preserve code indentation", 
        java.contains(" *     int x = 5;"));
    assertTrue("Generated Java should preserve nested code indentation", 
        java.contains(" *         System.out.println(\"positive\");"));
  }

  @Test
  public void testSortTagsSerialVariants() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method with serial variants\n"
            + "     *\n"
            + "     * @serialData some data\n"
            + "     * @serialField some field\n"
            + "     * @serial some serial\n"
            + "     */");
    List<BlockTag> blockTags = docComment.getBlockTags();
    
    // All serial variants should be grouped together in the serial position
    assertEquals(3, blockTags.size());
    assertEquals("serialData", blockTags.get(0).getType());
    assertEquals("serialField", blockTags.get(1).getType());
    assertEquals("serial", blockTags.get(2).getType());
  }

  @Test
  public void testSortTagsThrowsCaseInsensitive() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method with case-sensitive exception names\n"
            + "     *\n"
            + "     * @throws ioException lowercase io\n"
            + "     * @throws IOException uppercase IO\n"
            + "     * @throws IllegalArgumentException mixed case\n"
            + "     * @throws aException lowercase a\n"
            + "     */");
    List<BlockTag> blockTags = docComment.getBlockTags();
    
    // @throws tags should be sorted case-insensitively by exception name
    assertEquals(4, blockTags.size());
    assertEquals("throws", blockTags.get(0).getType());
    assertEquals("aException", blockTags.get(0).getArgument());
    assertEquals("throws", blockTags.get(1).getType());
    assertEquals("IllegalArgumentException", blockTags.get(1).getArgument());
    assertEquals("throws", blockTags.get(2).getType());
    assertEquals("ioException", blockTags.get(2).getArgument());
    assertEquals("throws", blockTags.get(3).getType());
    assertEquals("IOException", blockTags.get(3).getArgument());
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
  public void testRemoveInitialHyphens() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "     /**\n"
            + "     * Evaluates an XML resource with respect to an XPointer expressions\n"
            + "     * by checking if it's element and attributes parameters match the\n"
            + "     * criteria specified in the xpointer expression.\n"
            + "     *\n"
            + "     * @param element - The name of the element\n"
            + "     * @param attributes - The element attributes\n"
            + "     * @param augs - Additional information that may include infoset augmentations\n"
            + "     * @param event - An integer indicating\n"
            + "     *                0 - The start of an element\n"
            + "     *                1 - The end of an element\n"
            + "     *                2 - An empty element call\n"
            + "     * @return - true if the element was resolved by the xpointer\n"
            + "     * @throws XNIException thrown to signal an error\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue(java, java.contains("element the name of the element"));
    assertTrue(java, java.contains("attributes the element attributes"));
    assertTrue(java, java.contains("@return true if the"));
    assertTrue(java, java.contains("0 - The start of an element"));
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
  public void testParse_doesntLowerCaseProperNouns() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real the real part\n"
            + "     * @return Java representation of the number\n"
            + "     * @throws IOException if URL cannot be accessed\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(3, tags.size());
    assertEquals("return", tags.get(1).getType());
    assertEquals("Java representation of the number", tags.get(1).getText());
    assertEquals("throws", tags.get(2).getType());
    assertEquals("if URL cannot be accessed", tags.get(2).getText());
  }

  @Test
  public void testParse_doesntLowerCaseMoreAcronyms() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @param real the real part\n"
            + "     * @return API response as JSON\n"
            + "     * @throws Exception if JDK version is incompatible\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(3, tags.size());
    assertEquals("return", tags.get(1).getType());
    assertEquals("API response as JSON", tags.get(1).getText());
    assertEquals("throws", tags.get(2).getType());
    assertEquals("if JDK version is incompatible", tags.get(2).getText());
  }

  @Test
  public void testParse_recognizesCommonProperNames() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * processes data.\n"
            + "     *\n"
            + "     * @param name the person's name\n"
            + "     * @return John Smith if found\n"
            + "     * @throws Exception if Michael cannot be located\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(3, tags.size());
    
    // Check @return preserves "John"
    assertEquals("return", tags.get(1).getType());
    assertEquals("John Smith if found", tags.get(1).getText());
    
    // Check @throws preserves "Michael"
    assertEquals("throws", tags.get(2).getType());
    assertEquals("if Michael cannot be located", tags.get(2).getText());
  }

  @Test
  public void testParse_comprehensiveProperNounAndAcronymHandling() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * processes HTTP requests using Java APIs.\n"
            + "     *\n"
            + "     * @param url the URL to connect to\n"
            + "     * @param config XML configuration for the API\n"
            + "     * @return HTML response or JSON data\n"
            + "     * @throws IOException if I/O operation fails\n"
            + "     * @throws Exception if JDK or HTTP protocol has issues\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(5, tags.size()); // 2 @param + 1 @return + 2 @throws
    
    // Tags are sorted: @param first, then @return, then @throws
    
    // Check @param url preservation of URL
    assertEquals("param", tags.get(0).getType());
    assertEquals("the URL to connect to", tags.get(0).getText());
    assertEquals("url", tags.get(0).getArgument());
    
    // Check @param config preservation of XML and API
    assertEquals("param", tags.get(1).getType());
    assertEquals("XML configuration for the API", tags.get(1).getText());
    assertEquals("config", tags.get(1).getArgument());
    
    // Check @return preservation of HTML and JSON
    assertEquals("return", tags.get(2).getType());
    assertEquals("HTML response or JSON data", tags.get(2).getText());
    
    // Check first @throws (Exception comes before IOException alphabetically)
    assertEquals("throws", tags.get(3).getType());
    assertEquals("if JDK or HTTP protocol has issues", tags.get(3).getText());
    assertEquals("Exception", tags.get(3).getArgument());
    
    // Check second @throws preservation of I/O
    assertEquals("throws", tags.get(4).getType());
    assertEquals("if I/O operation fails", tags.get(4).getText());
    assertEquals("IOException", tags.get(4).getArgument());
  }

  @Test
  public void testConvertExceptionToThrows() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * constructs a complex number with the specified real and imaginary parts.\n"
            + "     *\n"
            + "     * @exception IOException if an I/O error occurs\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(1, tags.size());
    assertEquals("throws", tags.get(0).getType());
    assertEquals("if an I/O error occurs", tags.get(0).getText());
    assertEquals("IOException", tags.get(0).getArgument());
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
    String java = tags.get(0).toJava(true);
    assertEquals(java, " * @throws IllegalArgumentException some exception\n"
        + "     *     if something goes wrong\n", java);
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

  @Test
  public void testPreservePeriodInMultilineBlockTag() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + "     * Creates a new attribute.\n"
            + "     *\n"
            + "     * @throws IllegalDataException if the value contains characters\n"
            + "     *     which are not legal in XML such as vertical tab or a null.\n"
            + "     *     Characters such as \" and &amp; are legal, but will be\n"
            + "     *     automatically escaped when the attribute is serialized.\n"
            + "     */");
    String javaCode = docComment.toJava();
    
    // The period after "serialized" should be preserved because the text
    // contains multiple sentences (indicated by ". " after "null")
    assertTrue("Period after 'serialized' should be preserved", 
        javaCode.contains("automatically escaped when the attribute is serialized."));
  }

  @Test
  public void testDontAddPeriodAfterHttpsURL() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "/**\n"
            + " * File origin:\n"
            + " * https://github.com/gradle/gradle/blob/v5.6.2/subprojects/launcher/src/main/java/org/gradle/launcher/daemon/client/DaemonClientConnection.java\n"
            + " */");
    String java = docComment.toJava();
    assertTrue("Should not add period after HTTPS URL", 
        java.contains("DaemonClientConnection.java\n"));
    assertFalse("Should not add period after URL", 
        java.contains("DaemonClientConnection.java.\n"));
  }

  @Test
  public void testDontAddPeriodAfterHttpURL() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + " * See documentation at http://example.com/docs\n"
            + " */");
    String java = docComment.toJava();
    assertTrue("Should not add period after HTTP URL", 
        java.contains("http://example.com/docs\n"));
    assertFalse("Should not add period after URL", 
        java.contains("http://example.com/docs.\n"));
  }

  @Test
  public void testDontAddPeriodAfterFtpURL() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + " * Download from ftp://ftp.example.com/files/\n"
            + " */");
    String java = docComment.toJava();
    assertTrue("Should not add period after FTP URL", 
        java.contains("ftp://ftp.example.com/files/\n"));
    assertFalse("Should not add period after URL", 
        java.contains("ftp://ftp.example.com/files/.\n"));
  }

  @Test
  public void testAddPeriodAfterWwwURL() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + " * Visit www.example.com\n"
            + " */");
    String java = docComment.toJava();
    assertTrue("Should add period after www URL", 
        java.contains("www.example.com.\n"));
    assertFalse("Should not leave www URL without period", 
        java.contains("www.example.com\n"));
  }

  @Test
  public void testAddPeriodAfterURLInMiddle() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + " * Visit https://example.com and then do something else\n"
            + " */");
    String java = docComment.toJava();
    assertTrue("Should add period when URL is not at end", 
        java.contains("do something else.\n"));
  }

  @Test
  public void testAddPeriodWhenURLInMiddleOfText() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + " * Visit www.example.com for more info\n"
            + " */");
    String java = docComment.toJava();
    assertTrue("Should add period when text continues after URL", 
        java.contains("for more info.\n"));
  }

  @Test
  public void testAddPeriodForNormalText() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "/**\n"
            + " * This is a normal comment without URLs\n"
            + " */");
    String java = docComment.toJava();
    assertTrue("Should add period for normal text", 
        java.contains("without URLs.\n"));
  }

  @Test
  public void testDeprecatedTagCapitalizationNotAdjusted() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A deprecated method.\n"
            + "     *\n"
            + "     * @param name the name parameter\n"
            + "     * @deprecated This method is deprecated. Use the newer version instead.\n"
            + "     * @return something useful\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(3, tags.size());
    
    // Check that @param is lowercased as expected (order: param=2)
    assertEquals("param", tags.get(0).getType());
    assertEquals("the name parameter", tags.get(0).getText());
    
    // Check that @return is lowercased as expected (order: return=3)
    assertEquals("return", tags.get(1).getType());
    assertEquals("something useful", tags.get(1).getText());
    
    // Check that @deprecated maintains its capitalization (order: deprecated=8)
    assertEquals("deprecated", tags.get(2).getType());
    assertEquals("This method is deprecated. Use the newer version instead.", tags.get(2).getText());
  }

  @Test
  public void testDeprecatedTagVariousCapitalizations() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A deprecated class.\n"
            + "     *\n"
            + "     * @deprecated This class is obsolete\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(1, tags.size());
    assertEquals("deprecated", tags.get(0).getType());
    assertEquals("This class is obsolete", tags.get(0).getText());
  }

  @Test
  public void testDeprecatedTagLowercaseStart() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method with lowercase deprecated tag.\n"
            + "     *\n"
            + "     * @deprecated this method should not be used anymore\n"
            + "     */");

    List<BlockTag> tags = docComment.getBlockTags();
    assertEquals(1, tags.size());
    assertEquals("deprecated", tags.get(0).getType());
    // Should preserve the original lowercase start
    assertEquals("this method should not be used anymore", tags.get(0).getText());
  }

  @Test
  public void testSingleTagCollapseExtraSpaces() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Creates a new forked compiler.\n"
            + "     *\n"
            + "     * @param mojo  the MOJO from which to get the configuration\n"
            + "     */");
    String java = docComment.toJava();
    // When there's only one tag, extra spaces should collapse to single space
    assertTrue("Should collapse extra spaces for single tag",
        java.contains("@param mojo the MOJO"));
    assertFalse("Should not preserve extra spaces for single tag",
        java.contains("@param mojo  the MOJO"));
  }

  @Test
  public void testMultipleTagsPreserveAlignment() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Creates a new forked compiler.\n"
            + "     *\n"
            + "     * @param mojo      the MOJO from which to get the configuration\n"
            + "     * @param something another parameter\n"
            + "     */");
    String java = docComment.toJava();
    // When there are multiple tags, alignment spaces should be preserved
    assertTrue("Should preserve alignment for multiple tags",
        java.contains("@param mojo      the MOJO"));
    assertTrue("Should preserve alignment for multiple tags",
        java.contains("@param something another parameter"));
  }

  @Test
  public void testI212() {
    DocComment docComment = DocComment.parse(null, "/**/");
    String java = docComment.toJava();
    assertEquals("", java);
  }

  @Test
  public void testRemoveBlankReturn() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Test method with blank return tag.\n"
            + "     *\n"
            + "     * @return\n"
            + "     */");
    String java = docComment.toJava();
    assertFalse("Should remove blank @return tag", java.contains("@return"));
    assertTrue("Should keep description", java.contains("Test method with blank return tag."));
  }

  @Test
  public void testRemoveBlankParam() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Test method with blank param tag.\n"
            + "     *\n"
            + "     * @param\n"
            + "     * @param value the value\n"
            + "     */");
    String java = docComment.toJava();
    assertFalse("Should remove blank @param tag with no argument", java.contains("@param\n"));
    assertTrue("Should keep valid @param tag", java.contains("@param value the value"));
    assertTrue("Should keep description", java.contains("Test method with blank param tag."));
  }

  @Test
  public void testRemoveBlankThrows() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Test method with blank throws tag.\n"
            + "     *\n"
            + "     * @throws\n"
            + "     */");
    String java = docComment.toJava();
    assertFalse("Should remove blank @throws tag", java.contains("@throws"));
    assertTrue("Should keep description", java.contains("Test method with blank throws tag."));
  }

  @Test
  public void testKeepParamWithArgumentOnly() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Test method.\n"
            + "     *\n"
            + "     * @param name\n"
            + "     * @param value the value\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should keep @param with argument even without description", java.contains("@param name"));
    assertTrue("Should keep @param with both argument and description", java.contains("@param value the value"));
  }

  @Test
  public void testKeepThrowsWithArgumentOnly() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Test method.\n"
            + "     *\n"
            + "     * @throws IllegalArgumentException\n"
            + "     * @throws NullPointerException if null\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should keep @throws with exception type even without description", java.contains("@throws IllegalArgumentException"));
    assertTrue("Should keep @throws with both exception type and description", java.contains("@throws NullPointerException if null"));
  }

  @Test
  public void testRemoveRedundantReturnsLowercase() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Formats a message.\n"
            + "     *\n"
            + "     * @return returns the formatted message\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should remove redundant 'returns' from @return tag", java.contains("@return the formatted message"));
    assertFalse("Should not contain '@return returns'", java.contains("@return returns"));
  }

  @Test
  public void testRemoveRedundantReturnLowercase() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Gets a value.\n"
            + "     *\n"
            + "     * @return return the value\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should remove redundant 'return' from @return tag", java.contains("@return the value"));
    assertFalse("Should not contain '@return return'", java.contains("@return return"));
  }

  @Test
  public void testRemoveRedundantReturnsMixedCase() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Calculates something.\n"
            + "     *\n"
            + "     * @return Returns the result\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should remove redundant 'Returns' from @return tag", java.contains("@return the result"));
    assertFalse("Should not contain '@return Returns'", java.contains("@return Returns"));
  }

  @Test
  public void testRemoveRedundantReturnUppercase() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Gets data.\n"
            + "     *\n"
            + "     * @return RETURN the data\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should remove redundant 'RETURN' from @return tag", java.contains("@return the data"));
    assertFalse("Should not contain '@return RETURN'", java.contains("@return RETURN"));
  }

  @Test
  public void testDoNotRemoveReturnFromMiddleOfSentence() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Checks return status.\n"
            + "     *\n"
            + "     * @return a return value if available\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should not remove 'return' when it's not at the start", 
        java.contains("@return a return value if available"));
  }

  @Test
  public void testRemoveRedundantReturnsWithNoSpace() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Gets something.\n"
            + "     *\n"
            + "     * @return returnsValue\n"
            + "     */");
    String java = docComment.toJava();
    // Should not remove when there's no space after "returns"
    assertTrue("Should not remove 'returns' when not followed by space", 
        java.contains("@return returnsValue"));
  }

  @Test
  public void testRemoveRedundantReturnsPreservesCapitalization() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * Gets a name.\n"
            + "     *\n"
            + "     * @return returns john's name\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should remove 'returns' from @return tag", 
        java.contains("@return john's name"));
  }

  @Test
  public void testPreservePeriodForIncAbbreviation() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A class.\n"
            + "     *\n"
            + "     * @author Rahul Srivastava, Sun Microsystems Inc.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after Inc. abbreviation", 
        java.contains("@author Rahul Srivastava, Sun Microsystems Inc.\n"));
    assertFalse("Should not remove period from Inc.", 
        java.contains("Sun Microsystems Inc\n"));
  }

  @Test
  public void testPreservePeriodForLtdAbbreviation() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A class.\n"
            + "     *\n"
            + "     * @author Jane Doe, Microsoft Ltd.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after Ltd. abbreviation", 
        java.contains("@author Jane Doe, Microsoft Ltd.\n"));
  }

  @Test
  public void testPreservePeriodForCorpAbbreviation() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A class.\n"
            + "     *\n"
            + "     * @author John Smith, IBM Corp.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after Corp. abbreviation", 
        java.contains("@author John Smith, IBM Corp.\n"));
  }

  @Test
  public void testPreservePeriodForJrAbbreviation() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A class.\n"
            + "     *\n"
            + "     * @author Bob Johnson Jr.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after Jr. abbreviation", 
        java.contains("@author Bob Johnson Jr.\n"));
  }

  @Test
  public void testPreservePeriodForCoAbbreviation() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A class.\n"
            + "     *\n"
            + "     * @author Alice Williams, Acme Co.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after Co. abbreviation", 
        java.contains("@author Alice Williams, Acme Co.\n"));
  }

  @Test
  public void testPreservePeriodForEtcInParam() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method.\n"
            + "     *\n"
            + "     * @param name text ending with etc.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after etc. abbreviation in param", 
        java.contains("@param name text ending with etc.\n"));
  }

  @Test
  public void testPreservePeriodForStInParam() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method.\n"
            + "     *\n"
            + "     * @param address located on Main St.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after St. abbreviation in param", 
        java.contains("@param address located on Main St.\n"));
  }

  @Test
  public void testPreservePeriodForEgInReturn() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method.\n"
            + "     *\n"
            + "     * @return data from database e.g. user records\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after e.g. abbreviation in return", 
        java.contains("@return data from database e.g. user records\n"));
  }

  @Test
  public void testPreservePeriodForProfInSee() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method.\n"
            + "     *\n"
            + "     * @see Prof. Brown's research paper\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after Prof. abbreviation in see", 
        java.contains("@see Prof. Brown's research paper\n"));
  }

  @Test
  public void testPreservePeriodForIeInSince() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A class.\n"
            + "     *\n"
            + "     * @since version 1.0 i.e. initial release\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after i.e. abbreviation in since", 
        java.contains("@since version 1.0 i.e. initial release\n"));
  }

  @Test
  public void testPreservePeriodForMultipleAbbreviations() {
    DocComment docComment = DocComment.parse(Kind.CLASS,
        "    /**\n"
            + "     * A comprehensive test.\n"
            + "     *\n"
            + "     * @author Dr. Smith, Research Corp.\n"
            + "     * @author Prof. Johnson Jr.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should preserve period after Corp. in first author", 
        java.contains("@author Dr. Smith, Research Corp.\n"));
    assertTrue("Should preserve period after Jr. in second author", 
        java.contains("@author Prof. Johnson Jr.\n"));
  }

  @Test
  public void testRemovePeriodForNonAbbreviation() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method.\n"
            + "     *\n"
            + "     * @param name the name of the person.\n"
            + "     */");
    String java = docComment.toJava();
    assertTrue("Should remove period from non-abbreviation ending", 
        java.contains("@param name the name of the person\n"));
    assertFalse("Should not keep period for non-abbreviation", 
        java.contains("the name of the person.\n"));
  }

  @Test
  public void testPreservePeriodInMiddleOfText() {
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "    /**\n"
            + "     * A method.\n"
            + "     *\n"
            + "     * @param company the company name like Acme Inc. or others\n"
            + "     */");
    String java = docComment.toJava();
    // The period after "Inc." should be preserved, but the one after "others" should be removed
    assertTrue("Should preserve period after Inc. in middle of text", 
        java.contains("Acme Inc."));
    assertTrue("Should remove period after 'others' at end", 
        java.contains("or others\n"));
  }

  @Test
  public void testReturnTagWithExtraSpaces() {
    // Test case from issue: DocFix fails to lowercase return tag when there are extra spaces
    DocComment docComment = DocComment.parse(Kind.METHOD,
        "  /**\n"
            + "   * Removes a grammar.\n"
            + "   *\n"
            + "   * @param desc The Grammar Description.\n"
            + "   * @return     The removed grammar.\n"
            + "   */");
    String java = docComment.toJava();
    // The @return description should be lowercased even with extra spaces
    assertTrue("Return tag should be lowercased", 
        java.contains("@return the removed grammar\n"));
    // The @param should also be lowercased
    assertTrue("Param tag should be lowercased", 
        java.contains("@param desc the Grammar Description\n"));
  }

  @Test
  public void testSerialVersionUIDMultilineNotCapitalized() {
    DocComment docComment = DocComment.parse(Kind.FIELD,
        "/**\n"
            + " * serialVersionUID for serialization.\n"
            + " */");
    String java = docComment.toJava();
    // serialVersionUID should not be capitalized
    assertTrue("serialVersionUID should not be capitalized", 
        java.contains(" * serialVersionUID for serialization.\n"));
    assertFalse("Should not capitalize serialVersionUID", 
        java.contains(" * SerialVersionUID"));
  }

  @Test
  public void testSerialPersistentFieldsMultilineNotCapitalized() {
    DocComment docComment = DocComment.parse(Kind.FIELD,
        "/**\n"
            + " * serialPersistentFields declares which fields are serialized.\n"
            + " */");
    String java = docComment.toJava();
    // serialPersistentFields should not be capitalized
    assertTrue("serialPersistentFields should not be capitalized", 
        java.contains(" * serialPersistentFields declares which fields are serialized.\n"));
    assertFalse("Should not capitalize serialPersistentFields", 
        java.contains(" * SerialPersistentFields"));
  }
}
