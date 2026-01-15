package models.requests.employee;

import java.util.HashMap;
import java.util.Map;

public class DeleteEmployeeVariable {
    public static Map<String, Object> variables(String id) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", id);
        return variables;
    }
}
