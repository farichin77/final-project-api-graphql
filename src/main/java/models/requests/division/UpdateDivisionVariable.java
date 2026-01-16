package models.requests.division;

import java.util.HashMap;
import java.util.Map;

public class UpdateDivisionVariable {
    public static Map<String, Object> variables(String id, String name, String description) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("name", name);
        inputData.put("description", description);

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", id);
        variables.put("input", inputData);

        return variables;
    }
}
