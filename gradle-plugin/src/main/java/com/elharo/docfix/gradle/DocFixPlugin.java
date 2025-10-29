package com.elharo.docfix.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

import java.io.File;

/**
 * Gradle plugin that fixes Javadoc comments to conform to Oracle Javadoc guidelines.
 * <p>
 * This plugin registers a {@code docfix} task that processes Java source files
 * in the main source set by default.
 * </p>
 */
public class DocFixPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // Register the docfix task
        project.getTasks().register("docfix", DocFixTask.class, task -> {
            // Try to get the main source set directory if Java plugin is applied
            project.getPlugins().withId("java", plugin -> {
                JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
                SourceSet mainSourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                
                // Get the first Java source directory (typically src/main/java)
                for (File srcDir : mainSourceSet.getJava().getSrcDirs()) {
                    if (srcDir.exists()) {
                        task.getSourceDirectory().set(srcDir);
                        break;
                    }
                }
            });
            
            // If Java plugin is not applied or source directory not set, use conventional location
            if (!task.getSourceDirectory().isPresent()) {
                task.getSourceDirectory().set(new File(project.getProjectDir(), "src/main/java"));
            }
        });
    }
}
