package com.spandan.tests.ui;

import com.spandan.framework.config.ConfigManager;
import com.spandan.framework.ui.pages.InventoryPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

@Epic("Sauce Demo Web")
@Feature("Login")
public class LoginTest extends BaseUiTest {

    @Test(groups = {"ui", "smoke"})
    @Description("Standard user can log in and see the products page")
    public void validUserCanLogin() {
        InventoryPage inventory = loginPage()
                .loginAs(ConfigManager.get("ui.username"), ConfigManager.get("ui.password"));

        assertTrue(inventory.isLoaded(), "Products page should load after login");
    }

    @DataProvider(name = "invalidLogins")
    public Object[][] invalidLogins() {
        return new Object[][]{
                {ConfigManager.get("ui.locked.username"), ConfigManager.get("ui.password"), "locked out"},
                {"standard_user", "wrong_password", "do not match"},
                {"", "", "Username is required"},
                {"standard_user", "", "Password is required"}
        };
    }

    @Test(groups = {"ui", "regression"}, dataProvider = "invalidLogins")
    @Description("Invalid logins show the correct error message")
    public void invalidLoginShowsError(String username, String password, String expectedError) {
        String error = loginPage()
                .loginExpectingError(username, password)
                .getErrorMessage();

        assertTrue(error.contains(expectedError),
                "Expected error containing '" + expectedError + "' but got: " + error);
    }
}
