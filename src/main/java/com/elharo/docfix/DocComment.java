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

  DocComment(Kind kind, String description, List<BlockTag> blockTags) {
    this.kind = kind;
    if (description != null && !description.isEmpty()) {
      char first = description.charAt(0);
      this.description = Character.toString(first).toUpperCase(java.util.Locale.ENGLISH) + description.substring(1);
    } else {
      this.description = description;
    }
    this.blockTags = blockTags;
  }

  static DocComment parse(Kind kind, String raw) {
    // Remove leading/trailing comment markers and split into lines
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
        }
        blockTags.add(new BlockTag(type, arg, text, indent));
      } else if (!inBlockTags) {
        // Description lines before first block tag
        if (descBuilder.length() > 0) {
          descBuilder.append(" ");
        }
        descBuilder.append(trimmed);
      }
    }
    return new DocComment(kind, descBuilder.toString(), blockTags);
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
    sb.append("/**\n");
    if (description != null && !description.isEmpty()) {
      sb.append(" * ").append(description).append("\n");
      if (!blockTags.isEmpty()) {
        sb.append(" *\n");
      }
    }
    for (BlockTag tag : blockTags) {
      sb.append(tag.toJava());
      sb.append("\n");
    }
    sb.append(" */");
    return sb.toString();
  }
}
