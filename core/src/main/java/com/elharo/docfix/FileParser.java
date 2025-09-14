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
      StringBuilder content = new StringBuilder();
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        content.append(line).append("\n");
      }
      
      if (content.length() == 0) {
        return new ArrayList<>();
      }
      
      // Remove the last newline that we added
      if (content.length() > 0 && content.charAt(content.length() - 1) == '\n') {
        content.setLength(content.length() - 1);
      }
      
      String sourceText = content.toString();
      List<String> chunks = new ArrayList<>();
      
      int index = 0;
      while (index < sourceText.length()) {
        int javadocStart = sourceText.indexOf("/**", index);
        
        if (javadocStart == -1) {
          // No more Javadoc comments - add rest as one chunk
          String remaining = sourceText.substring(index);
          if (!remaining.isEmpty()) {
            addNonJavadocChunk(remaining, chunks);
          }
          break;
        }
        
        // Add any content before the Javadoc as chunk(s)
        if (javadocStart > index) {
          String beforeJavadoc = sourceText.substring(index, javadocStart);
          addNonJavadocChunk(beforeJavadoc, chunks);
        }
        
        // Find end of Javadoc comment
        int javadocEnd = sourceText.indexOf("*/", javadocStart);
        if (javadocEnd == -1) {
          throw new JavaParseException("Unclosed Javadoc comment starting at position " + javadocStart);
        }
        
        // Add Javadoc as chunk
        String javadoc = sourceText.substring(javadocStart, javadocEnd + 2);
        chunks.add(javadoc);
        
        index = javadocEnd + 2;
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
   * Adds a non-Javadoc chunk, applying line terminator rules and splitting if needed.
   */
  private static void addNonJavadocChunk(String chunk, List<String> chunks) {
    String processed = removeOneLeadingTrailingNewline(chunk);
    
    if (processed.isEmpty()) {
      return; // Don't add empty chunks
    }
    
    // Special case: if the processed chunk contains "\n\n", split on it
    int doubleNewline = processed.indexOf("\n\n");
    if (doubleNewline >= 0) {
      String before = processed.substring(0, doubleNewline);
      String after = processed.substring(doubleNewline + 2);
      
      if (!before.isEmpty()) {
        chunks.add(before);
      }
      chunks.add(""); // The empty line becomes an empty chunk
      
      if (!after.isEmpty()) {
        addNonJavadocChunk(after, chunks); // Recursively process the rest
      }
    }
    // If the processed chunk ends with "\n", split it
    else if (processed.endsWith("\n")) {
      String content = processed.substring(0, processed.length() - 1);
      if (!content.isEmpty()) {
        chunks.add(content);
      }
      chunks.add("");
    }
    else {
      chunks.add(processed);
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