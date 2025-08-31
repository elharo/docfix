package com.elharo.docfix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for detecting character encoding of Java source files.
 * Uses heuristics specific to Java files to improve detection accuracy.
 */
final class EncodingDetector {

  private EncodingDetector() {
    // Utility class
  }

  /**
   * Detects the character encoding of a Java source file.
   * Applies heuristics specific to Java files and defaults to UTF-8.
   *
   * @param file the Java source file to analyze
   * @return the detected charset, or UTF-8 if detection fails
   * @throws IOException if an I/O error occurs reading the file
   */
  static Charset detectEncoding(Path file) throws IOException {
    // Read first few bytes to check for BOM and initial content
    byte[] buffer = new byte[4096];
    int bytesRead;
    
    try (InputStream in = Files.newInputStream(file)) {
      bytesRead = in.read(buffer);
    }
    
    if (bytesRead == -1) {
      // Empty file, default to UTF-8
      return StandardCharsets.UTF_8;
    }
    
    // Check for BOM (Byte Order Mark)
    Charset bomCharset = detectBOM(buffer, bytesRead);
    if (bomCharset != null) {
      return bomCharset;
    }
    
    // Apply Java-specific heuristics
    return detectJavaEncoding(buffer, bytesRead);
  }

  /**
   * Detects encoding based on Byte Order Mark (BOM).
   *
   * @param buffer the byte buffer to analyze
   * @param length the number of valid bytes in the buffer
   * @return the charset indicated by BOM, or null if no BOM found
   */
  private static Charset detectBOM(byte[] buffer, int length) {
    if (length >= 3 && 
        (buffer[0] & 0xFF) == 0xEF && 
        (buffer[1] & 0xFF) == 0xBB && 
        (buffer[2] & 0xFF) == 0xBF) {
      return StandardCharsets.UTF_8;
    }
    
    if (length >= 2 && 
        (buffer[0] & 0xFF) == 0xFF && 
        (buffer[1] & 0xFF) == 0xFE) {
      return StandardCharsets.UTF_16LE;
    }
    
    if (length >= 2 && 
        (buffer[0] & 0xFF) == 0xFE && 
        (buffer[1] & 0xFF) == 0xFF) {
      return StandardCharsets.UTF_16BE;
    }
    
    return null;
  }

  /**
   * Applies Java-specific heuristics to detect encoding.
   * Java files must contain valid ASCII keywords and identifiers.
   *
   * @param buffer the byte buffer to analyze
   * @param length the number of valid bytes in the buffer
   * @return the most likely charset for this Java file
   */
  private static Charset detectJavaEncoding(byte[] buffer, int length) {
    // Try UTF-8 first as it's the most common modern encoding
    if (isValidUTF8(buffer, length) && containsJavaKeywords(buffer, length, StandardCharsets.UTF_8)) {
      return StandardCharsets.UTF_8;
    }
    
    // Try ISO-8859-1 (Latin-1) as fallback for older files
    if (containsJavaKeywords(buffer, length, StandardCharsets.ISO_8859_1)) {
      return StandardCharsets.ISO_8859_1;
    }
    
    // Default to UTF-8 if nothing else works
    return StandardCharsets.UTF_8;
  }

  /**
   * Checks if the byte sequence is valid UTF-8.
   *
   * @param buffer the byte buffer to check
   * @param length the number of valid bytes in the buffer
   * @return true if the sequence is valid UTF-8
   */
  private static boolean isValidUTF8(byte[] buffer, int length) {
    int i = 0;
    while (i < length) {
      byte b = buffer[i];
      
      // ASCII character (0xxxxxxx)
      if ((b & 0x80) == 0) {
        i++;
        continue;
      }
      
      // Multi-byte UTF-8 sequence
      int extraBytes;
      if ((b & 0xE0) == 0xC0) {
        extraBytes = 1; // 110xxxxx
      } else if ((b & 0xF0) == 0xE0) {
        extraBytes = 2; // 1110xxxx
      } else if ((b & 0xF8) == 0xF0) {
        extraBytes = 3; // 11110xxx
      } else {
        return false; // Invalid UTF-8 start byte
      }
      
      // Check continuation bytes
      for (int j = 1; j <= extraBytes; j++) {
        if (i + j >= length) {
          return true; // Incomplete sequence at end of buffer, assume valid
        }
        if ((buffer[i + j] & 0xC0) != 0x80) {
          return false; // Invalid continuation byte
        }
      }
      
      i += extraBytes + 1;
    }
    
    return true;
  }

  /**
   * Checks if the buffer contains common Java keywords when decoded with the given charset.
   *
   * @param buffer the byte buffer to check
   * @param length the number of valid bytes in the buffer
   * @param charset the charset to use for decoding
   * @return true if Java keywords are found
   */
  private static boolean containsJavaKeywords(byte[] buffer, int length, Charset charset) {
    String content = new String(buffer, 0, length, charset);

    // TODO: replace with the AhoCorasick algorithm here
    // By using AhoCorasick with byte strings instead of strings,
    // we could check for multiple encodings in a single pass
    // Look for common Java keywords and patterns. package and import appear
    // very early in most files and allow for early exit. Every valid Java
    // file must contains one of class, interface, or enum and these also
    // appear reasonably early in the file.
    return content.contains("package") ||
           content.contains("import") ||
           content.contains("class") ||
           content.contains("interface") ||
           content.contains("record") ||
           content.contains("enum");
  }
}