package com.elharo.docfix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DocFixTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
        assertTrue(fixed, fixed.contains("     * The imaginary part of the complex number."));
        assertTrue(fixed, fixed.contains("     * @return the imaginary part"));
    }

    @Test
    public void testPreserveTrailingSpace() {
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("public class ComplexNumber implements Cloneable { \n"));
    }

    @Test
    public void testDocFix_asterisks() {
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("/**\n * Represents a complex number with real and imaginary parts.\n"));
    }

    @Test
    public void testCommentedOutComment() {
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("    // /** this comment is commented out and should be ignored */"));
    }

    @Test
    public void testDocCommentInString() {
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("\"/** a string literal containing a javadoc comment */\""));
    }

    @Test
    public void testPreserveLineEndings() {
        code = code.replace('\n', '\r');
        String fixed = DocFix.fix(code);
        assertFalse(fixed.contains("\n"));
        assertTrue(fixed.contains("/**\r * Represents a complex number with real and imaginary parts.\r"));
    }

    // #30
    @Test
    public void testDocFix_preservesSingleLineComments() {
      String fixed = DocFix.fix(code);
      assertTrue(fixed, fixed.contains("if (denominator == 0) { // Check for single line comment preservation"));
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
        assertTrue(fixed, fixed.contains("     * The imaginary part of the complex number."));
        assertTrue(fixed, fixed.contains("     * @return the imaginary part"));
    }

    // Now let's see if we can load a file rather than a resource
    @Test
    public void testLoadFile() throws IOException {
        Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
        Path tempFile = Files.createTempFile("ComplexNumber", ".java");
        Files.writeString(tempFile, Files.readString(file, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        DocFix.fix(tempFile);
        String fixed = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertTrue(fixed, fixed.contains("     * The imaginary part of the complex number."));
        assertTrue(fixed, fixed.contains("     * @return the imaginary part"));
    }

    /**
     * Test that DocFix.main() applies fixes to a file given as a command line argument.
     */
    @Test
    public void testMainFixesFile() throws IOException {
        Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
        Path tempFile = Files.createTempFile("ComplexNumberMain", ".java");
        Files.writeString(tempFile, Files.readString(file, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String[] args = { tempFile.toString() };
        DocFix.main(args);
        String fixed = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertTrue(fixed, fixed.contains("     * The imaginary part of the complex number.\n"));
        assertTrue(fixed, fixed.contains("     * @return the imaginary part"));
    }

    /**
     * Test that DocFix.main() applies fixes to all files in a directory given as a command line argument.
     * The directory should contain two files to fix, and the test should verify that both files are fixed.
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
            assertTrue(fixed.contains("     * The imaginary part of the complex number.\n"));
            assertTrue(fixed, fixed.contains("     * @return the imaginary part"));
        }
    }

    /** 
     * At this point, I notice that I haven't actually tested that no .java files are not modified.
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
        for (Path file : Files.newDirectoryStream(dir, "*.java")) {
            String fixed = Files.readString(file, StandardCharsets.UTF_8);
            assertTrue(fixed.contains("     * The imaginary part of the complex number.\n"));
            assertTrue(fixed, fixed.contains("     * @return the imaginary part"));
        }
        for (Path file : Files.newDirectoryStream(dir, "*.txt")) {
            String fixed = Files.readString(file, StandardCharsets.UTF_8);
            assertEquals(original, fixed);
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
    public void testMainDryRunDoesNotModifyFiles() throws IOException {
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
        assertTrue(after.contains("     * The imaginary part of the complex number.\n"));
        assertFalse(after.contains("     * the imaginary part of the complex number.\n"));

        String output = baos.toString(StandardCharsets.UTF_8);
        // Output should show the fix
        assertTrue(output.contains("the imaginary part"));
    }

    /**
     * I need to think about the API now. This is where TDD shines.
     * Probably all I really want is a method that takes as an argument
     * a string containing the code and returns a string containing the fixed code.
     * I can add overloaded variants that take a file, input stream, or reader.
     * I would have come up with something much more complex if I jumped straight to implementation.
     */
    @Test
    public void testAtParam() {
        String fixed = DocFix.fix(code);
        assertFalse(fixed, fixed.contains("     * @param real The real part"));
        assertTrue(fixed, fixed.contains("     * @param real the real part"));
    }

    @Test
    public void testClassComment() {
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains(" * Represents a complex number with real and imaginary parts."));
        assertTrue(fixed, fixed.contains(" * @author ChatGPT"));
    }

    @Test
    public void testSingleLineFieldComment() {
        String fixed = DocFix.fix(code);
        assertTrue(fixed, fixed.contains("    /** The phase (angle) of the complex number in radians. */\n"));
    }

    /**
     * Test that DocFix works with explicit UTF-8 encoding.
     */
    @Test
    public void testFixWithUTF8Encoding() throws IOException {
        Path tempFile = temporaryFolder.newFile("test.java").toPath();
        String javaCode = "/** Constructs a new object with café. */\npublic class Test {}";
        Files.writeString(tempFile, javaCode, StandardCharsets.UTF_8);
        
        // Fix with explicit UTF-8 encoding
        DocFix.fix(tempFile, StandardCharsets.UTF_8);
        
        String result = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertTrue("Should fix Javadoc comment", result.contains("/** Constructs a new object with café. */"));
    }

    /**
     * Test that DocFix works with explicit ISO-8859-1 encoding.
     */
    @Test
    public void testFixWithISO88591Encoding() throws IOException {
        Path tempFile = temporaryFolder.newFile("test.java").toPath();
        String javaCode = "/** Constructs a new object with ñoño and ü. */\npublic class Test {}";
        Files.writeString(tempFile, javaCode, StandardCharsets.ISO_8859_1);
        
        // Fix with explicit ISO-8859-1 encoding
        DocFix.fix(tempFile, StandardCharsets.ISO_8859_1);
        
        String result = Files.readString(tempFile, StandardCharsets.ISO_8859_1);
        assertTrue("Should fix Javadoc comment", result.contains("/** Constructs a new object with ñoño and ü. */"));
    }

    /**
     * Test that DocFix auto-detects encoding when none is specified.
     */
    @Test
    public void testFixWithAutoDetection() throws IOException {
        Path tempFile = temporaryFolder.newFile("test.java").toPath();
        String javaCode = "/** Constructs a new object with résumé. */\npublic class Test {}";
        Files.writeString(tempFile, javaCode, StandardCharsets.UTF_8);
        
        // Fix with auto-detection (null encoding)
        DocFix.fix(tempFile, null);
        
        String result = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertTrue("Should fix Javadoc comment", result.contains("/** Constructs a new object with résumé. */"));
    }

    /**
     * Test command line parsing with encoding flag.
     */
    @Test
    public void testMainWithEncodingFlag() throws IOException {
        Path tempFile = temporaryFolder.newFile("test.java").toPath();
        String javaCode = "/**\n * Constructs A new object with naïve approach.\n * @param value The value with émotions\n */\npublic class Test {}";
        Files.writeString(tempFile, javaCode, StandardCharsets.UTF_8);
        
        // Capture System.out to check for output
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));
        
        try {
            String[] args = { "--dryrun", "-encoding", "UTF-8", tempFile.toString() };
            DocFix.main(args);
        } finally {
            System.setOut(oldOut);
        }
        
        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue("Should show @param changes", output.contains("@param value the value"));
        assertTrue("Should preserve non-ASCII characters in @param", output.contains("the value with émotions"));
    }

    /**
     * Test that DocFix can traverse deep directory structures beyond the original 3-level limit.
     * This test verifies that the increased depth limit allows processing of realistically deep directories.
     */
    @Test
    public void testDeepDirectoryTraversalSupported() throws IOException {
        Path file1 = Files.createTempFile("ComplexNumber1", ".java");
        Path file2 = Files.createTempFile("ComplexNumber2", ".java");
        String original = Files.readString(Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java"), StandardCharsets.UTF_8);
        Files.writeString(file1, original, StandardCharsets.UTF_8);
        Files.writeString(file2, original, StandardCharsets.UTF_8);
        
        // Create a deep directory structure: level1/level2/level3/level4/level5/level6
        Path dir = Files.createTempDirectory("docfix_deep_test_dir");
        Path deepSubdirectory = Files.createDirectories(dir.resolve("level1/level2/level3/level4/level5/level6"));
        Files.move(file1, deepSubdirectory.resolve(file1.getFileName()));
        Files.move(file2, deepSubdirectory.resolve(file2.getFileName()));
        
        String[] args = { dir.toString() };
        DocFix.main(args);
        
        // Verify that files in the deep subdirectory were processed correctly
        for (Path file : Files.newDirectoryStream(deepSubdirectory, "*.java")) {
            String fixed = Files.readString(file, StandardCharsets.UTF_8);
            assertNotEquals("DocFix should modify the content", original, fixed);
            assertTrue("Should contain the fix", fixed.contains("     * @param real the real part"));
        }
    }
}
