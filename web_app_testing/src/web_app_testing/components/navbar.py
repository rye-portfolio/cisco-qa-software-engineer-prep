from __future__ import annotations

from dataclasses import dataclass
from typing import TYPE_CHECKING
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from web_app_testing.constants import LOGOUT_REDIRECT_URL, STOCK_URL
from web_app_testing.poms.base import BasePage

if TYPE_CHECKING:
    from web_app_testing.poms.login_page import LoginPage
    from web_app_testing.poms.stock_page import StockPage

@dataclass
class NavBar:
    page: BasePage

    # ======================================================================
    # LOG OUT
    # ======================================================================
    def find_logout_button(self):
        # Matches this HTML:
        #   <form action="/logout" method="post">
        #       <button type="submit"></button>
        #   </form>
        return self.page.driver.find_element(By.CSS_SELECTOR, 'form[action="/logout"][method="post"] button[type="submit"]')

    def assert_log_out(self) -> LoginPage:
        from web_app_testing.poms.login_page import LoginPage
        assert self.page.click_and_await(self.find_logout_button(), EC.url_to_be(LOGOUT_REDIRECT_URL)), "Failed to log out"
        return LoginPage(self.page.driver)

    # ======================================================================
    # STOCK LINK 
    # ======================================================================
    def find_stock_link(self):
        return self.page.driver.find_element(By.CSS_SELECTOR, 'a[href="/stock"]')

    def assert_fail_to_navigate_to_stock(self) -> StockPage:
        from web_app_testing.poms.stock_page import StockPage
        assert self.page.click_and_await(self.find_stock_link(), EC.url_to_be(STOCK_URL)), "Failed to navigate to Stock page"
        return StockPage(self.page.driver)
