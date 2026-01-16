package models.requests.division;

import java.util.HashMap;
import java.util.Map;

public class CreateDivisionVariable {
    public static Map<String, Object> variables(String name, String description) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("name", name);
        inputData.put("description", description);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputData);

        return variables;
    }
}
