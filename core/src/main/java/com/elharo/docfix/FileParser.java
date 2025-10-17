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

      // Check if this line starts a Javadoc comment (original logic)
      if (trimmed.startsWith("/**")) {
        // Check if this is a single-line comment with potential code after it
        int commentEnd = trimmed.indexOf("*/");
        if (commentEnd >= 0 && commentEnd + 2 < trimmed.length()) {
          // Single-line comment with code after it - use the more general handler below
          // Fall through to the else block by not entering this if
        } else if (!trimmed.endsWith("*/")) {
          // Multi-line comment starting at the beginning of a line
          StringBuilder javadocBuilder = new StringBuilder();
          javadocBuilder.append(line);
          javadocBuilder.append(lineEnding);
          i++; // Move to next line

          // Read until we find the end of the Javadoc comment
          while (i < lines1.size()) {
            String currentLine = lines1.get(i);
            javadocBuilder.append(currentLine);

            if (currentLine.trim().endsWith("*/")) {
              break;
            }

            javadocBuilder.append(lineEnding);
            i++;
          }

          String originalComment = javadocBuilder.toString();
          String fixedComment = DocComment.parse(null, originalComment).toJava();
          fixedComment = fixedComment.replace("\n", lineEnding);
          // Only add the fixed comment if it's not empty (empty comments should be completely removed)
          if (!fixedComment.isEmpty()) {
            result.add(fixedComment);
          }
          continue; // Skip to next line
        } else {
          // Single-line comment at the beginning of line with nothing after
          String originalComment = line;
          String fixedComment = DocComment.parse(null, originalComment).toJava();
          fixedComment = fixedComment.replace("\n", lineEnding);
          // Only add the fixed comment if it's not empty (empty comments should be completely removed)
          if (!fixedComment.isEmpty()) {
            result.add(fixedComment);
          }
          continue; // Skip to next line
        }
      }
      
      // Handle lines with comments not at the beginning, or lines starting with single-line comments followed by code
      {
        // Look for Javadoc comments elsewhere on the line
        int searchStart = 0;
        String workingLine = line;
        boolean foundComment = false;
        
        while (searchStart < workingLine.length()) {
          int javadocStart = workingLine.indexOf("/**", searchStart);
          if (javadocStart < 0) {
            break; // No more comments on this line
          }
          
          foundComment = true;
          
          // Extract code before the comment
          String codeBefore = workingLine.substring(0, javadocStart);
          
          // Check if comment ends on the same line
          int javadocEnd = workingLine.indexOf("*/", javadocStart);
          if (javadocEnd >= 0) {
            // Single-line comment on same line as code
            String comment = workingLine.substring(javadocStart, javadocEnd + 2);
            String codeAfter = workingLine.substring(javadocEnd + 2);
            
            String fixedComment = DocComment.parse(null, comment).toJava();
            fixedComment = fixedComment.replace("\n", lineEnding);
            
            // Reconstruct this portion of the line
            StringBuilder reconstructed = new StringBuilder();
            reconstructed.append(codeBefore);
            if (!fixedComment.isEmpty()) {
              reconstructed.append(fixedComment);
            }
            
            // Update working line to continue processing from after this comment
            workingLine = reconstructed.toString() + codeAfter;
            searchStart = reconstructed.length();
          } else {
            // Multi-line comment starting after code - this is a complex edge case
            // For now, just add the line as-is (this can be enhanced later if needed)
            result.add(line);
            foundComment = false;
            break;
          }
        }
        
        if (foundComment) {
          // We processed comments on this line
          result.add(workingLine);
        } else {
          // Regular line, add as-is
          result.add(line);
        }
      }
    }

    return result;
  }

}