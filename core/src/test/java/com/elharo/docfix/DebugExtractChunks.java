package com.elharo.docfix;

import java.io.StringReader;
import java.util.List;

public class DebugExtractChunks {
  public static void main(String[] args) throws Exception {
    String source = "package test;\n\n/**\n * A simple class.\n */\npublic class Test {\n}\n";
    
    System.out.println("Source chars:");
    for (int i = 0; i < source.length(); i++) {
      char c = source.charAt(i);
      System.out.print(i + ":" + (c == '\n' ? "\\n" : String.valueOf(c)) + " ");
    }
    System.out.println();
    
    // Let's manually split this by Javadoc boundaries:
    int javadocStart = source.indexOf("/**");
    int javadocEnd = source.indexOf("*/", javadocStart) + 2;
    
    System.out.println("Javadoc start: " + javadocStart + ", end: " + javadocEnd);
    
    String before = source.substring(0, javadocStart);
    String javadoc = source.substring(javadocStart, javadocEnd);
    String after = source.substring(javadocEnd);
    
    System.out.println("Before Javadoc: '" + before + "'");
    System.out.println("Javadoc: '" + javadoc + "'");
    System.out.println("After Javadoc: '" + after + "'");
    
    System.out.println("\nExpected chunking:");
    System.out.println("Chunk 0: 'package test;'");
    System.out.println("Chunk 1: ''");
    System.out.println("Chunk 2: '/**\\n * A simple class.\\n */'");
    System.out.println("Chunk 3: 'public class Test {\\n}'");
    
    List<String> chunks = FileParser.extractChunks(new StringReader(source));
    
    System.out.println("\nActual chunks (" + chunks.size() + "):");
    for (int i = 0; i < chunks.size(); i++) {
      System.out.println(i + ": '" + chunks.get(i) + "'");
    }
  }
}