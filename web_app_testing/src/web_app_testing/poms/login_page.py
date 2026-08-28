from dataclasses import dataclass
from typing import Any
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from web_app_testing.models.login_details import LoginRequest
from web_app_testing.constants import PAGE_LOAD_TIMEOUT_SECONDS, LOGIN_PAGE_REDIRECT_URL

@dataclass
class LoginPage:
    driver: WebDriver

    def __init__(self, driver: WebDriver) -> None:
        self.driver = driver
        self.driver.get("http://localhost:8080/login")

    def find_username_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "username")
    
    def find_password_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "password")

    def find_submit_button(self) -> WebElement:
        return self.driver.find_element(By.CSS_SELECTOR, 'button[type="submit"]')

    def try_log_in(self, details: LoginRequest) -> bool:
        """Signs in as the given user.

        Args:
            details (LoginDetails): the user to sign in as
            driver (WebDriver): the driver to use
        """

        self.find_username_input().send_keys(details.username)
        self.find_password_input().send_keys(details.password)
        url = self.driver.current_url
        self.find_submit_button().click()
        WebDriverWait(self.driver, PAGE_LOAD_TIMEOUT_SECONDS).until(EC.url_changes(url))

        return self.driver.current_url == LOGIN_PAGE_REDIRECT_URL
    
    def assert_log_in(self, details: LoginRequest) -> None:
        assert self.try_log_in(details), f"Failed to log in: {details}"
