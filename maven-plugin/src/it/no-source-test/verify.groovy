import java.io.*;

// This test verifies that the plugin handles missing source directory gracefully
// The build should succeed without throwing an exception

File buildLog = new File(basedir, "build.log");
if (buildLog.exists()) {
    String log = buildLog.text;
    // Should contain a warning about non-existent source directory
    if (!log.contains("Source directory does not exist")) {
        throw new FileNotFoundException("Expected warning about non-existent source directory");
    }
}

return true;