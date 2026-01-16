package models.requests.login;

import java.util.Map;

public class LoginVariables {
    public final String email;
    public final String password;
    public final String companyId;

    public LoginVariables(String email, String password, String companyId) {
        this.email = email;
        this.password = password;
        this.companyId = companyId;
    }

    public Map<String, Object> toMap() {
        return Map.of(
            "usernameOrEmail", email,  // Changed from "email" to "usernameOrEmail" to match GraphQL query
            "password", password,
            "companyId", companyId
        );
    }
}
