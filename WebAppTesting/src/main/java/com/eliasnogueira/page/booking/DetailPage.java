package com.ryefry.page.booking;

import com.ryefry.page.booking.common.NavigationPage;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.ryefry.config.ConfigurationManager.configuration;

public class DetailPage extends NavigationPage {

    public DetailPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(css = "textarea#description")
    private WebElement roomDescription;

    @FindBy(css = "#message > p")
    private WebElement message;

    @Step
    public void fillRoomDescription(String description) {
        new WebDriverWait(driver, Duration.ofSeconds(configuration().timeout()))
                .until(ExpectedConditions.elementToBeClickable(roomDescription))
                .sendKeys(description);
    }

    @Step
    public String getAlertMessage() {
        return message.getText();
    }
}
