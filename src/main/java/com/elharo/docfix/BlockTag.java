package com.elharo.docfix;

/**
 * Represents a Javadoc block tag (e.g., @param, @return, @throws, @deprecated, etc.).
 */
class BlockTag {

  final String type; // e.g., param, return, throws, deprecated
  final String argument; // e.g., parameter name for @param, exception type for @throws, null otherwise
  final String text; // The text of the tag
  final int indent;

  BlockTag(String type, String argument, String text, int indent) {
    this.type = type;
    this.argument = argument;
    if (text != null && !text.isEmpty() && shouldLowerCase(text)) {
      char first = text.charAt(0);
      this.text = Character.toString(first).toLowerCase(java.util.Locale.ENGLISH) + text.substring(1);
    } else {
      this.text = text;
    }
    this.indent = indent;
  }

  // TODO handle title case ligatures
  /**
   * @return true iff the first word in the text is capitalized. That is,
   *     it contains an initial capital letter followed only by non-capital letters.
   */
  private boolean shouldLowerCase(String text) {
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

  // TODO use or delete
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
      sb.append(" ").append(text);
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return toJava();
  }
}
