package com.elharo.docfix;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing Java files line by line, with special handling
 * for Javadoc comments.
 */
public class FileParser {

    /**
     * Reads a Java file line by line and returns a list of strings.
     * If a line starts a Javadoc comment, reads all lines until the end
     * of the Javadoc comment, fixes the comment, and stores that as a single string.
     * Otherwise, stores each individual line as a separate string.
     *
     * @param path the path to the Java file to read
     * @return a list of strings representing the file content
     * @throws IOException if an I/O error occurs reading the file
     */
    public static List<String> parseFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        return parseLines(lines);
    }

    public static List<String> parseLines(List<String> lines) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // Check if this line starts a Javadoc comment
            if (trimmed.startsWith("/**")) {
                StringBuilder javadocBuilder = new StringBuilder();
                javadocBuilder.append(line);

                // If the comment doesn't end on the same line, continue reading
                // TODO this is wrong. */ does not have to be last on the line though it usually is.
                // This is a claude mistake.
                if (!trimmed.endsWith("*/")) {
                    javadocBuilder.append("\n");
                    i++; // Move to next line

                    // Read until we find the end of the Javadoc comment
                    while (i < lines.size()) {
                        String currentLine = lines.get(i);
                        javadocBuilder.append(currentLine);

                        // TODO same mistake
                        if (currentLine.trim().endsWith("*/")) {
                            break;
                        }

                        javadocBuilder.append("\n");
                        i++;
                    }
                }

                String originalComment = javadocBuilder.toString();
                String fixedComment = DocComment.parse(null, originalComment).toJava();
                result.add(fixedComment);
            } else {
                // Regular line, add as-is
                result.add(line);
            }
        }

        return result;
    }
}