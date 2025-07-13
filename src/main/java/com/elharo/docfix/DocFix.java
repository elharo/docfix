package com.elharo.docfix;

import static com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter.setup;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;

import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class for fixing Javadoc comments to conform to Oracle Javadoc conventions.
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
        compilationUnit = setup(compilationUnit);

        List<Comment> allComments = compilationUnit.getAllContainedComments();
        for (Comment comment : allComments) {
            if (comment instanceof JavadocComment) {
                JavadocComment javadoc = (JavadocComment) comment;
                String content = comment.getContent();
                // TODO how to determine the kind of Javadoc comment? The node it's attached to
                DocComment docComment = DocComment.parse(null, content);
                javadoc.setContent(docComment.toJava());
            }
        }

        return LexicalPreservingPrinter.print(compilationUnit);

      /*  DefaultPrinterConfiguration configuration = new DefaultPrinterConfiguration();
        configuration.addOption(new DefaultConfigurationOption(ConfigOption.END_OF_LINE_CHARACTER, "\n"));
        return compilationUnit.toString(configuration);*/
    }

    /**
     * Fixes Javadoc comments in the provided Java source file so that the first letter
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
     * Main method that applies Javadoc fixes to the file specified as the first command line argument.
     *
     * @param args command line arguments; the last argument should be the path to the file to fix
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
     * Prints only the changed lines between the original and fixed content, showing both old and new lines.
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
