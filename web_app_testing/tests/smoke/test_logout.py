from selenium.webdriver.remote.webdriver import WebDriver
from pytest import mark
from web_app_testing.models.login import ADMIN, USER, LoginRequest
from web_app_testing.poms.login_page import LoginPage

@mark.parametrize("login", [ADMIN, USER])
def test_logging_out_as_builtin_user_returns_to_empty_login_page(driver: WebDriver, login: LoginRequest):
    order_page = LoginPage(driver).log_in(login)

    # After clicking logout button and changing URLs, we should be back at the login screen.
    login_page = order_page.navbar().log_out()

    assert login_page, "Failed to log out"

    # Also, the input fields should be empty.
    assert not login_page.find_username_input().get_attribute("value")
    assert not login_page.find_password_input().get_attribute("value")
    