package models.requests.chapter;

import java.util.HashMap;
import java.util.Map;

public class UpdateChapterVariable {
    public static Map<String, Object> variables(
        String id,
        String title,
        String description,
        int order,
        String programId
    ) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("title", title);
        inputData.put("description", description);
        inputData.put("order", order);
        inputData.put("programId", programId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", id);
        variables.put("input", inputData);

        return variables;
    }
}
