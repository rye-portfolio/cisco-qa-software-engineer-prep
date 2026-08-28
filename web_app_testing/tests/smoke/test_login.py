from pytest import mark
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from web_app_testing.models.login_details import ADMIN, USER, LoginRequest
from web_app_testing.poms.login_page import LoginPage

def test_login_page_title_is_login(driver: WebDriver):
    # Self-explanatory...
    LoginPage(driver)
    assert driver.title == "Login"

@mark.parametrize("login", [ADMIN, USER])
def test_logging_in_as_builtin_user_with_right_password_redirects_to_orders_page(driver: WebDriver, login: LoginRequest):
    LoginPage(driver).assert_log_in(login)

@mark.parametrize("login", [
    LoginRequest("admin", "admin"), LoginRequest("user", "admin"),
    LoginRequest("admin", "pass"), LoginRequest("user", "pass"),
    LoginRequest("admin", "1234"), LoginRequest("user", "1234"),
])
def test_logging_in_as_builtin_user_with_wrong_password_fails(driver: WebDriver, login: LoginRequest):
    assert not LoginPage(driver).try_log_in(login)
    assert driver.current_url.endswith("/login?error")
    assert driver.title == "Login"
    assert "Invalid username or password" in driver.find_element(By.CSS_SELECTOR, "p#login-error").text
    