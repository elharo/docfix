package com.elharo.docfix;

import java.io.StringReader;
import java.util.List;

public class DebugExtractChunks {
  public static void main(String[] args) throws Exception {
    // Debug the multiple Javadocs case specifically
    String source = "/**\n * Class comment.\n */\nclass Test {\n  /**\n   * Method comment.\n   */\n  void method() {\n  }\n}\n";
    
    System.out.println("=== Multiple Javadocs Debug ===");
    System.out.println("Full source: '" + source.replace("\n", "\\n") + "'");
    System.out.println();
    
    // Find Javadoc boundaries
    int firstJavadocStart = source.indexOf("/**");
    int firstJavadocEnd = source.indexOf("*/") + 2;
    int secondJavadocStart = source.indexOf("/**", firstJavadocEnd);
    int secondJavadocEnd = source.indexOf("*/", secondJavadocStart) + 2;
    
    System.out.println("First Javadoc: " + firstJavadocStart + " to " + firstJavadocEnd);
    System.out.println("Second Javadoc: " + secondJavadocStart + " to " + secondJavadocEnd);
    
    String firstJavadoc = source.substring(firstJavadocStart, firstJavadocEnd);
    String betweenJavadocs = source.substring(firstJavadocEnd, secondJavadocStart);
    String secondJavadoc = source.substring(secondJavadocStart, secondJavadocEnd);
    String afterSecondJavadoc = source.substring(secondJavadocEnd);
    
    System.out.println("First Javadoc: '" + firstJavadoc.replace("\n", "\\n") + "'");
    System.out.println("Between: '" + betweenJavadocs.replace("\n", "\\n") + "'");
    System.out.println("Second Javadoc: '" + secondJavadoc.replace("\n", "\\n") + "'");
    System.out.println("After: '" + afterSecondJavadoc.replace("\n", "\\n") + "'");
    
    // Apply line terminator removal
    System.out.println();
    System.out.println("After removeOneLeadingTrailingNewline:");
    String processedBetween = removeOneLeadingTrailingNewline(betweenJavadocs);
    String processedAfter = removeOneLeadingTrailingNewline(afterSecondJavadoc);
    System.out.println("Between: '" + processedBetween.replace("\n", "\\n") + "'");
    System.out.println("After: '" + processedAfter.replace("\n", "\\n") + "'");
    
    System.out.println();
    System.out.println("Expected:");
    System.out.println("Between: 'class Test {'");
    System.out.println("After: 'void method() {\\n  }' + ''");
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    System.out.println();
    System.out.println("Actual result:");
    for (int i = 0; i < chunks.size(); i++) {
      System.out.println(i + ": '" + chunks.get(i).replace("\n", "\\n") + "'");
    }
  }
  
  private static String removeOneLeadingTrailingNewline(String text) {
    if (text.isEmpty()) {
      return text;
    }
    
    String result = text;
    
    // Remove one leading newline
    if (result.startsWith("\n")) {
      result = result.substring(1);
    }
    
    // Remove one trailing newline  
    if (result.endsWith("\n")) {
      result = result.substring(0, result.length() - 1);
    }
    
    return result;
  }
}