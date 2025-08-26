package com.elharo.docfix;

import org.junit.Test;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Tests for the EncodingDetector class.
 */
public class EncodingDetectorTest {

  @Test
  public void testDetectUTF8Encoding() throws IOException {
    // Test with the existing ComplexNumber.java file which should be UTF-8
    Path file = Paths.get("src/test/resources/com/elharo/math/ComplexNumber.java");
    Charset detected = EncodingDetector.detectEncoding(file);
    
    // Should detect UTF-8 for most modern Java files
    assertEquals(StandardCharsets.UTF_8, detected);
  }

  @Test
  public void testDetectEncodingOfEmptyFile() throws IOException {
    // Create a temporary empty file
    Path tempFile = Files.createTempFile("empty", ".java");
    try {
      Charset detected = EncodingDetector.detectEncoding(tempFile);
      // Should default to UTF-8 for empty files
      assertEquals(StandardCharsets.UTF_8, detected);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testDetectEncodingWithBOM() throws IOException {
    // Create a file with UTF-8 BOM
    Path tempFile = Files.createTempFile("utf8bom", ".java");
    try {
      byte[] content = {
          (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, // UTF-8 BOM
          'p', 'a', 'c', 'k', 'a', 'g', 'e', ' ', 't', 'e', 's', 't', ';'
      };
      Files.write(tempFile, content);
      
      Charset detected = EncodingDetector.detectEncoding(tempFile);
      assertEquals(StandardCharsets.UTF_8, detected);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testDetectEncodingWithUTF16LEBOM() throws IOException {
    // Create a file with UTF-16 LE BOM
    Path tempFile = Files.createTempFile("utf16le", ".java");
    try {
      byte[] content = {
          (byte) 0xFF, (byte) 0xFE, // UTF-16 LE BOM
          'p', 0, 'a', 0, 'c', 0, 'k', 0
      };
      Files.write(tempFile, content);
      
      Charset detected = EncodingDetector.detectEncoding(tempFile);
      assertEquals(StandardCharsets.UTF_16LE, detected);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testDetectEncodingWithUTF16BEBOM() throws IOException {
    // Create a file with UTF-16 BE BOM
    Path tempFile = Files.createTempFile("utf16be", ".java");
    try {
      byte[] content = {
          (byte) 0xFE, (byte) 0xFF, // UTF-16 BE BOM
          0, 'p', 0, 'a', 0, 'c', 0, 'k'
      };
      Files.write(tempFile, content);
      
      Charset detected = EncodingDetector.detectEncoding(tempFile);
      assertEquals(StandardCharsets.UTF_16BE, detected);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testDetectEncodingJavaKeywords() throws IOException {
    // Create a file with Java keywords in UTF-8
    Path tempFile = Files.createTempFile("javakeywords", ".java");
    try {
      String javaContent = "package test;\n\npublic class Test {\n    private void method() {}\n}";
      Files.write(tempFile, javaContent.getBytes(StandardCharsets.UTF_8));
      
      Charset detected = EncodingDetector.detectEncoding(tempFile);
      assertEquals(StandardCharsets.UTF_8, detected);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testDetectEncodingJavadocComment() throws IOException {
    // Create a file with Javadoc comment
    Path tempFile = Files.createTempFile("javadoc", ".java");
    try {
      String javaContent = "/**\n * Test class\n */\npublic class Test {}";
      Files.write(tempFile, javaContent.getBytes(StandardCharsets.UTF_8));
      
      Charset detected = EncodingDetector.detectEncoding(tempFile);
      assertEquals(StandardCharsets.UTF_8, detected);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testDetectEncodingDefaultsToUTF8() throws IOException {
    // Create a file with no recognizable Java content
    Path tempFile = Files.createTempFile("nonjava", ".java");
    try {
      String content = "This is not Java content but still a valid text file.";
      Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
      
      Charset detected = EncodingDetector.detectEncoding(tempFile);
      // Should still detect or default to UTF-8
      assertEquals(StandardCharsets.UTF_8, detected);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}