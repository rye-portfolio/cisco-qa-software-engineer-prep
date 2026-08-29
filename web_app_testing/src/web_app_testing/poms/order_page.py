from dataclasses import dataclass
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from web_app_testing.constants import STOCK_URL
from web_app_testing.poms.with_navbar import PageWithNavBar


@dataclass
class OrderPage(PageWithNavBar):
    page_url = STOCK_URL

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
