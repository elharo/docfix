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

        List<Comment> allComments = compilationUnit.getAllContainedComments();
        for (Comment comment : allComments) {
            if (comment instanceof JavadocComment) {
                JavadocComment javadoc = (JavadocComment) comment;
                String content = comment.getContent();
                // TODO how to determine the kind of Javadoc comment?
                DocComment docComment = DocComment.parse(null, content);
                String[] lines = content.split("\r?\n", -1); // preserve trailing empty lines
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    String trimmed = line.trim();
                    // Only lowercase the first letter after @param, @return, @throws, etc. if it is uppercase
                    if (trimmed.matches("\\* +@\\w+ +\\w+ +[A-Z].*")) {
                        // Find the start of the tag value (after tag and param name)
                        int atIdx = line.indexOf('@');
                        if (atIdx != -1) {
                            int tagEnd = line.indexOf(' ', atIdx); // after @param
                            if (tagEnd != -1) {
                                int paramStart = tagEnd + 1;
                                // skip spaces
                                while (paramStart < line.length() && line.charAt(paramStart) == ' ') paramStart++;
                                // skip param name
                                while (paramStart < line.length() && !Character.isWhitespace(line.charAt(paramStart))) paramStart++;
                                // skip spaces before value
                                while (paramStart < line.length() && Character.isWhitespace(line.charAt(paramStart))) paramStart++;
                                if (paramStart < line.length()) {
                                    char c = line.charAt(paramStart);
                                    if (Character.isUpperCase(c)) {
                                        StringBuilder sb = new StringBuilder(line);
                                        sb.setCharAt(paramStart, Character.toLowerCase(c));
                                        lines[i] = sb.toString();
                                    }
                                }
                            }
                        }
                    } else if (trimmed.startsWith("* ") && trimmed.length() > 2 && !trimmed.startsWith("* @")) {
                        char first = trimmed.charAt(2);
                        if (Character.isUpperCase(first)) {
                            lines[i] = line.replaceFirst("(\\* )([A-Z])", "$1" + Character.toLowerCase(first));
                        }
                    }
                }
                String newContent = String.join("\n", lines);
                javadoc.setContent(newContent);
            }
        }

        DefaultPrinterConfiguration configuration = new DefaultPrinterConfiguration();
        configuration.addOption(new DefaultConfigurationOption(ConfigOption.END_OF_LINE_CHARACTER, "\n"));
        return compilationUnit.toString(configuration);
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
