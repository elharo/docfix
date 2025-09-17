package com.elharo.docfix;

final class Strings {

  // Private constructor to prevent instantiation
  private Strings() {}
  
  /**
   * Finds the number of spaces at the beginning of a string,
   * counting tabs as 4 spaces each
   *
   * @param s the string to analyze
   * @return the indentation in spaces
   */
  static int findIndent(String s) {
    int indent = 0;
    for (char c : s.toCharArray()) {
      if (c == ' ') {
        indent++;
      } else if (c == '\t') {
        indent += 4; // Assuming tab is equivalent to 4 spaces
      } else {
        break; // Stop at first non-space character
      }
    }
    return indent;
  }

  /**
   * Checks if a string ends with a URL.
   * This method identifies common URL patterns to avoid adding periods after URLs.
   *
   * @param text the text to check
   * @return true if the text appears to end with a URL
   */
  static boolean endsWithURL(String text) {
    if (text == null || text.trim().isEmpty()) {
      return false;
    }
    
    String trimmed = text.trim();
    
    // Find the last word/token that might be a URL
    String[] words = trimmed.split("\\s+");
    if (words.length == 0) {
      return false;
    }
    
    String lastWord = words[words.length - 1];
    
    // Check if the last word contains explicit URL schemes
    String[] urlSchemes = {
      "http://", "https://", "ftp://", "ftps://", "file://", "mailto:"
    };
    
    for (String scheme : urlSchemes) {
      if (lastWord.contains(scheme)) {
        return true;
      }
    }
    
    // Check for common web domain patterns
    if (lastWord.startsWith("www.") || lastWord.startsWith("ftp.")) {
      return true;
    }
    
    // Check if it looks like a domain name (contains dots and common TLDs)
    if (lastWord.contains(".") && isLikelyDomain(lastWord)) {
      return true;
    }
    
    return false;
  }
  
  /**
   * Checks if a string looks like a domain name by checking for common TLD patterns.
   *
   * @param word the word to check
   * @return true if it looks like a domain name
   */
  private static boolean isLikelyDomain(String word) {
    if (word == null || !word.contains(".")) {
      return false;
    }
    
    // Common top-level domains
    String[] commonTlds = {
      ".com", ".org", ".net", ".edu", ".gov", ".mil", ".info", ".biz",
      ".co", ".io", ".ly", ".me", ".tv", ".cc", ".ws", ".in", ".uk",
      ".de", ".fr", ".jp", ".cn", ".au", ".ca", ".ru", ".br", ".it",
      ".nl", ".es", ".se", ".no", ".dk", ".fi", ".pl", ".ch", ".be",
      ".cz", ".at", ".hu", ".sk", ".si", ".hr", ".bg", ".ro", ".gr",
      ".pt", ".ie", ".lv", ".lt", ".ee", ".is", ".mt", ".cy", ".lu"
    };
    
    String lowerWord = word.toLowerCase();
    for (String tld : commonTlds) {
      if (lowerWord.endsWith(tld) || lowerWord.endsWith(tld + "/")) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Detects the line ending used in the provided code.
   * It checks for Windows (\r\n), Mac (\r), and Unix (\n) line endings.
   *
   * @param code the source code to analyze
   * @return the detected line ending as a string
   */
  static String detectLineEnding(String code) {
    String lineEnding = "\n";
    if (code.contains("\r\n")) {
      lineEnding = "\r\n";
    } else if (code.contains("\r")) {
      lineEnding = "\r";
    }
    return lineEnding;
  }
}
