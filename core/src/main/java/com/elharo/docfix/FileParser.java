package com.elharo.docfix;

import java.util.ArrayList;
import java.util.List;

/**
 * Read Java files line by line, applying fixes to Javadoc comments.
 */
final class FileParser {
  
  private FileParser() {}

  /**
   * Checks if the position in the line is inside a string literal.
   * This is a simple heuristic that counts quotes before the position.
   */
  private static boolean isInsideStringLiteral(String line, int position) {
    int quoteCount = 0;
    boolean escaped = false;
    for (int i = 0; i < position && i < line.length(); i++) {
      char c = line.charAt(i);
      if (escaped) {
        escaped = false;
        continue;
      }
      if (c == '\\') {
        escaped = true;
        continue;
      }
      if (c == '"') {
        quoteCount++;
      }
    }
    // If odd number of quotes, we're inside a string
    return quoteCount % 2 == 1;
  }

  /**
   * Checks if the position in the line is inside a single-line comment (// ...).
   */
  private static boolean isInsideSingleLineComment(String line, int position) {
    // Look for "//" before the position
    int slashSlashIndex = line.indexOf("//");
    return slashSlashIndex >= 0 && slashSlashIndex < position;
  }

  static List<String> parseLines(String[] lines, String lineEnding) {
    List<String> lines1 = List.of(lines);
    List<String> result = new ArrayList<>();

    for (int i = 0; i < lines1.size(); i++) {
      String line = lines1.get(i);
      String trimmed = line.stripLeading();

      // Check if this line starts a Javadoc comment (original logic)
      boolean handledByFirstBranch = false;
      if (trimmed.startsWith("/**")) {
        // Check if this is a single-line comment with potential code after it
        int commentEnd = trimmed.indexOf("*/");
        if (commentEnd >= 0 && commentEnd + 2 < trimmed.length()) {
          // Single-line comment with code after it - use the more general handler below
          handledByFirstBranch = false;
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
          handledByFirstBranch = true;
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
          handledByFirstBranch = true;
          continue; // Skip to next line
        }
      }
      
      // Handle lines with comments not at the beginning, or lines starting with single-line comments followed by code
      if (!handledByFirstBranch) {
        // Look for Javadoc comments on the line
        List<String> extractedComments = new ArrayList<>();
        int searchStart = 0;
        String workingLine = line;
        boolean foundComment = false;
        String leadingWhitespace = line.substring(0, line.length() - line.stripLeading().length());
        boolean hasCodeOnLine = false; // Track if there's any non-comment code
        
        // First pass: detect all comments and check if there's code mixed with them
        List<int[]> commentRanges = new ArrayList<>();
        int tempSearch = 0;
        while (tempSearch < workingLine.length()) {
          int javadocStart = workingLine.indexOf("/**", tempSearch);
          if (javadocStart < 0) break;
          // Skip if inside a string literal or single-line comment
          if (isInsideStringLiteral(workingLine, javadocStart) || isInsideSingleLineComment(workingLine, javadocStart)) {
            tempSearch = javadocStart + 1;
            continue;
          }
          int javadocEnd = workingLine.indexOf("*/", javadocStart);
          if (javadocEnd < 0) break; // Incomplete comment
          commentRanges.add(new int[]{javadocStart, javadocEnd + 2});
          tempSearch = javadocEnd + 2;
        }
        
        // Check if there's any code on the line (non-whitespace, non-comment content)
        if (!commentRanges.isEmpty()) {
          StringBuilder nonCommentContent = new StringBuilder();
          int lastEnd = 0;
          for (int[] range : commentRanges) {
            nonCommentContent.append(workingLine.substring(lastEnd, range[0]));
            lastEnd = range[1];
          }
          nonCommentContent.append(workingLine.substring(lastEnd));
          hasCodeOnLine = !nonCommentContent.toString().trim().isEmpty();
        }
        
        // Second pass: extract and fix comments
        while (searchStart < workingLine.length()) {
          int javadocStart = workingLine.indexOf("/**", searchStart);
          if (javadocStart < 0) {
            break; // No more comments on this line
          }
          
          // Skip if inside a string literal or single-line comment
          if (isInsideStringLiteral(workingLine, javadocStart) || isInsideSingleLineComment(workingLine, javadocStart)) {
            searchStart = javadocStart + 1;
            continue;
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
            
            // Extract comment to its own line if there's code mixed with comments on this line
            if (!fixedComment.isEmpty() && hasCodeOnLine) {
              // Move comment to its own line before the code
              extractedComments.add(leadingWhitespace + fixedComment);
              // Remove the comment from the working line, keeping the code
              String trimmedBefore = codeBefore.stripTrailing();
              String trimmedAfter = trimmedBefore.isEmpty() ? codeAfter.stripLeading() : codeAfter;
              workingLine = trimmedBefore + trimmedAfter;
              searchStart = trimmedBefore.length();
            } else {
              // No code on line, or comment is empty - keep it inline
              // Reconstruct this portion of the line
              StringBuilder reconstructed = new StringBuilder();
              reconstructed.append(codeBefore);
              if (!fixedComment.isEmpty()) {
                reconstructed.append(fixedComment);
              }
              
              // Update working line to continue processing from after this comment
              workingLine = reconstructed.toString() + codeAfter;
              searchStart = reconstructed.length();
            }
          } else {
            // Multi-line comment starting after code - this is a complex edge case
            // For now, just add the line as-is (this can be enhanced later if needed)
            result.add(line);
            foundComment = false;
            break;
          }
        }
        
        if (foundComment) {
          // Add extracted comments first (on their own lines)
          for (String comment : extractedComments) {
            result.add(comment);
          }
          // Then add the line with code (if any code remains)
          if (!workingLine.trim().isEmpty()) {
            result.add(workingLine);
          }
        } else {
          // Regular line, add as-is
          result.add(line);
        }
      }
    }

    return result;
  }

}