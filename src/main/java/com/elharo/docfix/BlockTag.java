package com.elharo.docfix;

import static com.elharo.docfix.DocComment.findIndent;

import java.util.Set;

/**
 * Represents a Javadoc block tag (e.g., @param, @return, @throws, @deprecated, etc.).
 */
class BlockTag {

  private final String type; // e.g., param, return, throws, deprecated
  private final String argument; // e.g., parameter name for @param, exception type for @throws, null otherwise
  private final String text; // The text of the tag
  private final int indent; // How many spaces before the comment starts

  /**
   * Spaces between the argument and the description.
   * Most often exactly one space, but can be more if the tags are aligned.
   */
  private final String spaces;

  private BlockTag(String type, String argument, String text, int indent, String spaces) {
    if ("exception".equals(type)) {
      type = "throws"; // Normalize 'exception' to 'throws'
    }
    this.type = type;
    this.argument = argument;
    if (text != null && !text.isEmpty() && shouldLowerCase(type, text)) {
      char first = text.charAt(0);
      text = Character.toString(first).toLowerCase(java.util.Locale.ENGLISH) + text.substring(1);
    }

    // Remove trailing period if not a sentence
    if (!text.contains(". ") && text.endsWith(".")) {
      text = text.trim().substring(0, text.trim().length() - 1);
    }
    this.text = text;
    this.indent = indent;
    this.spaces = spaces;
  }

  private final static Set<String> noArgumentTags = Set.of(
      "return",
      "deprecated",
      "author",
      "serial",
      "see",
      "serialData",
      "since",
      "version"
  );

  static BlockTag parse(String trimmed, int indent) {
    // Parse block tag: e.g. @param real The real part
    String[] parts = trimmed.split(" ", 3);
    String type = parts[0].substring(1); // remove '@'
    String text = "";
    // For tags like @return, no argument
    String arg = null;
    String spaces = " ";
    if (noArgumentTags.contains(type)) {
      if (parts.length > 1) {
        text += parts[1];
      }
      if (parts.length > 2) {
        text += " " + parts[2].trim();
      }
    } else {
       arg = parts.length > 1 ? parts[1] : null;
       text = parts.length > 2 ? parts[2].trim() : "";
       if (parts.length > 2) {
         int x = findIndent(parts[2]);
         spaces = " ".repeat(x + 1);
       }
    }
    BlockTag blockTag = new BlockTag(type, arg, text, indent, spaces);
    return blockTag;
  }

  // TODO handle title case ligatures
  /**
   * @return true iff the first word in the text is capitalized. That is,
   *     it contains an initial capital letter followed only by non-capital letters.
   */
  private boolean shouldLowerCase(String type, String text) {
    if ("author".equals(type)) {
      return false; // author is usually a proper name
    }

    if (!Character.isUpperCase(text.charAt(0))) {
      return false;
    }

    // Now we know first character is uppercase.
    char[] characters = text.toCharArray();
    for (int i = 1; i < characters.length; i++) {
      char c = characters[i];
      if (Character.isWhitespace(c)) { // end of first word
        return true;
      }
      if (Character.isUpperCase(c)) { // There's more than one uppercase letter in the first word
        return false;
      }
    }
    return true;
  }

  String getType() {
    return type;
  }

  String getText() {
    return text;
  }

  String getArgument() {
    return argument;
  }

  String toJava() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append(' ');
    }
    sb.append("* @").append(type);
    if (argument != null && !argument.isEmpty()) {
      sb.append(" ").append(argument);
    }
    if (text != null && !text.isEmpty()) {
      sb.append(spaces).append(text);
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return toJava();
  }

  public String getSpaces() {
    return this.spaces;
  }
}
