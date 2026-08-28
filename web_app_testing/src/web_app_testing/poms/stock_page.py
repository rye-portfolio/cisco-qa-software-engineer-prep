from dataclasses import dataclass
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from web_app_testing.models.login import LoginRequest
from web_app_testing.constants import STOCK_URL
from web_app_testing.models.stock import AddStockItemRequest
from web_app_testing.utils import assert_click

@dataclass
class StockItemRow:
    tr: WebElement
    id: int
    name: str
    quantity: int

@dataclass
class StockPage:
    driver: WebDriver

    def __init__(self, driver: WebDriver) -> None:
        self.driver = driver
        if driver.current_url != STOCK_URL:
            driver.get(STOCK_URL)

    def __find_add_stock_item_name_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "new-item-name")
    
    def __find_add_stock_item_quantity_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "new-item-quantity")

    def __find_add_stock_item_button(self) -> WebElement:
        return self.driver.find_element(By.ID, "create-stock-submit")

    def find_last_stock_row(self) -> StockItemRow:
        row = self.driver.find_elements(By.CLASS_NAME, "stock-row")[-1]
        return self.to_stock_row(row)

    def to_stock_row(self, row: WebElement) -> StockItemRow:
        row_values = row.find_elements(By.TAG_NAME, "td")
        return StockItemRow(
            tr=row,
            id=int(row_values[0].get_attribute("value") or -1),
            name=row_values[1].get_attribute("value") or "",
            quantity=int(row_values[2].get_attribute("value") or -1),
        )

    def try_add_stock_item(self, stock_item: AddStockItemRequest) -> StockItemRow:
        self.__find_add_stock_item_name_input().send_keys(stock_item.name)
        self.__find_add_stock_item_quantity_input().send_keys(str(stock_item.quantity))
        new_row_id = 1 + self.find_last_stock_row().id
        return self.to_stock_row(assert_click(
            self.__find_add_stock_item_button(),
            EC.presence_of_element_located((By.CSS_SELECTOR, f"tr.stock-row:nth-child({new_row_id})")),
            self.driver
        ))
