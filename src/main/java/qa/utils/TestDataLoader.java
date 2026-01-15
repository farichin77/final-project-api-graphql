package qa.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestDataLoader {

  public static String load(String path) {
    InputStream is = TestDataLoader.class
        .getClassLoader()
        .getResourceAsStream(path);

    if (is == null) {
      throw new RuntimeException("GraphQL file not found: " + path);
    }

    try {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read file: " + path, e);
    }
  }
}
