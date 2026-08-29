from dataclasses import dataclass
from typing import Literal
from selenium.common import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from web_app_testing.models.login import LoginRequest
from web_app_testing.constants import LOGIN_REDIRECT_URL, LOGIN_URL
from web_app_testing.poms.base import BasePage
from web_app_testing.poms.order_page import OrderPage

@dataclass
class LoginPage(BasePage):
    page_url = LOGIN_URL

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)

    def find_username_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "username")
    
    def find_password_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "password")

    def __find_submit_button(self) -> WebElement:
        return self.driver.find_element(By.CSS_SELECTOR, 'button[type="submit"]')

    def __await_login_result(self) -> OrderPage | None | Literal[False]:
        if self.driver.current_url == LOGIN_REDIRECT_URL:
            return OrderPage(self.driver)
        if self.driver.find_elements(By.CSS_SELECTOR, "p#login-error"):
            return None
        return False

    def try_log_in(self, login: LoginRequest) -> OrderPage | None:
        self.find_username_input().send_keys(login.username)
        self.find_password_input().send_keys(login.password)
        return self.click_and_await(self.__find_submit_button(), lambda _ : self.__await_login_result())

    def log_in(self, login: LoginRequest) -> OrderPage:
        result = self.try_log_in(login)
        assert result != None
        return result
