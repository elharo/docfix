package com.elharo.docfix;

import java.util.List;

/**
 * Represents a Javadoc comment, including its kind, description, and block tags.
 */
class DocComment {

  enum Kind {CLASS, METHOD, FIELD}

  final Kind kind;
  final String description; // Main description (before block tags)
  final List<BlockTag> blockTags;

  // Indentation to be applied before entire comment
  private final String indent;

  private DocComment(Kind kind, String description, List<BlockTag> blockTags, int indent) {
    this.kind = kind;
    if (description != null && !description.isEmpty()) {
      char first = description.charAt(0);
      this.description = Character.toString(first).toUpperCase(java.util.Locale.ENGLISH) + description.substring(1);
    } else {
      this.description = description;
    }
    this.blockTags = blockTags;
    this.indent = " ".repeat(indent);
  }

  static DocComment parse(Kind kind, String raw) {
    int tagIndent = findIndent(raw);

    // Remove leading/trailing comment markers and split into lines
    // TODO could just keep the beginning and end markers
    String body = raw.trim();
    if (body.startsWith("/**")) {
      body = body.substring(3);
    }
    if (body.endsWith("*/")) {
      body = body.substring(0, body.length() - 2);
    }
    String[] lines = body.split("\r?\n");
    StringBuilder descBuilder = new StringBuilder();
    List<BlockTag> blockTags = new java.util.ArrayList<>();
    boolean inBlockTags = false;
    for (String line : lines) {
      String trimmed = line.trim();
      // TODO might need to remove global indent
      int indent = line.length() - trimmed.length();
      if (trimmed.startsWith("*")) {
        trimmed = trimmed.substring(1).trim();
      }
      if (trimmed.isEmpty()) {
        continue;
      }
      if (trimmed.startsWith("@")) {
        inBlockTags = true;
        // Parse block tag: e.g. @param real The real part
        String[] parts = trimmed.split(" ", 3);
        String type = parts[0].substring(1); // remove '@'
        String arg = parts.length > 1 ? parts[1] : null;
        String text = parts.length > 2 ? parts[2] : "";
        // For tags like @return, no argument
        if (type.equals("return") || type.equals("deprecated")) {
          arg = null;
          text = parts.length > 1 ? parts[1] : "";
          if (parts.length > 2) {
            text += " " + parts[2];
          }
        }
        blockTags.add(new BlockTag(type, arg, text, indent));
      } else if (!inBlockTags) {
        // Description lines before first block tag
        if (descBuilder.length() > 0) {
          descBuilder.append("\n");
        }
        descBuilder.append(trimmed);
      }
    }
    return new DocComment(kind, descBuilder.toString(), blockTags, tagIndent);
  }

  private static int findIndent(String raw) {
    int indent = 0;
    for (char c : raw.toCharArray()) {
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

  Kind getKind() {
    return kind;
  }

  String getDescription() {
    return description;
  }

  List<BlockTag> getBlockTags() {
    return blockTags;
  }

  /**
   * Converts this DocComment to a JavaDoc comment string.
   *
   * @return the JavaDoc comment as a string
   */
  String toJava() {
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append("/**\n");
    if (description != null && !description.isEmpty()) {
      String[] lines = description.split("\r?\n");
      for (String line : lines) {
        sb.append(indent).append(" * ").append(line).append("\n");
      }
    }
    if (!blockTags.isEmpty()) {
      sb.append(indent).append(" *\n");
      for (BlockTag tag : blockTags) {
        sb.append(indent).append(" * @").append(tag.getType());
        if (tag.getArgument() != null) {
          sb.append(" ").append(tag.getArgument());
        }
        if (tag.getText() != null && !tag.getText().isEmpty()) {
          // TODO handle multi-line tag text
          sb.append(" ").append(tag.getText());
        }
        sb.append("\n");
      }
    }
    sb.append(indent).append(" */");
    return sb.toString();
  }

  @Override
  public String toString() {
      return toJava();
  }
}
