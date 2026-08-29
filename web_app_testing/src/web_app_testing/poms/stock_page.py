from dataclasses import dataclass
from typing import Literal
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from web_app_testing.constants import STOCK_URL
from web_app_testing.models.stock import AddStockItemRequest
from web_app_testing.poms.with_navbar import PageWithNavBar

def to_stock_row(row: WebElement) -> StockItemRow | None:
    row_values = row.find_elements(By.TAG_NAME, "td")
    if len(row_values) < 3 or any(not v.text for v in row_values[:3]):
        return None
    return StockItemRow(
        tr=row,
        id=int(row_values[0].text),
        name=row_values[1].text,
        quantity=int(row_values[2].text),
    )
    
@dataclass
class StockItemRow:
    tr: WebElement
    id: int
    name: str
    quantity: int

@dataclass
class StockPage(PageWithNavBar):
    page_url = STOCK_URL

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)

    def find_add_stock_item_name_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "new-item-name")
    
    def find_add_stock_item_quantity_input(self) -> WebElement:
        return self.driver.find_element(By.ID, "new-item-quantity")

    def find_add_stock_item_button(self) -> WebElement:
        return self.driver.find_element(By.ID, "create-stock-submit")

    def find_last_stock_row(self) -> StockItemRow:
        row = self.driver.find_elements(By.CLASS_NAME, "stock-row")[-1]
        result = to_stock_row(row)
        assert result, "Last stock row is malformed"
        return result

    def __await_add_stock_result(self, row_count_before: int) -> StockItemRow | None | Literal[False]:
        rows = self.driver.find_elements(By.CLASS_NAME, "stock-row")
        if len(rows) > row_count_before:
            return self.find_last_stock_row()
        if self.driver.find_elements(By.ID, "stock-error"):
            return None
        return False

    def try_add_stock_item(self, stock_item: AddStockItemRequest) -> StockItemRow | None:
        row_count_before = len(self.driver.find_elements(By.CLASS_NAME, "stock-row"))
        if stock_item.name:
            self.find_add_stock_item_name_input().send_keys(stock_item.name)
        if stock_item.quantity:
            self.find_add_stock_item_quantity_input().send_keys(str(stock_item.quantity))
        return self.click_and_await(
            self.find_add_stock_item_button(),
            lambda _ : self.__await_add_stock_result(row_count_before)
        )

    def add_stock_item(self, stock_item: AddStockItemRequest) -> StockItemRow:
        result = self.try_add_stock_item(stock_item)
        assert result
        return result
