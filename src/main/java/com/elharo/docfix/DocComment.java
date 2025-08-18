package com.elharo.docfix;

import java.util.List;

/**
 * Represents a Javadoc comment, including its kind, description,
 * and block tags.
 */
class DocComment {

  enum Kind {
    CLASS, METHOD, FIELD
  }

  final Kind kind;
  final String description; // Main description (before block tags)
  final List<BlockTag> blockTags;
  final boolean hasTrailingBlankLine; // Whether there's a blank line after the last block tag

  // Indentation to be applied before entire comment
  protected final String indent;

  protected DocComment(Kind kind, String description, List<BlockTag> blockTags, int indent,
      boolean hasTrailingBlankLine) {
    this.kind = kind;
    if (description != null && !description.isEmpty()) {
      char first = description.charAt(0);
      description = (Character.toString(first).toUpperCase(java.util.Locale.ENGLISH) + description.substring(1)).trim();
      // add a period to the end of the description if it doesn't end with a
      // punctuation mark
      if ((Character.isLetterOrDigit(description.charAt(description.length() - 1)))) {
        description = description + ".";
      }
    }
    this.description = description;
    this.blockTags = sortTags(blockTags);
    this.hasTrailingBlankLine = hasTrailingBlankLine;
    this.indent = " ".repeat(indent);
  }

  private List<BlockTag> sortTags(List<BlockTag> blockTags) {
    if (blockTags == null || blockTags.size() <= 1) {
      return blockTags;
    }

    // Create a list with original indices to preserve order for same-type tags
    List<java.util.AbstractMap.SimpleEntry<BlockTag, Integer>> tagWithIndex = new java.util.ArrayList<>();
    for (int i = 0; i < blockTags.size(); i++) {
      tagWithIndex.add(new java.util.AbstractMap.SimpleEntry<>(blockTags.get(i), i));
    }

    // Define the standard tag order
    java.util.Map<String, Integer> tagOrder = new java.util.HashMap<>();
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
      if (order1 == null)
        order1 = Integer.MAX_VALUE;
      if (order2 == null)
        order2 = Integer.MAX_VALUE;

      // First sort by tag type priority
      int orderComparison = order1.compareTo(order2);
      if (orderComparison != 0) {
        return orderComparison;
      }

      // For throws tags, sort alphabetically by exception name (case insensitive)
      if ("throws".equals(type1) && "throws".equals(type2)) {
        String arg1 = tag1.getArgument();
        String arg2 = tag2.getArgument();
        if (arg1 != null && arg2 != null) {
          return arg1.compareToIgnoreCase(arg2);
        }
      }

      // For tags of the same type (except throws), preserve original order
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
    int tagIndent = findIndent(raw);

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
    String[] lines = body.split("\r?\n");
    StringBuilder description = new StringBuilder();
    List<BlockTag> blockTags = new java.util.ArrayList<>();
    boolean inBlockTags = false;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      String trimmed = line.trim();
      int indent = line.length() - trimmed.length();
      if (trimmed.startsWith("*")) {
        trimmed = trimmed.substring(1).trim();
      }
      if (trimmed.startsWith("@")) { // starts a new block tag
        inBlockTags = true;
        // Add any additional lines that are part of the same block tag
        while (i < lines.length - 1 && !lines[i + 1].trim().startsWith("* @")) {
          i++;
          if (!lines[i].trim().endsWith("*")) {
            trimmed += "\n" + lines[i];
          }
        }
        BlockTag blockTag = BlockTag.parse(trimmed, indent);
        blockTags.add(blockTag);
      } else if (!inBlockTags) {
        // Description lines before first block tag
        if (description.length() > 0) {
          description.append("\n");
        }
        description.append(trimmed);
      }
    }

    // Check for trailing blank lines after all block tags
    boolean hasTrailingBlankLine = false;
    if (!blockTags.isEmpty()) {
      // Look for blank lines after the last block tag
      for (int i = lines.length - 1; i >= 0; i--) {
        String line = lines[i].trim();
        if (line.startsWith("*")) {
          line = line.substring(1).trim();
        }
        if (line.isEmpty()) {
          hasTrailingBlankLine = true;
          break;
        } else if (line.startsWith("@")) {
          // Found the last block tag, no trailing blank line
          break;
        }
      }
    }
    return new DocComment(kind, description.toString(), blockTags, tagIndent, hasTrailingBlankLine);
  }

  // TODO move this to utility class
  static int findIndent(String raw) {
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
      // TODO use toJava in BlockTag instead
      for (BlockTag tag : blockTags) {
        sb.append(indent).append(" * @").append(tag.getType());
        if (tag.getArgument() != null) {
          sb.append(" ").append(tag.getArgument());
        }
        if (tag.getText() != null && !tag.getText().isEmpty()) {
          sb.append(tag.getSpaces()).append(tag.getText());
        }
        sb.append("\n");
      }
      if (hasTrailingBlankLine) {
        sb.append(indent).append(" *\n");
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
