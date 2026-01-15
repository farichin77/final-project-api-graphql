package models.requests.employee;

import java.util.HashMap;
import java.util.Map;

public class AddEmployeeVariable {
  public static Map<String, Object> variables(
      String name,
      String employeeId,
      String email,
      String phoneNumber,
      String divisionId,
      String employeeRole,
      int angkatanId,
      String gender,
      String dateOfBirth,
      String address,
      String nik,
      String npwp
  ) {
    Map<String, Object> inputData = new HashMap<>();
    inputData.put("name", name);
    inputData.put("employeeId", employeeId);
    inputData.put("email", email);
    inputData.put("phoneNumber", phoneNumber);
    inputData.put("divisionId", divisionId);
    inputData.put("employeeRole", employeeRole);
    inputData.put("angkatanId", angkatanId);
    inputData.put("gender", gender);
    inputData.put("dateOfBirth", dateOfBirth);
    inputData.put("address", address);
    inputData.put("nik", nik);
    inputData.put("npwp", npwp);

    // Wrap it in the variables map
    Map<String, Object> inputVariables = new HashMap<>();
    inputVariables.put("input", inputData);

    return inputVariables;
  }
}
