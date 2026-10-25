package com.spandan.framework.ui;

import com.spandan.framework.config.ConfigManager;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * Creates one WebDriver per thread so UI tests can run in parallel safely.
 *
 * Local run: Selenium Manager (built into Selenium 4) downloads the matching driver automatically.
 * Grid run:  set -Dgrid.url=http://host:4444 and the same browser options are sent to Selenium Grid
 *            through RemoteWebDriver (see docker-compose.yml).
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static void initDriver() {
        String browser = ConfigManager.get("browser").toLowerCase();
        boolean headless = ConfigManager.getBoolean("headless");
        String gridUrl = ConfigManager.get("grid.url");

        MutableCapabilities options = browserOptions(browser, headless);
        WebDriver driver = gridUrl.isEmpty() ? localDriver(browser, options) : remoteDriver(gridUrl, options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicit.wait.seconds")));
        DRIVER.set(driver);
    }

    private static MutableCapabilities browserOptions(String browser, boolean headless) {
        return switch (browser) {
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                yield options;
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (headless) options.addArguments("--headless=new");
                yield options;
            }
            default -> {
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage");
                // The demo site's public test passwords trigger Chrome's "password found in a data breach"
                // and "save password" pop-ups, which block clicks. Turn the password manager off.
                options.addArguments("--disable-features=PasswordLeakDetection");
                options.setExperimentalOption("prefs", Map.of(
                        "credentials_enable_service", false,
                        "profile.password_manager_enabled", false,
                        "profile.password_manager_leak_detection", false));
                yield options;
            }
        };
    }

    private static WebDriver localDriver(String browser, MutableCapabilities options) {
        return switch (browser) {
            case "firefox" -> new FirefoxDriver((FirefoxOptions) options);
            case "edge" -> new EdgeDriver((EdgeOptions) options);
            default -> new ChromeDriver((ChromeOptions) options);
        };
    }

    private static WebDriver remoteDriver(String gridUrl, MutableCapabilities options) {
        try {
            return new RemoteWebDriver(new URL(gridUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid grid.url: " + gridUrl, e);
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialised for this thread. Call initDriver() first.");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
