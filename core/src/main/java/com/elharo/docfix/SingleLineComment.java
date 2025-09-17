package com.elharo.docfix;

import java.util.Collections;

/**
 * Represents a single-line Javadoc comment that has no block tags and
 * where the description does not contain line breaks. The comment markers appear
 * on the same line as the description.
 */
class SingleLineComment extends DocComment {

  SingleLineComment(Kind kind, String description, int indent) {
    super(kind, description, Collections.emptyList(), indent);
  }

  /**
   * Converts this SingleLineComment to a JavaDoc comment string.
   *
   * @return the single-line JavaDoc comment as a string
   */
  @Override
  String toJava() {
    if (description.isBlank() && blockTags.isEmpty()) {
      return ""; // No comment to generate
    }
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append("/** ");
    if (description != null && !description.isEmpty()) {
      String desc = description;
      // Add period if description doesn't end with punctuation and doesn't end with URL
      if (!desc.endsWith(".") && !desc.endsWith("!") && !desc.endsWith("?") 
          && !Strings.endsWithURL(desc)) {
        desc += ".";
      }
      sb.append(desc);
    }
    sb.append(" */");
    return sb.toString();
  }
}