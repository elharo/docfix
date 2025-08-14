package com.elharo.docfix;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for fixing Javadoc comments to conform to Oracle Javadoc
 * guidelines.
 * 
 * @see <a href=
 *      "https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html">How
 *      to Write Doc Comments for the Javadoc Tool</a>
 */
public class DocFix {

  /**
   * Fixes Javadoc comments in the code so that the first letter
   * of each javadoc tag is lower case and each javadoc comment is upper case.
   *
   * @param code the source code containing Javadoc comments
   * @return the fixed source code
   */
  public static String fix(String code) {
    String[] lines = code.split("\\R");
    List<String> fixedLines = FileParser.parseLines(Arrays.asList(lines));
    // TODO preserve line breaks
    return String.join("\n", fixedLines);
  }

  /**
   * Fixes Javadoc comments in the provided Java source file so that the first letter
   * of each doc comment is lower case. The file is modified in place.
   *
   * @param file the path to the Java source file
   * @throws IOException if an I/O error occurs
   */
  public static void fix(Path file) throws IOException {
    List<String> lines = FileParser.parseFile(file);
    try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
      for (String line : lines) {
        writer.write(line);
        writer.write("\n");
      }
    }
  }

  /**
   * Main method that applies Javadoc fixes to the file specified as the first
   * command line argument.
   *
   * @param args command line arguments; the last argument should be the path to
   *             the file to fix
   */
  public static void main(String[] args) {
    int argIndex = 0;
    if (args.length > 0 && "--dryrun".equals(args[0])) {
      argIndex = 1;
    }
    if (args.length <= argIndex) {
      System.err.println("Usage: java DocFix [--dryrun] <file-or-directory>");
      System.exit(1);
    }

    final boolean dryrun = "--dryrun".equals(args[0]);

    Path path = java.nio.file.Paths.get(args[argIndex]);
    
    // Check if the path exists
    if (!Files.exists(path)) {
      System.err.println("Error: File or directory does not exist: " + path);
      System.exit(1);
    }
    
    if (Files.isDirectory(path)) {
      try {
        Files.walk(path, 3)
            .filter(p -> !Files.isSymbolicLink(p))
            .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
            .forEach(p -> {
              try {
                if (dryrun) {
                  String original = Files.readString(p, StandardCharsets.UTF_8);
                  String fixed = fix(original);
                  if (!original.equals(fixed)) {
                    java.nio.file.Path cwd = java.nio.file.Paths.get("").toAbsolutePath();
                    java.nio.file.Path relPath = cwd.relativize(p.toAbsolutePath());
                    System.out.println(relPath);
                    printChangedLines(original, fixed);
                  }
                } else {
                  fix(p);
                }
              } catch (Exception e) {
                System.err.println("Failed to fix: " + p + ", " + e.getMessage());
              }
            });
      } catch (IOException e) {
        System.err.println("Error walking directory " + path + ": " + e.getMessage());
        System.exit(1);
      }
    } else {
      try {
        if (dryrun) {
          String original = Files.readString(path, StandardCharsets.UTF_8);
          String fixed = fix(original);
          if (!original.equals(fixed)) {
            java.nio.file.Path cwd = java.nio.file.Paths.get("").toAbsolutePath();
            java.nio.file.Path relPath = cwd.relativize(path.toAbsolutePath());
            System.out.println(relPath);
            printChangedLines(original, fixed);
          }
        } else {
          fix(path);
        }
      } catch (IOException e) {
        System.err.println("Error processing file " + path + ": " + e.getMessage());
        System.exit(1);
      }
    }
  }

  /**
   * Prints only the changed lines between the original and fixed content, showing
   * both old and new lines.
   */
  private static void printChangedLines(String original, String fixed) {
    String[] origLines = original.split("\\r?\\n");
    String[] fixedLines = fixed.split("\\r?\\n");
    int max = Math.max(origLines.length, fixedLines.length);
    for (int i = 0; i < max; i++) {
      String origLine = i < origLines.length ? origLines[i] : "";
      String fixedLine = i < fixedLines.length ? fixedLines[i] : "";
      if (!origLine.equals(fixedLine)) {
        if (!origLine.isEmpty()) {
          System.out.println(origLine);
        }
        if (!fixedLine.isEmpty()) {
          System.out.println(fixedLine);
        }
      }
    }
  }
}
