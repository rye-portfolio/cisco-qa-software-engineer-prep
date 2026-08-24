package com.ryefry.page.booking;

import com.ryefry.page.booking.common.NavigationPage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RoomPage extends NavigationPage {

    public RoomPage(WebDriver driver) {
        super(driver);
    }

    @Step
    public void selectRoomType(String room) {
        driver.findElement(By.xpath("//h6[text()='" + room + "']")).click();
    }
}
