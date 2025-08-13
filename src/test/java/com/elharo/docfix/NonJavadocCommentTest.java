package com.elharo.docfix;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test that DocFix only modifies Javadoc comments and leaves other comments
 * unchanged. This
 * test exposes a bug where single-line comments are incorrectly moved.
 */
public class NonJavadocCommentTest {

        /**
         * Test that single-line comments following code on the same line are not moved.
         * This test
         * should fail until the bug is fixed.
         */
        @Test
        public void testSingleLineCommentsNotMoved() {
                String code = "package com.example;\n"
                                + "\n"
                                + "public class TestClass {\n"
                                + "    /**\n"
                                + "     * This is a javadoc comment that should be fixed.\n"
                                + "     * @param value The value parameter\n"
                                + "     */\n"
                                + "    public void method(int value) {\n"
                                + "        buf.getChar(); // u2 bootstrap_method_attr_index;\n"
                                + "        buf.getChar(); // u2 name_and_type_index;\n"
                                + "    }\n"
                                + "}\n";

                String fixed = DocFix.fix(code);

                // The Javadoc should be fixed (lowercase first letter in @param)
                assertTrue("Javadoc @param should be fixed", fixed.contains("@param value the value parameter"));

                // But single-line comments should remain on the same line as the code
                assertTrue(
                                "Single-line comment should stay on same line",
                                fixed.contains("buf.getChar(); // u2 bootstrap_method_attr_index;"));
                assertTrue(
                                "Single-line comment should stay on same line",
                                fixed.contains("buf.getChar(); // u2 name_and_type_index;"));

                // These patterns should NOT exist (comments moved to separate lines)
                assertFalse(
                                "Comment should not be moved to separate line",
                                fixed.contains("        // u2 bootstrap_method_attr_index;\n        buf.getChar();"));
                assertFalse(
                                "Comment should not be moved to separate line",
                                fixed.contains("        // u2 name_and_type_index;\n        buf.getChar();"));
        }

        /** Test that block comments are not modified. */
        @Test
        public void testBlockCommentsNotModified() {
                String code = "package com.example;\n"
                                + "\n"
                                + "public class TestClass {\n"
                                + "    /**\n"
                                + "     * This javadoc should be fixed.\n"
                                + "     * @return The return value\n"
                                + "     */\n"
                                + "    public int getValue() {\n"
                                + "        /* This is a block comment\n"
                                + "           that should not be modified */\n"
                                + "        return 42;\n"
                                + "    }\n"
                                + "}\n";

                String fixed = DocFix.fix(code);

                // The Javadoc should be fixed
                assertTrue("Javadoc @return should be fixed", fixed.contains("@return the return value"));

                // Block comment should remain unchanged
                assertTrue(
                                "Block comment should not be modified",
                                fixed.contains("/* This is a block comment\n           that should not be modified */"));
        }

        /** Test the specific case from the bug report with consumeDynamic method. */
        @Test
        public void testConsumeDynamicBugCase() {
                String code = "package com.example;\n"
                                + "\n"
                                + "public class TestClass {\n"
                                + "    /**\n"
                                + "     * Consumes dynamic data from buffer.\n"
                                + "     * @param buf The buffer to read from\n"
                                + "     */\n"
                                + "    private static void consumeDynamic(ByteBuffer buf) {\n"
                                + "        buf.getChar(); // u2 bootstrap_method_attr_index;\n"
                                + "        buf.getChar(); // u2 name_and_type_index;\n"
                                + "    }\n"
                                + "}\n";

                String fixed = DocFix.fix(code);

                // The Javadoc should be fixed
                assertTrue("Javadoc @param should be fixed", fixed.contains("@param buf the buffer to read from"));

                // The single-line comments should stay on the same line as the code
                assertTrue(
                                "First comment should stay inline",
                                fixed.contains("buf.getChar(); // u2 bootstrap_method_attr_index;"));
                assertTrue(
                                "Second comment should stay inline",
                                fixed.contains("buf.getChar(); // u2 name_and_type_index;"));

                // This is the bug pattern that should NOT occur
                String buggyPattern1 = "        // u2 bootstrap_method_attr_index;\n        buf.getChar();";
                String buggyPattern2 = "        // u2 name_and_type_index;\n        buf.getChar();";

                assertFalse(
                                "Comments should not be moved to separate lines (bug pattern 1)",
                                fixed.contains(buggyPattern1));
                assertFalse(
                                "Comments should not be moved to separate lines (bug pattern 2)",
                                fixed.contains(buggyPattern2));
        }
}