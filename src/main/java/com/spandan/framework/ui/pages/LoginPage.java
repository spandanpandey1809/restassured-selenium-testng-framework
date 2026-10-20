package com.spandan.framework.ui.pages;

import com.spandan.framework.config.ConfigManager;
import com.spandan.framework.ui.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private final By usernameInput = By.id("user-name");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open login page")
    public LoginPage open() {
        driver.get(ConfigManager.get("ui.base.url"));
        waitForVisible(loginButton);
        return this;
    }

    @Step("Log in as {username}")
    public InventoryPage loginAs(String username, String password) {
        submitCredentials(username, password);
        return new InventoryPage(driver);
    }

    @Step("Submit credentials expecting an error")
    public LoginPage loginExpectingError(String username, String password) {
        submitCredentials(username, password);
        return this;
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    private void submitCredentials(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        click(loginButton);
    }
}
