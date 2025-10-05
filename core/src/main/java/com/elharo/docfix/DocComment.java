package com.elharo.docfix;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Javadoc comment, including its kind, description, and block tags.
 */
class DocComment {

  enum Kind {
    CLASS, METHOD, FIELD
  }

  final Kind kind;
  final String description; // Main description (before block tags)
  final List<BlockTag> blockTags;

  // Indentation to be applied before each line of the comment
  protected final String indent;

  protected DocComment(Kind kind, String description, List<BlockTag> blockTags, int indent) {
    this.kind = kind;
    if (description != null && !description.isBlank()) {
      char first = description.charAt(0);
      description = (Character.toString(first).toUpperCase(java.util.Locale.ENGLISH) + description.substring(1)).trim();
      // add a period to the end of the description if it doesn't end with a
      // punctuation mark and doesn't end with a URL
      if ((Character.isLetterOrDigit(description.charAt(description.length() - 1))) 
          && !Strings.endsWithURL(description)) {
        description = description + ".";
      }
    }
    this.description = description;
    this.blockTags = sortTags(blockTags);
    this.indent = " ".repeat(indent);
  }

  private final static Map<String, Integer> tagOrder = new HashMap<>();

  static {
    tagOrder.put("author", 0);
    tagOrder.put("version", 1);
    tagOrder.put("param", 2);
    tagOrder.put("return", 3);
    tagOrder.put("throws", 4);
    tagOrder.put("see", 5);
    tagOrder.put("since", 6);
    tagOrder.put("serial", 7);
    tagOrder.put("serialField", 7); // Same priority as serial
    tagOrder.put("serialData", 7); // Same priority as serial
    tagOrder.put("deprecated", 8);
  }

  private static List<BlockTag> sortTags(List<BlockTag> blockTags) {
    if (blockTags == null || blockTags.size() <= 1) {
      return blockTags;
    }

    // Create a list with original indices to preserve order for same-type tags
    List<AbstractMap.SimpleEntry<BlockTag, Integer>> tagWithIndex = new ArrayList<>();
    for (int i = 0; i < blockTags.size(); i++) {
      tagWithIndex.add(new AbstractMap.SimpleEntry<>(blockTags.get(i), i));
    }

    tagWithIndex.sort((entry1, entry2) -> {
      BlockTag tag1 = entry1.getKey();
      BlockTag tag2 = entry2.getKey();
      int index1 = entry1.getValue();
      int index2 = entry2.getValue();

      String type1 = tag1.getType();
      String type2 = tag2.getType();

      Integer order1 = tagOrder.get(type1);
      Integer order2 = tagOrder.get(type2);

      // Unknown/custom tags get highest priority (sorted last)
      if (order1 == null) {
        order1 = Integer.MAX_VALUE;
      }
      if (order2 == null) {
        order2 = Integer.MAX_VALUE;
      }

      // First sort by tag type priority
      int orderComparison = order1.compareTo(order2);
      if (orderComparison != 0) {
        return orderComparison;
      }

      // For throws tags, sort alphabetically by exception name (case-insensitive)
      if ("throws".equals(type1) && "throws".equals(type2)) {
        String arg1 = tag1.getArgument();
        String arg2 = tag2.getArgument();
        if (arg1 != null && arg2 != null) {
          return arg1.compareToIgnoreCase(arg2);
        }
      }

      // For tags of the same type (except throws), preserve original order.
      // This isn't fully compatible with the guidelines, but we have no way of
      // knowing which authors were added first and so forth.
      return Integer.compare(index1, index2);
    });

    // Extract the sorted tags
    List<BlockTag> sortedTags = new java.util.ArrayList<>();
    for (java.util.AbstractMap.SimpleEntry<BlockTag, Integer> entry : tagWithIndex) {
      sortedTags.add(entry.getKey());
    }

    return sortedTags;
  }

  static DocComment parse(Kind kind, String raw) {
    String lineEnding = Strings.detectLineEnding(raw);
    raw = raw.replace(lineEnding, "\n"); // Normalize line endings

    int tagIndent = Strings.findIndent(raw);
    int postAsteriskIndent = findPostAsteriskIndent(raw);

    // Remove leading/trailing comment markers and split into lines
    String body = raw.trim();
    boolean singleLine = !body.contains("\n");
    if (body.startsWith("/**")) {
      body = body.substring(3);
    }
    if (body.endsWith("**/")) {
      body = body.substring(0, body.length() - 3).trim();
    } else if (body.endsWith("*/")) {
      body = body.substring(0, body.length() - 2).trim();
    }

    // If it's a single line comment with no block tags, return a SingleLineComment
    if (singleLine && !body.contains("@")) {
      String description = body.trim();
      return new SingleLineComment(kind, description, tagIndent);
    }
    String[] lines = body.split("\n");
    StringBuilder description = new StringBuilder();
    List<BlockTag> blockTags = new java.util.ArrayList<>();
    boolean inBlockTags = false;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      String trimmed = line.stripLeading();
      if (trimmed.startsWith("*")) {
        // Remove asterisk and at most one space after it to preserve indentation
        String afterAsterisk = trimmed.substring(1);
        if (afterAsterisk.startsWith(" ")) {
          // Remove spaces after asterisk, but preserve spaces for indentation
          trimmed = afterAsterisk.substring(postAsteriskIndent);
        } else {
          // No space after asterisk
          trimmed = afterAsterisk;
        }
      }
      if (trimmed.stripLeading().startsWith("@")) { // starts a new block tag
        inBlockTags = true;
        trimmed = trimmed.stripLeading();
        // Add any additional lines that are part of the same block tag
        while (i < lines.length - 1 && !lines[i + 1].matches("^\\s*\\*\\s*@.*")) {
          i++;
          if (!lines[i].trim().endsWith("*")) {
            trimmed += "\n" + lines[i];
          }
        }
        BlockTag blockTag = BlockTag.parse(trimmed);
        blockTags.add(blockTag);
      } else if (!inBlockTags) {
        // Description lines before first block tag
        if (description.length() > 0) {
          description.append("\n");
        }
        description.append(trimmed);
      }
    }

    return new DocComment(kind, description.toString(), blockTags, tagIndent);
  }

  // visible for testing
  static int findPostAsteriskIndent(String raw) {
    String[] lines = raw.split("\n");
    int minSpaces = -1;
    // ignore first line after /**
    for (int i = 1; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.startsWith("* @")) {
        break; // Stop at first block tag
      }
      if (line.startsWith("*") && !line.endsWith("*/") && !"*".equals(line)) {
        int lineIndent = Strings.findIndent(line.substring(1));
        if (minSpaces == -1 || lineIndent < minSpaces) {
          minSpaces = lineIndent;
        }
      }
    }

    if (minSpaces < 1) {
      minSpaces = 1; // Ensure at least one space after the asterisk
    }
    return minSpaces;
  }

  final Kind getKind() {
    return kind;
  }

  final String getDescription() {
    return description;
  }

  final List<BlockTag> getBlockTags() {
    return blockTags;
  }

  /**
   * Converts this DocComment to a JavaDoc comment string.
   *
   * @return the JavaDoc comment as a string
   */
  String toJava() {
    if (description.isBlank() && blockTags.isEmpty()) {
      return ""; // No comment to generate
    }
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append("/**\n");
    if (description != null && !description.isBlank()) {
      String[] lines = description.split("\r?\n");
      for (String line : lines) {
        sb.append(indent);
        sb.append(" *");
        if (!line.isEmpty()) {
          sb.append(" ");
          sb.append(line);
        }
        sb.append("\n");
      }
    }
    if (!blockTags.isEmpty()) {
      if (description != null && !description.isBlank()) {
        sb.append(indent).append(" *\n"); // Blank line between description and block tags
      }
      boolean indentDescription = blockTags.size() > 1;
      for (BlockTag tag : blockTags) {
        sb.append(indent).append(tag.toJava(indentDescription));
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
