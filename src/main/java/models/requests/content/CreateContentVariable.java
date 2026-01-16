package models.requests.content;

import java.util.HashMap;
import java.util.Map;

public class CreateContentVariable {
    public static Map<String, Object> variables(
        String title,
        String description,
        int order,
        String article,
        String articleType,
        String chapterId,
        int duration,
        boolean isRandomQuestion,
        String mediaId,
        String thumbnailUrl,
        String type
    ) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("title", title);
        inputData.put("description", description);
        inputData.put("order", order);
        inputData.put("article", article);
        inputData.put("articleType", articleType);
        inputData.put("chapterId", chapterId);
        inputData.put("duration", duration);
        inputData.put("isRandomQuestion", isRandomQuestion);
        inputData.put("mediaId", mediaId);
        inputData.put("thumbnailUrl", thumbnailUrl);
        // "type" is likely an enum in schema but treated as string here
        inputData.put("type", type);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputData);

        return variables;
    }
}
