package com.ryefry.report;

import com.ryefry.driver.DriverManager;
import io.qameta.allure.Attachment;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import static io.qameta.allure.model.Status.BROKEN;
import static io.qameta.allure.model.Status.FAILED;

/*
 * Approach implemented using the https://github.com/biczomate/allure-testng7.5-attachment-example as reference
 */
public class AllureTestLifecycleListener implements TestLifecycleListener {

    public AllureTestLifecycleListener() {
    }

    @Attachment(value = "Page Screenshot", type = "image/png")
    public byte[] saveScreenshot(WebDriver driver) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return new byte[0];
        }

        try {
            return screenshotDriver.getScreenshotAs(OutputType.BYTES);
        } catch (WebDriverException exception) {
            return new byte[0];
        }
    }

    @Override
    public void beforeTestStop(TestResult result) {
        if (result.getStatus() != FAILED && result.getStatus() != BROKEN) {
            return;
        }

        WebDriver driver = DriverManager.getDriver();

        if (driver != null) {
            saveScreenshot(driver);
        }
    }
}
