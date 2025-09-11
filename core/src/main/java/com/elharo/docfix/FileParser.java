package com.elharo.docfix;

import java.util.ArrayList;
import java.util.List;

/**
 * Read Java files line by line, applying fixes to Javadoc comments.
 */
final class FileParser {
  
  private FileParser() {}

  static List<String> parseLines(String[] lines, String lineEnding) {
    List<String> lines1 = List.of(lines);
    List<String> result = new ArrayList<>();

    for (int i = 0; i < lines1.size(); i++) {
      String line = lines1.get(i);
      String trimmed = line.stripLeading();

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
          while (i < lines1.size()) {
            String currentLine = lines1.get(i);
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
        // Only add the fixed comment if it's not empty (empty comments should be completely removed)
        if (!fixedComment.isEmpty()) {
          result.add(fixedComment);
        }
      } else {
        // Regular line, add as-is
        result.add(line);
      }
    }

    return result;
  }

}