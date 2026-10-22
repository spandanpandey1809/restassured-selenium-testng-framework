package com.spandan.tests.ui;

import com.spandan.framework.ui.DriverFactory;
import com.spandan.framework.ui.pages.LoginPage;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;

/** Fresh browser per test method keeps tests independent and safe to run in parallel. */
public abstract class BaseUiTest {

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverFactory.initDriver();
    }

    /*
     * The failure screenshot is taken here, before the browser quits, rather than in a TestNG
     * listener: depending on version, a listener's onTestFailure can run after @AfterMethod,
     * when the driver is already gone.
     */
    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE && DriverFactory.hasDriver()) {
                attachScreenshot(result.getName());
            }
        } finally {
            DriverFactory.quitDriver();
        }
    }

    protected WebDriver driver() {
        return DriverFactory.getDriver();
    }

    protected LoginPage loginPage() {
        return new LoginPage(driver()).open();
    }

    private void attachScreenshot(String testName) {
        try {
            byte[] png = ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot on failure - " + testName, "image/png",
                    new ByteArrayInputStream(png), "png");
        } catch (RuntimeException e) {
            // A failed screenshot must never hide the real test failure.
            System.err.println("Could not capture screenshot: " + e.getMessage());
        }
    }
}
