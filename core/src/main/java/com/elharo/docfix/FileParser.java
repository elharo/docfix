package com.elharo.docfix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
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

  /**
   * Extracts chunks from a Java source file.
   * Each Javadoc comment becomes one chunk, and runs of non-Javadoc text become separate chunks.
   * Order is maintained. Leading and trailing line terminators are removed from non-Javadoc chunks,
   * but no more than one at each end.
   *
   * @param reader the reader containing Java source code
   * @return a list of chunks where each Javadoc comment is one string and other text runs are separate strings
   * @throws IOException if an I/O error occurs reading from the reader
   * @throws JavaParseException if there is an error parsing the Java source
   */
  public static List<String> extractChunks(Reader reader) throws IOException, JavaParseException {
    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
      List<String> lines = new ArrayList<>();
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        lines.add(line);
      }
      
      if (lines.isEmpty()) {
        return new ArrayList<>();
      }
      
      List<String> chunks = new ArrayList<>();
      int i = 0;
      
      while (i < lines.size()) {
        String currentLine = lines.get(i);
        String trimmed = currentLine.stripLeading();
        
        if (trimmed.startsWith("/**")) {
          // Found start of Javadoc comment
          StringBuilder javadocBuilder = new StringBuilder();
          javadocBuilder.append(currentLine);
          
          if (trimmed.endsWith("*/")) {
            // Single line Javadoc
            chunks.add(javadocBuilder.toString());
            i++;
          } else {
            // Multi-line Javadoc
            i++;
            while (i < lines.size()) {
              String javadocLine = lines.get(i);
              javadocBuilder.append("\n").append(javadocLine);
              
              if (javadocLine.trim().endsWith("*/")) {
                chunks.add(javadocBuilder.toString());
                i++;
                break;
              }
              i++;
            }
          }
        } else {
          // Non-Javadoc content
          StringBuilder contentBuilder = new StringBuilder();
          
          // Keep collecting lines until we hit a Javadoc or end of file
          while (i < lines.size() && !lines.get(i).stripLeading().startsWith("/**")) {
            if (contentBuilder.length() > 0) {
              contentBuilder.append("\n");
            }
            contentBuilder.append(lines.get(i));
            i++;
          }
          
          if (contentBuilder.length() > 0) {
            String content = removeOneLeadingTrailingNewline(contentBuilder.toString());
            chunks.add(content);
          }
        }
      }
      
      return chunks;
    } catch (Exception e) {
      if (e instanceof IOException) {
        throw e;
      }
      if (e instanceof JavaParseException) {
        throw e;
      }
      throw new JavaParseException("Error parsing Java source", e);
    }
  }
  
  /**
   * Removes at most one leading and one trailing newline from a string.
   */
  private static String removeOneLeadingTrailingNewline(String text) {
    if (text.isEmpty()) {
      return text;
    }
    
    String result = text;
    
    // Remove one leading newline
    if (result.startsWith("\n")) {
      result = result.substring(1);
    }
    
    // Remove one trailing newline  
    if (result.endsWith("\n")) {
      result = result.substring(0, result.length() - 1);
    }
    
    return result;
  }

}