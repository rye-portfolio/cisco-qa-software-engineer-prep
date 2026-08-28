from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from web_app_testing.constants import LOGOUT_REDIRECT_URL, STOCK_URL
from web_app_testing.utils import assert_click_redirects_to

# ======================================================================
# LOG OUT
# ======================================================================
def __find_logout_button(driver: WebDriver):
    # Matches this HTML:
    #   <form action="/logout" method="post">
    #       <button type="submit"></button>
    #   </form>
    return driver.find_element(By.CSS_SELECTOR, 'form[action="/logout"][method="post"] button[type="submit"]')

def __try_log_out(driver: WebDriver) -> bool:
    return assert_click_redirects_to(__find_logout_button(driver), LOGOUT_REDIRECT_URL, driver)

def assert_log_out(driver: WebDriver) -> None:
    assert __try_log_out(driver), "Failed to log out"

# ======================================================================
# STOCK LINK 
# ======================================================================
def __find_stock_link(driver: WebDriver):
    return driver.find_element(By.CSS_SELECTOR, 'a[href="/stock"]')

def __try_navigate_to_stock(driver: WebDriver) -> bool:
    return assert_click_redirects_to(__find_stock_link(driver), STOCK_URL, driver)

def assert_navigate_to_stock(driver: WebDriver) -> None:
    assert __try_navigate_to_stock(driver), "Failed to click Stock nav"
