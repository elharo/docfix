package com.elharo.docfix;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
// ChatGPT put this in the wrong package intially so the code wouldn't compile.
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for fixing Javadoc comments to conform to Oracle Javadoc conventions.
 */
public class DocFix {

    /**
     * Fixes Javadoc comments in the provided code string so that the first letter
     * of each doc comment is lower case.
     *
     * @param code the source code containing Javadoc comments
     * @return the fixed source code
     */
    public static String fix(String code) {
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(code).getResult().orElse(null);
        if (cu == null) return code;

        cu.getAllContainedComments().forEach(comment -> {
            if (comment instanceof JavadocComment) {
                JavadocComment javadoc = (JavadocComment) comment;
                String content = javadoc.getContent();
                String[] lines = content.split("\r?\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    String trimmed = line.trim();
                    if (trimmed.startsWith("* ") && trimmed.length() > 2) {
                        char first = trimmed.charAt(2);
                        if (Character.isUpperCase(first)) {
                            // Lowercase the first letter after '* '
                            lines[i] = line.replaceFirst("(\\* )([A-Z])", "$1" + Character.toLowerCase(first));
                        }
                    }
                }
                String newContent = String.join("\n", lines);
                javadoc.setContent(newContent);
            }
        });
        PrettyPrinterConfiguration conf = new PrettyPrinterConfiguration();
        conf.setEndOfLineCharacter("\n");
        return cu.toString(conf);
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
     * @param args command line arguments; args[0] should be the path to the file to fix
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java DocFix <file-or-directory>");
            System.exit(1);
        }
        Path path = java.nio.file.Paths.get(args[0]);
        if (Files.isDirectory(path)) {
            Files.walk(path, 3)
                .filter(p -> !Files.isSymbolicLink(p))
                .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                .forEach(p -> {
                    try {
                        fix(p);
                    } catch (Exception e) {
                        System.err.println("Failed to fix: " + p + ", " + e.getMessage());
                    }
                });
        } else {
            fix(path);
        }
    }
}
