package com.elharo.docfix;

import java.util.List;

/**
 * Represents a Javadoc comment, including its kind, description, and block tags.
 */
class DocComment {

  enum Kind { CLASS, METHOD, FIELD }
    final Kind kind;
    final String description; // Main description (before block tags)
    final List<BlockTag> blockTags;

  DocComment(Kind kind, String description, List<BlockTag> blockTags) {
    this.kind = kind;
    this.description = description;
    this.blockTags = blockTags;
  }

  public static DocComment parse(String text) {
    // Remove opening /** and closing */
    String trimmed = text.trim();
    if (trimmed.startsWith("/**")) {
      trimmed = trimmed.substring(3);
    }
    if (trimmed.endsWith("*/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 2);
    }
    // Remove leading * and whitespace from each line
    String[] lines = trimmed.split("\r?\n");
    StringBuilder desc = new StringBuilder();
    for (String rawLine : lines) {
      String strippedLine = rawLine.stripLeading();
      if (strippedLine.startsWith("*")) {
        strippedLine = strippedLine.substring(1);
        strippedLine = strippedLine.stripLeading();
      }
      desc.append(strippedLine);
      desc.append(" ");
    }
    String description = desc.toString().trim();
    return new DocComment(null, description, List.of());
  }

  public String getDescription() {
    return description;
  }
}
