package utils;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GraphQlFileReader {
    public static String readMutationAuth(String fileName) {
        return readFile("graphql/mutations/auth/" + fileName);
    }

    public static String readMutationEmployee(String fileName) {
        return readFile("graphql/mutations/employee/" + fileName);
    }


    public static String readMutationTraining(String fileName) {
        return readFile("graphql/mutations/training/" + fileName);
    }
    
    public static String readQuery(String fileName) {
        return readFile("graphql/queries/" + fileName);
    }
    
    private static String readFile(String path) {
        try (InputStream inputStream = GraphQlFileReader.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new RuntimeException("GraphQL file not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read GraphQL file: " + path, e);
        }
    }
}
