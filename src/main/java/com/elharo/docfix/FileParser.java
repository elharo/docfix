package com.elharo.docfix;

import static com.elharo.docfix.DocFix.detectLineEnding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Read Java files line by line, applying fixes to Javadoc comments.
 */
class FileParser {

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
  static List<String> parseFile(Path path) throws IOException {
    // TODO This is almost the same as DocFix.fix(Path file)
    // TODO handle encoding; might not be UTF-8
    String code = Files.readString(path, StandardCharsets.UTF_8);
    String lineEnding = detectLineEnding(code);
    String[] lines = code.split("\\R");
    return parseLines(lines, lineEnding);
  }

  static List<String> parseLines(String[] lines, String lineEnding) {
    return parseLines(List.of(lines), lineEnding);
  }

  static List<String> parseLines(List<String> lines, String lineEnding) {
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
          javadocBuilder.append(lineEnding);
          i++; // Move to next line

          // Read until we find the end of the Javadoc comment
          while (i < lines.size()) {
            String currentLine = lines.get(i);
            javadocBuilder.append(currentLine);

            // TODO same claude sonnet mistake
            if (currentLine.trim().endsWith("*/")) {
              break;
            }

            javadocBuilder.append(lineEnding);
            i++;
          }
        }

        String originalComment = javadocBuilder.toString();
        String fixedComment = DocComment.parse(null, originalComment).toJava();
        fixedComment = fixedComment.replace("\n", lineEnding);
        result.add(fixedComment);
      } else {
        // Regular line, add as-is
        result.add(line);
      }
    }

    return result;
  }
}