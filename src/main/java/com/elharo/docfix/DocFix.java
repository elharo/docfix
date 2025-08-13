package com.elharo.docfix;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    JavaParser parser = new JavaParser();
    CompilationUnit compilationUnit = parser.parse(code).getResult().orElse(null);
    if (compilationUnit == null) {
      return code;
    }

    List<Comment> allComments = compilationUnit.getAllContainedComments();
    String result = code;
    
    // Process comments in reverse order to maintain correct string positions
    // when doing replacements
    for (int i = allComments.size() - 1; i >= 0; i--) {
      Comment comment = allComments.get(i);
      if (comment instanceof JavadocComment) {
        JavadocComment javadoc = (JavadocComment) comment;
        String originalContent = comment.getContent();
        
        // TODO how to determine the kind of Javadoc comment?
        DocComment docComment = DocComment.parse(null, originalContent);
        String fixedContent = docComment.toJava();
        
        // Only replace if the content actually changed
        if (!originalContent.equals(fixedContent)) {
          // Get the position of the comment in the source code
          int startPos = comment.getBegin().get().column - 1;
          int startLine = comment.getBegin().get().line - 1;
          int endPos = comment.getEnd().get().column;
          int endLine = comment.getEnd().get().line - 1;
          
          // Split the result into lines to work with line/column positions
          String[] lines = result.split("\\r?\\n", -1);
          
          if (startLine == endLine) {
            // Single line comment
            String line = lines[startLine];
            String before = line.substring(0, startPos);
            String after = line.substring(endPos);
            lines[startLine] = before + "/**" + fixedContent + "*/" + after;
          } else {
            // Multi-line comment
            String firstLine = lines[startLine];
            String lastLine = lines[endLine];
            
            String before = firstLine.substring(0, startPos);
            String after = lastLine.substring(endPos);
            
            // Replace the comment lines
            String[] fixedLines = ("/**" + fixedContent + "*/").split("\\r?\\n", -1);
            
            // Build new lines array
            String[] newLines = new String[lines.length - (endLine - startLine) + fixedLines.length - 1];
            
            // Copy lines before the comment
            System.arraycopy(lines, 0, newLines, 0, startLine);
            
            // Add the first line with prefix
            newLines[startLine] = before + fixedLines[0];
            
            // Add middle lines of the fixed comment
            for (int j = 1; j < fixedLines.length - 1; j++) {
              newLines[startLine + j] = fixedLines[j];
            }
            
            // Add the last line with suffix
            if (fixedLines.length > 1) {
              newLines[startLine + fixedLines.length - 1] = fixedLines[fixedLines.length - 1] + after;
            } else {
              newLines[startLine] = before + fixedLines[0] + after;
            }
            
            // Copy lines after the comment
            System.arraycopy(lines, endLine + 1, newLines, startLine + fixedLines.length, 
                           lines.length - endLine - 1);
            
            lines = newLines;
          }
          
          // Reconstruct the result string
          result = String.join("\n", lines);
        }
      }
    }

    return result;
  }

  /**
   * Fixes Javadoc comments in the provided Java source file so that the first
   * letter
   * of each doc comment is lower case. The file is modified in place.
   *
   * @param file the path to the Java source file
   * @throws IOException if an I/O error occurs
   */
  public static void fix(Path file) throws IOException {
    String code = Files.readString(file, StandardCharsets.UTF_8);
    String fixed = fix(code);
    Files.writeString(file, fixed, StandardCharsets.UTF_8);
  }

  /**
   * Main method that applies Javadoc fixes to the file specified as the first
   * command line argument.
   *
   * @param args command line arguments; the last argument should be the path to
   *             the file to fix
   */
  public static void main(String[] args) throws IOException {
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
    if (Files.isDirectory(path)) {
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
    } else {
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
