package models.requests.training;

import java.util.HashMap;
import java.util.Map;

public class CreateTrainingVariable {
    public static Map<String, Object> variables(
        String title,
        String description,
        String type,
        boolean isSequential
    ) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("title", title);
        inputData.put("description", description);
        inputData.put("type", type);
        inputData.put("isSequential", isSequential);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputData);

        return variables;
    }
}
