package com.elharo.docfix;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

public class DocFixTest {

    /**
     * Holds the contents of ComplexNumber.java test resource.
     */
    private String code;

    /**
     * Loads the ComplexNumber.java resource into the code field before each test.
     */
    @Before
    public void setUp() throws IOException {
        String resourcePath = "/com/elharo/math/ComplexNumber.java";
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull("Resource not found: " + resourcePath, in);
            byte[] bytes = in.readAllBytes();
            code = new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * Test that ComplexNumber.java contains the string "ChatGPT".
     * Mostly this just verifies that we can read a test resource as a
     * prerequisite for tests fo the functionality. This is traditionally
     * a rather tricky thing to make work, and one I almost always have trouble with.
     * Copilot+ChatGPT did a pretty good job here.
     */
    @Test
    public void testComplexNumberResourceContainsChatGPT() {
        assertTrue("Resource does not contain 'ChatGPT'", code.contains("ChatGPT"));
    }
}
