package com.elharo.docfix;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     * prerequisite for tests of the functionality. This is traditionally
     * a rather tricky thing to make work, and one I almost always have trouble with.
     * Copilot+ChatGPT did a pretty good job here.
     */
    @Test
    public void testComplexNumberResourceContainsChatGPT() {
        assertTrue("Resource does not contain 'ChatGPT'", code.contains("ChatGPT"));
    }

    /** 
     * I need to think about the API now. This is where TDD shines.
     * Probably all I really want is a method that takes as an argument
     * a string containing the code and returns a string containing the fixed code.
     * I can add overloaded variants that take a file, input stream, or reader.
     * I would have come up with something much more complex if I jumped straight to implementation.
     */
    @Test
    public void testDocFix_noInitialCaps() {
        String fixed = DocFix.fix(code);
        assertFalse(fixed, fixed.contains("     * The real part of the complex number.\n"));
        assertTrue(fixed, fixed.contains("     * the real part of the complex number.\n"));
    }

    /** 
     * Now I'll add another failing test and see what ChatGPT suggests.
     * OK, not so great. Now it's just replacing two very specific strings.
     * Let's do better.
     * 
     * We'll need to do a little work by hand.
     */
    @Test
    public void testDocFix_noInitialCaps_anotherMethod() {
        //String fixed = DocFix.fix(code);
        // assertFalse(fixed, fixed.contains("     * The imaginary part of the complex number.\n"));
        //assertTrue(fixed, fixed.contains("     * the imaginary part of the complex number.\n"));
    }

    // Now let's see if we can load a file rather than a resource
    @Test
    public void testLoadFile() throws IOException {
        Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
        Path tempFile = Files.createTempFile("ComplexNumber", ".java");
        Files.writeString(tempFile, Files.readString(file, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        DocFix.fix(tempFile);
        String fixed = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertFalse(fixed, fixed.contains("     * The imaginary part of the complex number.\n"));
        assertTrue(fixed, fixed.contains("     * the imaginary part of the complex number.\n"));
    }

}
