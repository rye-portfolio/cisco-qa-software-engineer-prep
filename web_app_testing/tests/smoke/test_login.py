from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from pytest import mark
from web_app_testing.constants import ADMIN_PASSWORD, USER_PASSWORD, MAX_PAGE_LOAD_SECONDS

def test_login_page_title_is_login(driver: WebDriver):
    # Self-explanatory...
    driver.get("http://localhost:8080/login")
    assert driver.title == "Login"

@mark.parametrize("username,password", [("admin", ADMIN_PASSWORD), ("user", USER_PASSWORD)])
def test_logging_in_as_builtin_user_with_right_password_redirects_to_orders_page(driver: WebDriver, username: str, password: str):
    driver.get("http://localhost:8080/login")

    # Enter right username and password and wait for it to load
    driver.find_element(By.ID, "username").send_keys(username)
    driver.find_element(By.ID, "password").send_keys(password)
    driver.find_element(By.CSS_SELECTOR, 'button[type="submit"]').click()
    WebDriverWait(driver, MAX_PAGE_LOAD_SECONDS).until(EC.url_changes("http://localhost:8080/login"))

    # We logged in. Should automatically navigate to Orders page
    assert driver.current_url.endswith("/orders")
    assert driver.title == "Orders"

@mark.parametrize("username,password", [
    ("admin", "admin"), ("user", "admin"),
    ("admin", "pass"), ("user", "pass"),
    ("admin", "1234"), ("user", "1234"),
])
def test_logging_in_as_builtin_user_with_wrong_password_fails(driver: WebDriver, username: str, password: str):
    driver.get("http://localhost:8080/login")

    # Log in as admin and wait for it to load
    driver.find_element(By.ID, "username").send_keys(username)
    driver.find_element(By.ID, "password").send_keys(password)
    driver.find_element(By.CSS_SELECTOR, 'button[type="submit"]').click()
    WebDriverWait(driver, MAX_PAGE_LOAD_SECONDS).until(EC.url_changes("http://localhost:8080/login"))

    assert driver.current_url.endswith("/login?error")
    assert driver.title == "Login"
    assert "Invalid username or password" in driver.find_element(By.CSS_SELECTOR, "p#login-error").text
    