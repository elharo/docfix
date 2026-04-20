package com.elharo.docfix.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Functional tests for the DocFix Gradle plugin.
 */
public class DocFixPluginFunctionalTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private File buildFile;
    private File settingsFile;
    private File javaFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
        settingsFile = testProjectDir.newFile("settings.gradle");
        
        // Create source directory
        File srcDir = new File(testProjectDir.getRoot(), "src/main/java/com/example");
        srcDir.mkdirs();
        javaFile = new File(srcDir, "Example.java");
        
        // Write settings file
        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write("rootProject.name = 'test-project'\n");
        }
    }

    @Test
    public void testPluginApplies() throws IOException {
        // Write build file
        try (FileWriter writer = new FileWriter(buildFile)) {
            writer.write("plugins {\n");
            writer.write("    id 'java'\n");
            writer.write("    id 'com.elharo.docfix'\n");
            writer.write("}\n");
        }

        // Write Java file with Javadoc issues
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write("package com.example;\n\n");
            writer.write("/**\n");
            writer.write(" * example class\n");
            writer.write(" */\n");
            writer.write("public class Example {\n");
            writer.write("    /**\n");
            writer.write("     * @param value The value\n");
            writer.write("     */\n");
            writer.write("    public void setValue(int value) {}\n");
            writer.write("}\n");
        }

        // Run the plugin
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("docfix")
                .withPluginClasspath()
                .build();

        assertEquals(SUCCESS, result.task(":docfix").getOutcome());
        
        // Read the fixed file
        String fixedContent = new String(Files.readAllBytes(javaFile.toPath()));
        
        // Verify fixes were applied
        assertTrue("First letter should be uppercase", fixedContent.contains("Example class"));
        assertTrue("@param description should be lowercase", fixedContent.contains("@param value the value"));
    }

    @Test
    public void testDryRun() throws IOException {
        // Write build file
        try (FileWriter writer = new FileWriter(buildFile)) {
            writer.write("plugins {\n");
            writer.write("    id 'java'\n");
            writer.write("    id 'com.elharo.docfix'\n");
            writer.write("}\n");
            writer.write("\n");
            writer.write("docfix {\n");
            writer.write("    dryrun = true\n");
            writer.write("}\n");
        }

        // Write Java file with Javadoc issues
        String originalContent = "package com.example;\n\n" +
                "/**\n" +
                " * example class\n" +
                " */\n" +
                "public class Example {\n" +
                "}\n";
        
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(originalContent);
        }

        // Run the plugin in dry-run mode
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("docfix")
                .withPluginClasspath()
                .build();

        assertEquals(SUCCESS, result.task(":docfix").getOutcome());
        
        // Read the file - it should be unchanged
        String content = new String(Files.readAllBytes(javaFile.toPath()));
        assertEquals("File should not be modified in dry-run mode", originalContent, content);
        assertTrue("Output should mention dry-run", result.getOutput().contains("dry-run mode"));
    }
}
