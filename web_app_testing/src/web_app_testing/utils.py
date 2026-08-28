from typing import Callable, Literal

from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import wait, expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from web_app_testing.constants import PAGE_LOAD_TIMEOUT_SECONDS

def assert_click(button: WebElement, expected_condition: Callable[[WebDriver], Literal[False] | wait.T], driver: WebDriver) -> wait.T:
    button.click()
    return WebDriverWait(driver, PAGE_LOAD_TIMEOUT_SECONDS).until(expected_condition)

def assert_click_redirects_to(button: WebElement, expected_url: str, driver: WebDriver) -> bool:
    button.click()
    return assert_click(button, EC.url_to_be(expected_url), driver)

def assert_click_redirects(button: WebElement, driver: WebDriver) -> bool:
    previous_url = driver.current_url
    button.click()
    return assert_click(button, EC.url_changes(previous_url), driver)
