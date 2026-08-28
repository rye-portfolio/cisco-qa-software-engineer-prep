from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from pytest import mark
from web_app_testing.components import navbar
from web_app_testing.constants import PAGE_LOAD_TIMEOUT_SECONDS
from web_app_testing.models.login_details import ADMIN, USER, LoginRequest
from web_app_testing.poms.login_page import LoginPage

@mark.parametrize("login", [ADMIN, USER])
def test_logging_out_as_builtin_user_returns_to_empty_login_page(driver: WebDriver, login: LoginRequest):
    LoginPage(driver).assert_log_in(login)

    # After clicking logout button and changing URLs, we should be back at the login screen.
    assert navbar.try_logout(driver)

    # Also, the input fields should be empty.
    assert not driver.find_element(By.ID, "username").get_attribute("value")
    assert not driver.find_element(By.ID, "password").get_attribute("value")
    