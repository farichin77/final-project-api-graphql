package models.requests.employee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferEmployeeVariable {
    public static Map<String, Object> variables(
        List<String> employeeIds,
        String divisionId
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeIds", employeeIds);
        variables.put("divisionId", divisionId);

        return variables;
    }
}
