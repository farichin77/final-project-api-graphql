package models.requests.chapter;

import java.util.HashMap;
import java.util.Map;

public class CreateChapterVariable {
    public static Map<String, Object> variables(
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
        variables.put("input", inputData);

        return variables;
    }
}
