package com.ryefry.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.Objects;

import static com.ryefry.config.ConfigurationManager.configuration;
import static org.openqa.selenium.support.PageFactory.initElements;

public class AbstractPageObject {

    protected final WebDriver driver;

    protected AbstractPageObject(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver must not be null");
        initElements(new AjaxElementLocatorFactory(this.driver, configuration().timeout()), this);
    }
}
