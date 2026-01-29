package models.requests.training;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignProgramVariable {
    public static Map<String, Object> variables(
        List<String> employeeIds,
        String programId,
        String startDate,
        String endDate
    ) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("employeeIds", employeeIds);
        inputData.put("programId", programId);
        inputData.put("startDate", startDate);
        inputData.put("endDate", endDate);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputData);

        return variables;
    }
}
