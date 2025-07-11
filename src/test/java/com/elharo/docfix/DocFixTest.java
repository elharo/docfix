package com.elharo.docfix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
     * I need to think about the API now. This is where TDD shines.
     * Probably all I really want is a method that takes as an argument
     * a string containing the code and returns a string containing the fixed code.
     * I can add overloaded variants that take a file, input stream, or reader.
     * I would have come up with something much more complex if I jumped straight to implementation.
     */
    @Test
    public void testDocFix_noInitialCaps() {
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("     * The real part of the complex number.\n"));
        assertFalse(fixed, fixed.contains("     * the real part of the complex number.\n"));
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
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("     * The imaginary part of the complex number.\n"));
        assertFalse(fixed, fixed.contains("     * the imaginary part of the complex number.\n"));
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

    /**
     * Test that DocFix.main() applies fixes to a file given as a command line argument.
     * This test should fail until the main method is implemented.
     */
    @Test
    public void testMainFixesFile() throws IOException {
        Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
        Path tempFile = Files.createTempFile("ComplexNumberMain", ".java");
        Files.writeString(tempFile, Files.readString(file, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String[] args = { tempFile.toString() };
        DocFix.main(args);
        String fixed = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertFalse(fixed, fixed.contains("     * The imaginary part of the complex number.\n"));
        assertTrue(fixed, fixed.contains("     * the imaginary part of the complex number.\n"));
    }

    /**
     * Test that DocFix.main() applies fixes to all files in a directory given as a command line argument.
     * The directory should contain two files to fix, and the test should verify that both files are fixed.
     * This test will fail until the main method is updated to support directories.
     */
    @Test
    public void testMainFixesDirectory() throws IOException {
        Path file1 = Files.createTempFile("ComplexNumber1", ".java");
        Path file2 = Files.createTempFile("ComplexNumber2", ".java");
        String original = Files.readString(Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java"), StandardCharsets.UTF_8);
        Files.writeString(file1, original, StandardCharsets.UTF_8);
        Files.writeString(file2, original, StandardCharsets.UTF_8);
        Path dir = Files.createTempDirectory("docfix_test_dir");
        Files.move(file1, dir.resolve(file1.getFileName()));
        Files.move(file2, dir.resolve(file2.getFileName()));
        String[] args = { dir.toString() };
        DocFix.main(args);
        for (Path file : Files.newDirectoryStream(dir, "*.java")) {
            String fixed = Files.readString(file, StandardCharsets.UTF_8);
            assertFalse(fixed.contains("     * The imaginary part of the complex number.\n"));
            assertTrue(fixed.contains("     * the imaginary part of the complex number.\n"));
        }
    }

    /** 
     * At this point, I notice that I haven't actually tested that non .java files are not modified.
     * Let's add a test for that.
     */
    @Test
    public void testDocFix_noModificationOfNonJavaFiles() throws IOException {
        Path file1 = Files.createTempFile("ComplexNumber1", ".java");
        Path file2 = Files.createTempFile("ComplexNumber2", ".java");
        Path file3 = Files.createTempFile("ComplexNumber3", ".txt");
        String original = Files.readString(Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java"), StandardCharsets.UTF_8);
        Files.writeString(file1, original, StandardCharsets.UTF_8);
        Files.writeString(file2, original, StandardCharsets.UTF_8);
        Files.writeString(file3, original, StandardCharsets.UTF_8);
        Path dir = Files.createTempDirectory("docfix_test_dir");
        Files.move(file1, dir.resolve(file1.getFileName()));
        Files.move(file2, dir.resolve(file2.getFileName()));
        Files.move(file3, dir.resolve(file3.getFileName()));
        String[] args = { dir.toString() };
        DocFix.main(args);
        for (Path file : Files.newDirectoryStream(dir, "*.txt")) {
            String originalContent = Files.readString(file, StandardCharsets.UTF_8);
            String fixed = Files.readString(file, StandardCharsets.UTF_8);
            assertEquals(originalContent, fixed);
        }
    }

    /**
     * Now let's add subdirectories and recursion.
     * I started this one with the LLM, but I realized it was trivial to do by hand
     * with some copy pasta and then passing a parent directory to the main method.
     * Joke's on me. It wasn't nearly as trivial as it looked, and my efforts took
     * some real debugging.
     */
    @Test
    public void testMainFixesSubDirectories() throws IOException {
        Path file1 = Files.createTempFile("ComplexNumber1", ".java");
        Path file2 = Files.createTempFile("ComplexNumber2", ".java");
        String original = Files.readString(Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java"), StandardCharsets.UTF_8);
        Files.writeString(file1, original, StandardCharsets.UTF_8);
        Files.writeString(file2, original, StandardCharsets.UTF_8);
        Path dir = Files.createTempDirectory("docfix_test_dir");
        Path subdirectory = Files.createDirectories(dir.resolve("com/elharo/docfix"));
        Files.move(file1, subdirectory.resolve(file1.getFileName()));
        Files.move(file2, subdirectory.resolve(file2.getFileName()));
        String[] args = { dir.toString() };
        DocFix.main(args);
        for (Path file : Files.newDirectoryStream(dir, "*.java")) {
            String fixed = Files.readString(file, StandardCharsets.UTF_8);
            assertFalse(fixed.contains("     * The imaginary part of the complex number.\n"));
            assertTrue(fixed.contains("     * the imaginary part of the complex number.\n"));
        }
    }

    /**
     * Test that DocFix.main() with --dryrun prints the changes but does not modify the file.
     */
    @Test
    public void testMain_dryRunDoesNotModifyFiles() throws IOException {
        Path file = Files.createTempFile("ComplexNumberDryRun", ".java");
        String original = Files.readString(Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java"), StandardCharsets.UTF_8);
        Files.writeString(file, original, StandardCharsets.UTF_8);

        // Capture System.out
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));

        try {
            String[] args = { "--dryrun", file.toString() };
            DocFix.main(args);
        } finally {
            System.setOut(oldOut);
        }

        String after = Files.readString(file, StandardCharsets.UTF_8);
        // File should not be changed
        assertEquals(original, after);

        String output = baos.toString(StandardCharsets.UTF_8);
        // Output should show the fix
        assertTrue(output.contains("     * The imaginary part of the complex number."));
        assertFalse(output.contains("     * the imaginary part of the complex number."));
    }

    @Test
    public void testClassComment() {
        // TODO make fixed a fixture
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("/**\n  * Represents a complex number in the field ℂ.\n */"));
    }

    // needs to work on params
    @Test
    public void testAtParam() {
        String fixed = DocFix.fix(code);
        assertFalse(fixed, fixed.contains("     * @param real The real part"));
        assertTrue(fixed, fixed.contains("     * @param real the real part"));
    }
}
