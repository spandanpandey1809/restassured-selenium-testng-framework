package com.spandan.tests.api;

import com.spandan.framework.config.ConfigManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.nullValue;

@Epic("Booking API")
@Feature("Authentication")
public class AuthTest extends BaseApiTest {

    @Test(groups = {"api", "smoke"})
    @Description("Valid credentials return a non-empty auth token")
    public void validCredentialsReturnToken() {
        Response response = bookingService.createToken(
                ConfigManager.get("api.username"), ConfigManager.get("api.password"));

        response.then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"admin", "wrong-password"},
                {"unknown-user", "password123"},
                {"", ""}
        };
    }

    @Test(groups = {"api", "regression"}, dataProvider = "invalidCredentials")
    @Description("Invalid credentials do not return a token")
    public void invalidCredentialsAreRejected(String username, String password) {
        // Note: this API returns HTTP 200 with a "Bad credentials" reason instead of 401.
        bookingService.createToken(username, password)
                .then()
                .body("token", nullValue())
                .body("reason", equalTo("Bad credentials"));
    }
}
