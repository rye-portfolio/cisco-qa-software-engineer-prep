from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from web_app_testing.constants import PAGE_LOAD_TIMEOUT_SECONDS

def find_logout_button(driver: WebDriver):
    # Matches this HTML:
    #   <form action="/logout" method="post">
    #       <button type="submit"></button>
    #   </form>
    return driver.find_element(By.CSS_SELECTOR, 'form[action="/logout"][method="post"] button[type="submit"]')

def try_logout(driver: WebDriver):
    previous_url = driver.current_url
    find_logout_button(driver).click()
    WebDriverWait(driver, PAGE_LOAD_TIMEOUT_SECONDS).until(EC.url_changes(previous_url))
    return driver.current_url == "http://localhost:8080/login?logout"
