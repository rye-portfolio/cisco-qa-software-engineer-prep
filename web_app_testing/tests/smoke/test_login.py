from pytest import mark
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from web_app_testing.models.login import ADMIN, USER, LoginRequest
from web_app_testing.poms.login_page import LoginPage

def test_login_page_title_is_login(driver: WebDriver):
    """Opens the login page and asserts the title is 'Login'."""
    LoginPage(driver)
    assert driver.title == "Login"

@mark.parametrize("login", [ADMIN, USER])
def test_logging_in_as_builtin_user_with_right_password_redirects_to_orders_page(driver: WebDriver, login: LoginRequest):
    """Opens the login page and asserts that logging in as 'admin' or 'user' succeeds."""
    LoginPage(driver).log_in(login)

@mark.parametrize("login", [
    LoginRequest(USER.username, "wrongpassword123"),
    LoginRequest(ADMIN.username, "wrongpassword123"),
    LoginRequest(ADMIN.username, ""),
    LoginRequest("wrongusername", ADMIN.password),
    LoginRequest("wrongusername", "wrongpassword123"),
    LoginRequest("", ADMIN.password),
    LoginRequest("", ""),
])
def test_logging_in_as_builtin_user_with_wrong_password_fails(driver: WebDriver, login: LoginRequest):
    assert not LoginPage(driver).try_log_in(login)
    assert "Invalid username or password" in driver.find_element(By.CSS_SELECTOR, "p#login-error").text
    