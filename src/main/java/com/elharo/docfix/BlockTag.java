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
    if (text != null && !text.isEmpty()) {
      char first = text.charAt(0);
      this.text = Character.toString(first).toLowerCase(java.util.Locale.ENGLISH) + text.substring(1);
    } else {
      this.text = text;
    }
    this.indent = indent;
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
