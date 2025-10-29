package com.elharo.docfix.gradle;

import com.elharo.docfix.DocFix;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Gradle task that fixes Javadoc comments in Java source files.
 */
public abstract class DocFixTask extends DefaultTask {

    /**
     * The source directory to process. Defaults to src/main/java.
     */
    @InputDirectory
    public abstract DirectoryProperty getSourceDirectory();

    /**
     * Character encoding to use when reading and writing files.
     * Defaults to UTF-8.
     */
    @Input
    @Optional
    public abstract Property<String> getEncoding();

    /**
     * Dry run mode - show what would be changed without modifying files.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDryrun();

    public DocFixTask() {
        setGroup("documentation");
        setDescription("Fixes Javadoc comments to conform to Oracle Javadoc guidelines");
        getEncoding().convention("UTF-8");
        getDryrun().convention(false);
    }

    @TaskAction
    public void fixJavadoc() throws IOException {
        Path sourceDir = getSourceDirectory().get().getAsFile().toPath();
        
        if (!sourceDir.toFile().exists()) {
            getLogger().warn("Source directory does not exist: " + sourceDir);
            return;
        }

        if (!sourceDir.toFile().isDirectory()) {
            getLogger().warn("Source directory is not a directory: " + sourceDir);
            return;
        }

        try {
            Charset charset = Charset.forName(getEncoding().get());
            boolean dryrun = getDryrun().get();
            
            if (dryrun) {
                getLogger().lifecycle("Running in dry-run mode. No files will be modified.");
            }
            
            DocFix.fixDirectory(sourceDir, dryrun, charset);
            
            if (!dryrun) {
                getLogger().lifecycle("DocFix completed successfully");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid encoding: " + getEncoding().get(), e);
        }
    }
}
