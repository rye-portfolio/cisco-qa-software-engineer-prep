from typing import Callable, Literal, TypeVar
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support.wait import T
from web_app_testing.constants import PAGE_LOAD_TIMEOUT_SECONDS

class BasePage:
    page_url: str | None

    def __init__(self, driver: WebDriver) -> None:
        self.driver = driver
        if self.page_url != None and driver.current_url != self.page_url:
            driver.get(self.page_url)

    def click_and_await(self, e: WebElement, expected_condition: Callable[[WebDriver], Literal[False] | T]) -> T:
        """Clicks an element and waits for an expected condition.

        Args:
            e (WebElement): the element to click
            expected_condition (ExpectedCondition[T]): the expected condition

        Returns:
            wait.T: the result of the expected condition function
        """
        e.click()
        return WebDriverWait(self.driver, PAGE_LOAD_TIMEOUT_SECONDS).until(expected_condition)
