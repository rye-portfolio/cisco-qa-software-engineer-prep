package com.ryefry.page.booking.common;

import com.ryefry.page.AbstractPageObject;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class NavigationPage extends AbstractPageObject {

    protected NavigationPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(name = "next")
    private WebElement next;

    @FindBy(name = "previous")
    private WebElement previous;

    @FindBy(name = "finish")
    private WebElement finish;

    @Step
    public void next() {
        next.click();
    }

    @Step
    public void finish() {
        finish.click();
    }
}
