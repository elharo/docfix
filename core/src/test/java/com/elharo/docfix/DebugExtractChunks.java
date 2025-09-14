package com.elharo.docfix;

import java.io.StringReader;
import java.util.List;

public class DebugExtractChunks {
  public static void main(String[] args) throws Exception {
    // Test case 1 - PASSES
    testCase("Simple class", 
        "package test;\n\n/**\n * A simple class.\n */\npublic class Test {\n}\n",
        new String[]{"package test;", "", "/**\n * A simple class.\n */", "public class Test {\n}"});
    
    // Test case 2 - FAILS (expects 5, probably getting 4)
    testCase("Multiple Javadocs",
        "/**\n * Class comment.\n */\nclass Test {\n  /**\n   * Method comment.\n   */\n  void method() {\n  }\n}\n",
        new String[]{"/**\n * Class comment.\n */", "class Test {", "/**\n   * Method comment.\n   */", "void method() {\n  }", ""});
    
    // Test case 3 - FAILS (expects 4, probably getting 7) 
    testCase("Leading/trailing terminators",
        "\npackage test;\n\n/**\n * Comment.\n */\n\nclass Test {\n}\n\n",
        new String[]{"package test;\n", "/**\n * Comment.\n */", "class Test {\n}", ""});
  }
  
  private static void testCase(String name, String source, String[] expected) throws Exception {
    System.out.println("=== " + name + " ===");
    System.out.println("Source: '" + source.replace("\n", "\\n") + "'");
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    System.out.println("Expected " + expected.length + " chunks:");
    for (int i = 0; i < expected.length; i++) {
      System.out.println("  " + i + ": '" + expected[i].replace("\n", "\\n") + "'");
    }
    
    System.out.println("Got " + chunks.size() + " chunks:");
    for (int i = 0; i < chunks.size(); i++) {
      System.out.println("  " + i + ": '" + chunks.get(i).replace("\n", "\\n") + "'");
    }
    
    boolean match = chunks.size() == expected.length;
    if (match) {
      for (int i = 0; i < expected.length; i++) {
        if (!expected[i].equals(chunks.get(i))) {
          match = false;
          break;
        }
      }
    }
    
    System.out.println("Result: " + (match ? "PASS" : "FAIL"));
    System.out.println();
  }
}