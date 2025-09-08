package com.elharo.docfix;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Maven plugin goal that fixes Javadoc comments in Java source files.
 */
@Mojo(name = "fix", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DocFixMojo extends AbstractMojo {

    /**
     * The Maven project instance.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The source directory to process. Defaults to src/main/java.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/java")
    private File sourceDirectory;

    /**
     * Character encoding to use when reading and writing files.
     * Defaults to UTF-8.
     */
    @Parameter(defaultValue = "UTF-8")
    private String encoding;

    /**
     * Dry run mode - show what would be changed without modifying files.
     */
    @Parameter(property = "docfix.dryrun", defaultValue = "false")
    private boolean dryrun;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!sourceDirectory.exists()) {
            getLog().warn("Source directory does not exist: " + sourceDirectory);
            return;
        }

        if (!sourceDirectory.isDirectory()) {
            getLog().warn("Source directory is not a directory: " + sourceDirectory);
            return;
        }

        try {
            Charset charset = Charset.forName(encoding);
            processDirectory(sourceDirectory.toPath(), charset);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to fix Javadoc comments", e);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Invalid encoding: " + encoding, e);
        }
    }

    private void processDirectory(Path directory, Charset charset) throws IOException {
        Files.walk(directory, 3)
            .filter(p -> !Files.isSymbolicLink(p))
            .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
            .forEach(p -> {
                try {
                    if (dryrun) {
                        processDryRun(p, charset);
                    } else {
                        processFile(p, charset);
                    }
                } catch (IOException e) {
                    getLog().error("Failed to process file: " + p, e);
                }
            });
    }

    private void processDryRun(Path file, Charset charset) throws IOException {
        String original = Files.readString(file, charset);
        String fixed = DocFix.fix(original);
        if (!original.equals(fixed)) {
            Path basePath = sourceDirectory.toPath();
            Path relativePath = basePath.relativize(file);
            getLog().info("Would fix: " + relativePath);
        }
    }

    private void processFile(Path file, Charset charset) throws IOException {
        String original = Files.readString(file, charset);
        String fixed = DocFix.fix(original);
        if (!original.equals(fixed)) {
            Path basePath = sourceDirectory.toPath();
            Path relativePath = basePath.relativize(file);
            getLog().debug("Fixing: " + relativePath);
            Files.writeString(file, fixed, charset);
        }
    }
}