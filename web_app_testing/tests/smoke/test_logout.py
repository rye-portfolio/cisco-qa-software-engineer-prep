from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from pytest import mark
from web_app_testing.components import navbar
from web_app_testing.models.login import ADMIN, USER, LoginRequest
from web_app_testing.poms.login_page import LoginPage

@mark.parametrize("login", [ADMIN, USER])
def test_logging_out_as_builtin_user_returns_to_empty_login_page(driver: WebDriver, login: LoginRequest):
    LoginPage(driver).assert_log_in(login)

    # After clicking logout button and changing URLs, we should be back at the login screen.
    navbar.assert_log_out(driver)

    # Also, the input fields should be empty.
    assert not driver.find_element(By.ID, "username").get_attribute("value")
    assert not driver.find_element(By.ID, "password").get_attribute("value")
    