package com.ryefry;

import com.ryefry.driver.DriverManager;
import com.ryefry.driver.TargetFactory;
import com.ryefry.report.AllureManager;
import com.ryefry.report.AllureTestLifecycleListener;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.ITestResult;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import static com.ryefry.config.ConfigurationManager.configuration;

public abstract class BaseWeb {

    protected WebDriver driver;

    @BeforeSuite
    public void beforeSuite() {
        AllureManager.setAllureEnvironmentInformation();
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void preCondition(@Optional("chrome") String browser) {
        driver = new TargetFactory().createInstance(browser);
        DriverManager.setDriver(driver);

        driver.get(configuration().url());
    }

    @AfterMethod(alwaysRun = true)
    public void postCondition(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE && driver != null) {
                new AllureTestLifecycleListener().saveScreenshot(driver);
            }
        } finally {
            DriverManager.quit();
            driver = null;
        }
    }
}
