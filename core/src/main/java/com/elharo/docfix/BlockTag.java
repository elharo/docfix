package com.elharo.docfix;

import com.elharo.propernouns.Names;
import java.util.Set;

/**
 * Represents a Javadoc block tag (e.g., @param, @return, @throws, @deprecated, etc.).
 */
class BlockTag {

  private final String type; // e.g., param, return, throws, deprecated
  private final String argument; // e.g., parameter name for @param, exception type for @throws, null otherwise
  private final String text; // The text of the tag

  /**
   * Spaces between the argument and the description.
   * Most often exactly one space, but can be more if the tags are aligned.
   */
  private final String spaces;

  private BlockTag(String type, String argument, String text, String spaces) {
    if ("exception".equals(type)) {
      type = "throws"; // Normalize 'exception' to 'throws'
    }
    this.type = type;
    this.argument = argument;

    if (text.startsWith("- ")) {
      text = text.substring(2).trim(); // Remove leading "- "
    }

    // Remove redundant "return" or "returns" at the start of @return tag descriptions
    if ("return".equals(type) && text != null && !text.isEmpty()) {
      int returnsLength = "returns ".length();
      int returnLength = "return ".length();
      // Check for "returns " (with space) at the start, case-insensitive
      if (text.length() > returnsLength && 
          text.regionMatches(true, 0, "returns ", 0, returnsLength)) {
        text = text.substring(returnsLength);
      }
      // Check for "return " (with space) at the start, case-insensitive
      else if (text.length() > returnLength && 
               text.regionMatches(true, 0, "return ", 0, returnLength)) {
        text = text.substring(returnLength);
      }
    }

    if (text != null && !text.isEmpty() && shouldLowerCase(type, text)) {
      char first = text.charAt(0);
      text = Character.toString(first).toLowerCase(java.util.Locale.ENGLISH) + text.substring(1);
    }

    // Remove trailing period if not a sentence.
    // Check for periods followed by space or newline to detect multiple sentences.
    // Don't remove periods from @deprecated tags since they typically contain complete sentences.
    // Don't remove periods from abbreviations like Inc., Ltd., Corp., etc.
    if (!text.contains(". ") && !text.contains(".\n") && text.endsWith(".") && !"deprecated".equals(type) && !endsWithAbbreviation(text)) {
      text = text.trim().substring(0, text.trim().length() - 1);
    }
    this.text = text;
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

  static BlockTag parse(String trimmed) {
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
         int x = Strings.findIndent(parts[2]);
         spaces = " ".repeat(x + 1);
       }
    }
    BlockTag blockTag = new BlockTag(type, arg, text, spaces);
    return blockTag;
  }

  // Known proper nouns that should remain capitalized
  // This is kept for specific technical terms that may not be in the Names database
  private static final Set<String> PROPER_NOUNS = Set.of(
      "Java"
  );

  // TODO handle title case ligatures
  /**
   * @return true iff the first word in the text is capitalized. That is,
   *     it contains an initial capital letter followed only by non-capital letters.
   */
  private boolean shouldLowerCase(String type, String text) {
    if ("author".equals(type) || "see".equals(type) || "deprecated".equals(type)) {
      return false; // author is usually a proper name, deprecated tags use complete sentences
    }

    if (!Character.isUpperCase(text.charAt(0))) {
      return false;
    }

    // Extract the first word
    String firstWord = extractFirstWord(text);
    
    // Check if it's a known proper noun from our static set
    if (PROPER_NOUNS.contains(firstWord)) {
      return false;
    }

    // Check if it's a name using the propernouns library
    if (Names.isName(firstWord)) {
      return false;
    }

    // Check if it's an acronym (3+ letters, all uppercase)
    if (isAcronym(firstWord)) {
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

  /**
   * Extracts the first word from the given text.
   *
   * @param text the text to extract the first word from
   * @return the first word
   */
  private String extractFirstWord(String text) {
    text = text.trim();
    int endIndex = 0;
    while (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
      endIndex++;
    }
    return text.substring(0, endIndex);
  }

  /**
   * Checks if a word is an acronym (3+ letters, all uppercase).
   *
   * @param word the word to check
   * @return true if the word is an acronym
   */
  private boolean isAcronym(String word) {
    if (word.length() < 3) {
      return false;
    }
    
    for (char c : word.toCharArray()) {
      if (Character.isLowerCase(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Common abbreviations that should retain their trailing period.
   * These are commonly found at the end of names, titles, and company names.
   */
  private static final Set<String> ABBREVIATIONS = Set.of(
      "Inc.", "Ltd.", "Corp.", "Co.", "LLC.", "LLP.", "LP.",
      "Jr.", "Sr.", "Esq.",
      "Dr.", "Mr.", "Mrs.", "Ms.", "Miss.", "Prof.",
      "Ph.D.", "M.D.", "M.B.A.", "B.A.", "B.S.", "M.A.", "M.S.",
      "Ave.", "St.", "Rd.", "Blvd.", "Dept.", "Univ.",
      "etc.", "e.g.", "i.e.", "cf.", "vs.", "vol.", "no.", "pp."
  );

  /**
   * Checks if the text ends with a common abbreviation that should keep its period.
   * This method performs early termination when a match is found.
   * The abbreviation set is small (~30 items), so linear search is efficient.
   *
   * @param text the text to check
   * @return true if the text ends with a known abbreviation
   */
  private boolean endsWithAbbreviation(String text) {
    if (text == null || text.isEmpty()) {
      return false;
    }
    
    String trimmed = text.trim();
    // Early return if text is too short to contain any abbreviation
    if (trimmed.length() < 3) { // shortest abbreviations are 3 chars (e.g., "Co.", "St.")
      return false;
    }
    
    // Check if text ends with any known abbreviation
    // Early termination: returns immediately when match is found
    for (String abbreviation : ABBREVIATIONS) {
      if (trimmed.endsWith(abbreviation)) {
        return true;
      }
    }
    return false;
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

  /**
   * Determines if this block tag has no argument and no description text.
   * For @return tags (which have no argument), only the text is checked.
   * For @param and @throws tags, both argument and text must be null/empty.
   * Other tags (like custom tags) are kept even if they have no text.
   *
   * @return true if the tag is blank and should be removed, false otherwise
   */
  boolean isBlank() {
    // Only check @param, @return, and @throws tags
    if (!("param".equals(type) || "return".equals(type) || "throws".equals(type))) {
      return false;
    }
    
    // For @return (no argument tag), check only text
    if ("return".equals(type)) {
      return text == null || text.trim().isEmpty();
    }
    
    // For @param and @throws, both argument and text must be blank
    boolean argBlank = argument == null || argument.trim().isEmpty();
    boolean textBlank = text == null || text.trim().isEmpty();
    return argBlank && textBlank;
  }

  String toJava(boolean indent) {
    StringBuilder sb = new StringBuilder();
    sb.append(" * @").append(type);
    if (argument != null && !argument.isEmpty()) {
      sb.append(" ").append(argument);
    }
    if (text != null && !text.isEmpty()) {
      if (indent) {
        sb.append(spaces);
      } else {
        sb.append(" ");
      }
      sb.append(text);
    }
    sb.append("\n");
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return toJava(false);
  }

}
