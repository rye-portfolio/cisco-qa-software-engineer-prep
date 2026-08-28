from dataclasses import dataclass
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from web_app_testing.models.login import LoginRequest
from web_app_testing.constants import LOGIN_REDIRECT_URL, LOGIN_URL
from web_app_testing.utils import assert_click_redirects, assert_click_redirects_to

@dataclass
class LoginPage:
    driver: WebDriver

    def __init__(self, driver: WebDriver) -> None:
        self.driver = driver
        if driver.current_url != LOGIN_URL:
            driver.get(LOGIN_URL)

    def __find_username_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "username")
    
    def __find_password_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "password")

    def __find_submit_button(self) -> WebElement:
        return self.driver.find_element(By.CSS_SELECTOR, 'button[type="submit"]')

    def try_log_in(self, login: LoginRequest) -> bool:
        self.__find_username_input().send_keys(login.username)
        self.__find_password_input().send_keys(login.password)
        return assert_click_redirects(self.__find_submit_button(), self.driver)

    def assert_log_in(self, login: LoginRequest) -> LoginPage:
        self.__find_username_input().send_keys(login.username)
        self.__find_password_input().send_keys(login.password)
        assert_click_redirects_to(self.__find_submit_button(), LOGIN_REDIRECT_URL, self.driver)
        return self
