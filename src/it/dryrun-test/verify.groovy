import java.io.*;
import java.nio.file.*;

try {
    Path testFile = Paths.get(basedir.toString(), "src/main/java/com/example/TestClass.java");
    String content = Files.readString(testFile);
    
    // Check that file was NOT modified in dry-run mode
    if (!content.contains("test class with javadoc issues")) {
        throw new FileNotFoundException("Expected original 'test class with javadoc issues' but was modified");
    }
    
    // Should still have lowercase because dry-run doesn't modify files
    if (content.contains("Test class with javadoc issues")) {
        throw new FileNotFoundException("File was modified during dry-run, but it should not have been");
    }
    
    return true;
} catch (Exception e) {
    e.printStackTrace();
    return false;
}