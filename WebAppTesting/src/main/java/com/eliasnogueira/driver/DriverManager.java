package com.ryefry.driver;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

public class DriverManager {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void setDriver(WebDriver driver) {
        DriverManager.driver.set(driver);
    }

    public static void quit() {
        WebDriver currentDriver = driver.get();

        try {
            if (currentDriver != null) {
                currentDriver.quit();
            }
        } finally {
            driver.remove();
        }
    }

    public static String getInfo() {
        WebDriver currentDriver = getDriver();

        if (!(currentDriver instanceof HasCapabilities hasCapabilities)) {
            throw new IllegalStateException("Current driver does not expose capabilities");
        }

        Capabilities capabilities = hasCapabilities.getCapabilities();

        return "browser: %s v: %s platform: %s".formatted(
                capabilities.getBrowserName(),
                capabilities.getBrowserVersion(),
                capabilities.getPlatformName());
    }
}
