package com.elharo.docfix;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Test the DocFix Maven plugin functionality.
 */
public class DocFixMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testMojoBasicExecution() throws Exception {
        // Create a temporary project structure
        File projectRoot = temporaryFolder.newFolder("test-project");
        File srcMainJava = new File(projectRoot, "src/main/java");
        File srcDir = new File(srcMainJava, "com/example");
        srcDir.mkdirs();

        // Create a test Java file with Javadoc issues
        File testFile = new File(srcDir, "TestClass.java");
        String originalContent = "package com.example;\n\n" +
            "/**\n" +
            " * test class\n" +
            " * @author Someone\n" +
            " */\n" +
            "public class TestClass {\n" +
            "    /**\n" +
            "     * some method\n" +
            "     * @param value The value\n" +
            "     * @return The result\n" +
            "     */\n" +
            "    public String test(String value) {\n" +
            "        return value;\n" +
            "    }\n" +
            "}\n";
        Files.writeString(testFile.toPath(), originalContent, StandardCharsets.UTF_8);

        // Create and configure the Mojo
        DocFixMojo mojo = new DocFixMojo();
        setField(mojo, "sourceDirectory", srcMainJava);
        setField(mojo, "encoding", "UTF-8");
        setField(mojo, "dryrun", false);
        setField(mojo, "skip", false);

        // Execute the mojo
        mojo.execute();

        // Verify the file was fixed
        String fixedContent = Files.readString(testFile.toPath(), StandardCharsets.UTF_8);
        assertTrue("File should be modified", !originalContent.equals(fixedContent));
        assertTrue("Class comment should be capitalized", fixedContent.contains("Test class."));
        assertTrue("@param should be lowercase", fixedContent.contains("@param value the value"));
        assertTrue("@return should be lowercase", fixedContent.contains("@return the result"));
        assertTrue("Method comment should have period", fixedContent.contains("Some method."));
    }

    @Test
    public void testMojoDryRun() throws Exception {
        // Create a temporary project structure
        File projectRoot = temporaryFolder.newFolder("test-project");
        File srcMainJava = new File(projectRoot, "src/main/java");
        File srcDir = new File(srcMainJava, "com/example");
        srcDir.mkdirs();

        // Create a test Java file with Javadoc issues
        File testFile = new File(srcDir, "TestClass.java");
        String originalContent = "package com.example;\n\n" +
            "/**\n" +
            " * test class\n" +
            " */\n" +
            "public class TestClass {\n" +
            "}\n";
        Files.writeString(testFile.toPath(), originalContent, StandardCharsets.UTF_8);

        // Create and configure the Mojo for dry run
        DocFixMojo mojo = new DocFixMojo();
        setField(mojo, "sourceDirectory", srcMainJava);
        setField(mojo, "encoding", "UTF-8");
        setField(mojo, "dryrun", true);
        setField(mojo, "skip", false);

        // Execute the mojo
        mojo.execute();

        // Verify the file was NOT modified in dry run mode
        String afterContent = Files.readString(testFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("File should not be modified in dry run mode", originalContent, afterContent);
    }

    @Test
    public void testMojoSkip() throws Exception {
        // Create a temporary project structure
        File projectRoot = temporaryFolder.newFolder("test-project");
        File srcMainJava = new File(projectRoot, "src/main/java");
        File srcDir = new File(srcMainJava, "com/example");
        srcDir.mkdirs();

        // Create a test Java file with Javadoc issues
        File testFile = new File(srcDir, "TestClass.java");
        String originalContent = "package com.example;\n\n" +
            "/**\n" +
            " * test class\n" +
            " */\n" +
            "public class TestClass {\n" +
            "}\n";
        Files.writeString(testFile.toPath(), originalContent, StandardCharsets.UTF_8);

        // Create and configure the Mojo with skip=true
        DocFixMojo mojo = new DocFixMojo();
        setField(mojo, "sourceDirectory", srcMainJava);
        setField(mojo, "encoding", "UTF-8");
        setField(mojo, "dryrun", false);
        setField(mojo, "skip", true);

        // Execute the mojo
        mojo.execute();

        // Verify the file was NOT modified when skip=true
        String afterContent = Files.readString(testFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("File should not be modified when skip=true", originalContent, afterContent);
    }

    /**
     * Helper method to set private fields using reflection.
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}