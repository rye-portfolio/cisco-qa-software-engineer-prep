from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from pytest import mark
from web_app_testing.constants import ADMIN_PASSWORD, USER_PASSWORD, MAX_PAGE_LOAD_SECONDS

@mark.parametrize("username,password", [("admin", ADMIN_PASSWORD), ("user", USER_PASSWORD)])
def test_logging_out_as_builtin_user_returns_to_empty_login_page(driver: WebDriver, username: str, password: str):
    driver.get("http://localhost:8080/login")

    # Enter right username and password and wait for it to load
    driver.find_element(By.ID, "username").send_keys(username)
    driver.find_element(By.ID, "password").send_keys(password)
    driver.find_element(By.CSS_SELECTOR, "button[type=\"submit\"]").click()
    WebDriverWait(driver, MAX_PAGE_LOAD_SECONDS).until(EC.url_changes("http://localhost:8080/login"))

    # Logged in. Find log out button.
    # Matches this HTML:
    #   <form action="/logout" method="post">
    #       <button type="submit"></button>
    #   </form>
    previous_url = driver.current_url
    driver.find_element(By.CSS_SELECTOR, 'form[action="/logout"][method="post"] button[type="submit"]').click()
    WebDriverWait(driver, MAX_PAGE_LOAD_SECONDS).until(EC.url_changes(previous_url))

    # After clicking logout button and changing URLs, we should be back at the login screen.
    # Also, the input fields should be empty.
    assert driver.current_url == "http://localhost:8080/login?logout"
    assert not driver.find_element(By.ID, "username").get_attribute("value")
    assert not driver.find_element(By.ID, "password").get_attribute("value")
    