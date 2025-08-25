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
}
