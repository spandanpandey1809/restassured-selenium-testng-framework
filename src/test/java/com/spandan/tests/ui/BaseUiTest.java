package com.spandan.tests.ui;

import com.spandan.framework.ui.DriverFactory;
import com.spandan.framework.ui.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Fresh browser per test method keeps tests independent and safe to run in parallel. */
public abstract class BaseUiTest {

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverFactory.initDriver();
    }

    // Runs after the listener's onTestFailure, so the screenshot is taken before quit.
    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        DriverFactory.quitDriver();
    }

    protected WebDriver driver() {
        return DriverFactory.getDriver();
    }

    protected LoginPage loginPage() {
        return new LoginPage(driver()).open();
    }
}
