package com.elharo.docfix;

final class Strings {

  // Private constructor to prevent instantiation
  private Strings() {}
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
