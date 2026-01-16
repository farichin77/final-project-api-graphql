package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonHelper {

    public static String getIdFromJson(String filePath) {
        try {
            String content = readJsonFile(filePath);
            if (content == null) return null;

            if (content.startsWith("[") && content.endsWith("]")) {
                // Array format
                return extractIdFromLastObjectInArray(content);
            } else if (content.startsWith("{") && content.endsWith("}")) {
                // Object format
                return extractIdFromObject(content);
            }
        } catch (Exception e) {
            System.out.println("⚠ Failed to read ID from JSON (" + filePath + "): " + e.getMessage());
        }
        return null;
    }

    public static String getLatestIdFromJson(String filePath) {
        try {
            String content = readJsonFile(filePath);
            if (content == null) return null;

            if (content.startsWith("[") && content.endsWith("]")) {
                String arrayContent = content.substring(1, content.length() - 1);
                // Split by }, { handling potential spaces
                String[] objects = arrayContent.split("(?<=\\}),\\s*(?=\\{)");
                
                String latestId = null;
                long latestTimestamp = 0;

                for (String obj : objects) {
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}")) obj = obj + "}";

                    String id = extractIdFromObject(obj);
                    long timestamp = extractTimestampFromObject(obj);

                    if (timestamp > latestTimestamp) {
                        latestTimestamp = timestamp;
                        latestId = id;
                    }
                }
                if (latestId == null && objects.length > 0) {
                     // Fallback to last object if timestamps fail or missing
                     return extractIdFromObject(objects[objects.length-1]);
                }
                return latestId;

            } else if (content.startsWith("{") && content.endsWith("}")) {
                return extractIdFromObject(content);
            }
        } catch (Exception e) {
            System.out.println("⚠ Failed to read latest ID from JSON (" + filePath + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Reads the Latest ID from a JSON file that matches a specific parent ID (e.g. ProgramId).
     */
    public static String getLatestIdByParentId(String filePath, String parentField, String parentId) {
        try {
            String content = readJsonFile(filePath);
            if (content == null) return null;

            String latestId = null;
            long latestTimestamp = 0;

            if (content.startsWith("[") && content.endsWith("]")) {
               String arrayContent = content.substring(1, content.length() - 1);
               String[] objects = arrayContent.split("(?<=\\}),\\s*(?=\\{)");

               for (String obj : objects) {
                   if (!obj.startsWith("{")) obj = "{" + obj;
                   if (!obj.endsWith("}")) obj = obj + "}";

                   String currentParentId = extractValueByKey(obj, parentField);
                   if (parentId.equals(currentParentId)) {
                       String id = extractIdFromObject(obj);
                       long timestamp = extractTimestampFromObject(obj);
                       
                       if (timestamp > latestTimestamp) {
                           latestTimestamp = timestamp;
                           latestId = id;
                       } else if (latestId == null) {
                           latestId = id;
                       }
                   }
               }
            }
            return latestId;
        } catch (Exception e) {
             System.out.println("⚠ Failed to read latest ID by parent from JSON (" + filePath + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Reads all IDs from a JSON file that match a specific parent ID.
     */
    public static java.util.List<String> getIdsByParentId(String filePath, String parentField, String parentId) {
        java.util.List<String> ids = new java.util.ArrayList<>();
        try {
            String content = readJsonFile(filePath);
            if (content == null) return ids;

            if (content.startsWith("[") && content.endsWith("]")) {
               String arrayContent = content.substring(1, content.length() - 1);
               if (arrayContent.isEmpty()) return ids;
               
               String[] objects = arrayContent.split("(?<=\\}),\\s*(?=\\{)");

               for (String obj : objects) {
                   if (!obj.startsWith("{")) obj = "{" + obj;
                   if (!obj.endsWith("}")) obj = obj + "}";

                   String currentParentId = extractValueByKey(obj, parentField);
                   if (parentId.equals(currentParentId)) {
                       String id = extractIdFromObject(obj);
                       if (id != null) {
                           ids.add(id);
                       }
                   }
               }
            }
        } catch (Exception e) {
             System.out.println("⚠ Failed to read IDs by parent from JSON (" + filePath + "): " + e.getMessage());
        }
        return ids;
    }

    /**
     * Reads an ID from a JSON file that matches a specific field value (e.g. name or title).
     */
    public static String getIdByFieldValue(String filePath, String field, String value) {
        String result = null;
        try {
            String content = readJsonFile(filePath);
            if (content == null) return null;

            if (content.startsWith("[") && content.endsWith("]")) {
               String arrayContent = content.substring(1, content.length() - 1);
               if (arrayContent.isEmpty()) return null;
               
               String[] objects = arrayContent.split("(?<=\\}),\\s*(?=\\{)");

               for (String obj : objects) {
                   if (!obj.startsWith("{")) obj = "{" + obj;
                   if (!obj.endsWith("}")) obj = obj + "}";

                   String currentValue = extractValueByKey(obj, field);
                   // Check both "name" and "title" if searching for one of them
                   if (currentValue == null && "name".equals(field)) {
                       currentValue = extractValueByKey(obj, "title");
                   }
                   
                   if (value.equals(currentValue)) {
                       result = extractIdFromObject(obj);
                   }
               }
            }
        } catch (Exception e) {
             System.out.println("⚠ Failed to read ID by field value from JSON (" + filePath + "): " + e.getMessage());
        }
        return result;
    }

    public static void saveIdToJson(String filePath, String id, String name, String parentField, String parentId) {
         try {
             String existingJson = readJsonFile(filePath);
             if (existingJson == null || existingJson.isEmpty()) existingJson = "[]";

             long timestamp = System.currentTimeMillis();
             StringBuilder newJson = new StringBuilder();
             newJson.append("{\"id\":\"").append(id).append("\"");
             if (name != null) newJson.append(",\"name\":\"").append(name).append("\"");
             if (parentField != null && parentId != null) newJson.append(",\"").append(parentField).append("\":\"").append(parentId).append("\"");
             newJson.append(",\"timestamp\":").append(timestamp).append("}");

             String finalJson;
             if (existingJson.equals("[]")) {
                 finalJson = "[" + newJson.toString() + "]";
             } else if (existingJson.startsWith("[") && existingJson.endsWith("]")) {
                 finalJson = existingJson.substring(0, existingJson.length() - 1) + "," + newJson.toString() + "]";
             } else {
                 finalJson = "[" + existingJson + "," + newJson.toString() + "]";
             }

             try (FileWriter writer = new FileWriter(filePath)) {
                 writer.write(finalJson);
             }
         } catch (IOException e) {
             System.out.println("⚠ Failed to save ID to JSON (" + filePath + "): " + e.getMessage());
         }
    }

    // --- Private Helper Methods ---

    private static String readJsonFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString().trim();
        } catch (IOException e) {
            return null; // Return null if file not found
        }
    }

    private static String extractIdFromLastObjectInArray(String arrayContent) {
        String inner = arrayContent.substring(1, arrayContent.length() - 1);
        if (inner.isEmpty()) return null;
        String[] objects = inner.split("(?<=\\}),\\s*(?=\\{)");
        return extractIdFromObject(objects[objects.length - 1]);
    }

    private static String extractIdFromObject(String jsonObject) {
        return extractValueByKey(jsonObject, "id");
    }

    private static long extractTimestampFromObject(String jsonObject) {
        String val = extractValueByKey(jsonObject, "timestamp");
        if (val != null) {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private static String extractValueByKey(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;
        start += searchKey.length();
        
        char firstChar = json.charAt(start);
        if (firstChar == '"') {
            start++;
            int end = json.indexOf("\"", start);
            if (end != -1) return json.substring(start, end);
        } else {
            // Number or boolean
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end != -1) return json.substring(start, end).trim();
        }
        return null;
    }
}
