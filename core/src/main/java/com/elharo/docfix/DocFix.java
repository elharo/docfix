package com.elharo.docfix;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility class for fixing Javadoc comments to conform to Oracle Javadoc guidelines.
 *
 * @see <a href=
 *      "https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html">How
 *      to Write Doc Comments for the Javadoc Tool</a>
 */
public final class DocFix {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private DocFix() {
  }

  /**
   * Fixes Javadoc comments in the code so that the first letter
   * of each javadoc tag is lower case and each javadoc comment is upper case.
   *
   * @param code the source code containing Javadoc comments
   * @return the fixed source code
   */
  public static String fix(String code) {
    String lineEnding = Strings.detectLineEnding(code);
    String[] lines = code.split("\\R");
    List<String> fixedLines = FileParser.parseLines(lines, lineEnding);
    return String.join(lineEnding, fixedLines);
  }

  /**
   * Fixes Javadoc comments in the provided Java source file so that the first letter
   * of each doc comment is lower case. The file is modified in place.
   *
   * @param file the path to the Java source file
   * @throws IOException if an I/O error occurs
   */
  public static void fix(Path file) throws IOException {
    fix(file, null);
  }

  /**
   * Fixes Javadoc comments in the provided Java source file according to Oracle Javadoc guidelines.
   * The file is modified in place.
   *
   * @param file the path to the Java source file
   * @param encoding the character encoding to use, or null to auto-detect
   * @throws IOException if an I/O error occurs
   */
  public static void fix(Path file, Charset encoding) throws IOException {
    if (encoding == null) {
      encoding = EncodingDetector.detectEncoding(file);
    }
    String code = Files.readString(file, encoding);
    String lineEnding = Strings.detectLineEnding(code);
    String[] rawLines = code.split("\\R");
    List<String> fixedLines = FileParser.parseLines(rawLines, lineEnding);
    try (Writer writer = Files.newBufferedWriter(file, encoding)) {
      for (String line : fixedLines) {
        writer.write(line);
        writer.write(lineEnding);
      }
    }
  }

  /**
   * Fixes Javadoc comments in Java files in the provided directory according to Oracle Javadoc guidelines.
   * The file is modified in place.
   *
   * @param path the directory to scan for Java source files
   * @param dryrun if true only prints what would be changed without actually changning any files
   * @param encoding the character encoding to use, or null to auto-detect
   * @throws IOException if an I/O error occurs
   */
  public static void fixDirectory(Path path, boolean dryrun, Charset encoding) throws IOException {
    Files.walk(path, 63)
        .filter(p -> !Files.isSymbolicLink(p))
        .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
        .forEach(p -> {
          try {
            if (dryrun) {
              Charset charset = encoding != null ? encoding : EncodingDetector.detectEncoding(p);
              String original = Files.readString(p, charset);
              String fixed = fix(original);
              if (!original.equals(fixed)) {
                Path cwd = Paths.get("").toAbsolutePath();
                Path relPath = cwd.relativize(p.toAbsolutePath());
                System.out.println(relPath);
                printChangedLines(original, fixed);
              }
            } else {
              fix(p, encoding);
            }
          } catch (IOException e) {
            System.err.println("Failed to fix: " + p + ", " + e.getMessage());
          }
        });
  }

  /**
   * Main method that applies Javadoc fixes to the file specified as the first
   * command line argument.
   *
   * @param args command line arguments; supported flags: [--dryrun] [-encoding charset] &lt;file-or-directory&gt;
   */
  public static void main(String[] args) {
    int argIndex = 0;
    boolean dryrun = false;
    Charset encoding = null;

    // Parse command line arguments
    while (argIndex < args.length && args[argIndex].startsWith("-")) {
      if ("--dryrun".equals(args[argIndex])) {
        dryrun = true;
        argIndex++;
      } else if ("-encoding".equals(args[argIndex])) {
        if (argIndex + 1 >= args.length) {
          System.err.println("Error: -encoding flag requires a charset name");
          System.exit(1);
        }
        try {
          encoding = Charset.forName(args[argIndex + 1]);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
          System.err.println("Error: Invalid charset name: " + args[argIndex + 1]);
          System.exit(1);
        }
        argIndex += 2;
      } else {
        System.err.println("Error: Unknown flag: " + args[argIndex]);
        System.exit(1);
      }
    }

    if (args.length <= argIndex) {
      System.err.println("Usage: java DocFix [--dryrun] [-encoding charset] <file-or-directory>");
      System.exit(1);
    }

    Path path = Paths.get(args[argIndex]);

    // Check if the path exists
    if (!Files.exists(path)) {
      System.err.println("Error: File or directory does not exist: " + path);
      System.exit(1);
    }

    if (Files.isDirectory(path)) {
      try {
        fixDirectory(path, dryrun, encoding);
      } catch (IOException ex) {
        System.err.println("Error walking directory " + path + ": " + ex.getMessage());
        System.exit(1);
      }
    } else {
      try {
        if (dryrun) {
          Charset charset = encoding != null ? encoding : EncodingDetector.detectEncoding(path);
          String original = Files.readString(path, charset);
          String fixed = fix(original);
          if (!original.equals(fixed)) {
            java.nio.file.Path cwd = java.nio.file.Paths.get("").toAbsolutePath();
            java.nio.file.Path relPath = cwd.relativize(path.toAbsolutePath());
            System.out.println(relPath);
            printChangedLines(original, fixed);
          }
        } else {
          fix(path, encoding);
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
