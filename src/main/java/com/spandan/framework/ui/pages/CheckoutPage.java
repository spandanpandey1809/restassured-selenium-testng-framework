package com.spandan.framework.ui.pages;

import com.spandan.framework.ui.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Covers checkout step one (details), step two (overview) and the confirmation page. */
public class CheckoutPage extends BasePage {

    private final By firstNameInput = By.cssSelector("[data-test='firstName']");
    private final By lastNameInput = By.cssSelector("[data-test='lastName']");
    private final By postalCodeInput = By.cssSelector("[data-test='postalCode']");
    private final By continueButton = By.cssSelector("[data-test='continue']");
    private final By finishButton = By.cssSelector("[data-test='finish']");
    private final By errorMessage = By.cssSelector("[data-test='error']");
    private final By completeHeader = By.cssSelector("[data-test='complete-header']");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    @Step("Enter checkout details")
    public CheckoutPage enterDetails(String firstName, String lastName, String postalCode) {
        type(firstNameInput, firstName);
        type(lastNameInput, lastName);
        type(postalCodeInput, postalCode);
        click(continueButton);
        return this;
    }

    @Step("Finish order")
    public CheckoutPage finish() {
        click(finishButton);
        return this;
    }

    public String getConfirmationMessage() {
        return getText(completeHeader);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}
