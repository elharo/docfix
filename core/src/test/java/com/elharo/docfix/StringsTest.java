package com.elharo.docfix;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringsTest {

  @Test
  public void testEndsWithURL_httpUrl() {
    assertTrue("Should detect HTTP URL", Strings.endsWithURL("Visit http://example.com"));
    assertTrue("Should detect HTTP URL with path", Strings.endsWithURL("Download from http://example.com/downloads"));
  }

  @Test
  public void testEndsWithURL_httpsUrl() {
    assertTrue("Should detect HTTPS URL", Strings.endsWithURL("See https://example.com"));
    assertTrue("Should detect HTTPS URL with path", Strings.endsWithURL("Documentation at https://example.com/docs/api"));
  }

  @Test
  public void testEndsWithURL_ftpUrl() {
    assertTrue("Should detect FTP URL", Strings.endsWithURL("Download from ftp://ftp.example.com"));
    assertTrue("Should detect FTP URL with path", Strings.endsWithURL("Files at ftp://ftp.example.com/files/"));
  }

  @Test
  public void testEndsWithURL_wwwUrl() {
    assertTrue("Should detect www URL", Strings.endsWithURL("Visit www.example.com"));
    // Subdomains without www. should not be detected as URLs
    assertFalse("Should not detect subdomain without www", Strings.endsWithURL("Check docs.example.com"));
  }

  @Test
  public void testEndsWithURL_mailtoUrl() {
    assertTrue("Should detect mailto URL", Strings.endsWithURL("Contact mailto:support@example.com"));
  }

  @Test
  public void testEndsWithURL_fileUrl() {
    assertTrue("Should detect file URL", Strings.endsWithURL("Local file://path/to/file"));
  }

  @Test
  public void testEndsWithURL_normalText() {
    assertFalse("Should not detect normal text", Strings.endsWithURL("This is normal text"));
    assertFalse("Should not detect text ending with period", Strings.endsWithURL("This ends with period."));
    assertFalse("Should not detect text with URL in middle", 
        Strings.endsWithURL("Visit https://example.com and then do something else"));
    assertFalse("Should not detect domain names without explicit schemes", 
        Strings.endsWithURL("Visit example.com"));
    assertFalse("Should not detect subdomains", 
        Strings.endsWithURL("Check docs.example.com"));
  }

  @Test
  public void testEndsWithURL_edgeCases() {
    assertFalse("Should handle null", Strings.endsWithURL(null));
    assertFalse("Should handle empty string", Strings.endsWithURL(""));
    assertFalse("Should handle whitespace only", Strings.endsWithURL("   "));
    assertTrue("Should handle URL with trailing whitespace", Strings.endsWithURL("Visit https://example.com   "));
  }

  @Test
  public void testEndsWithURL_multiLine() {
    assertTrue("Should detect URL at end of multiline text", 
        Strings.endsWithURL("File origin:\nhttps://github.com/example/repo/blob/main/src/File.java"));
    assertFalse("Should not detect URL not at end of multiline text", 
        Strings.endsWithURL("Visit https://example.com\nand then do something else"));
  }
}