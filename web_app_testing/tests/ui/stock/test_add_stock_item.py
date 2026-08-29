from uuid import uuid4

import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from web_app_testing.models.login import ADMIN, ORDER_VIEWER, STOCK_MANAGER, USER
from web_app_testing.models.stock import AddStockItemRequest
from web_app_testing.poms.login_page import LoginPage
from web_app_testing.poms.order_page import OrderPage
from web_app_testing.poms.stock_page import StockPage

@pytest.fixture(params=[ADMIN, STOCK_MANAGER], scope="module")
def logged_in_with_permission(driver: WebDriver, request: pytest.FixtureRequest) -> OrderPage:
    return LoginPage(driver).log_in(request.param)

@pytest.fixture(scope="module")
def on_stock_page_with_permission(logged_in_with_permission: OrderPage) -> StockPage:
    return logged_in_with_permission.navbar().navigate_to_stock_page()

def test_adding_simple_stock_item_succeeds(on_stock_page_with_permission: StockPage):
    page = on_stock_page_with_permission
    
    row_to_add = AddStockItemRequest(f"test-stock-item-{uuid4()}", "10")
    row_to_add_id = page.find_last_stock_row().id + 1
    added_row = page.add_stock_item(row_to_add)

    assert added_row
    assert added_row.id == row_to_add_id
    assert added_row.name == row_to_add.name
    assert added_row.quantity == row_to_add.quantity
    assert added_row.quantity.isdigit()

    # Should be visible after refresh too.
    page.driver.refresh()
    most_recently_added_row = page.find_last_stock_row()
    
    assert most_recently_added_row.id == row_to_add_id
    assert most_recently_added_row.name == row_to_add.name
    assert most_recently_added_row.quantity == row_to_add.quantity

def test_adding_stock_item_without_quantity_fails(on_stock_page_with_permission: StockPage):
    page = on_stock_page_with_permission
    row_to_fail_adding = AddStockItemRequest("fail-test", "")
    added_row = page.try_add_stock_item(row_to_fail_adding)
    assert not added_row

@pytest.fixture(params=[USER, ORDER_VIEWER], scope="module")
def logged_in_without_permission(driver: WebDriver, request: pytest.FixtureRequest) -> OrderPage:
    return LoginPage(driver).log_in(request.param)

def test_adding_stock_item_without_permission_fails(logged_in_without_permission: OrderPage):
    # params: with OrderViewer and User user
    # ditto
    pass

# also test boundary cases:
# empty name? empty quantity? 0 quantity? able to enter -1 quantity?
# also create test_update_stock_quantity.py
# Note: Empty name will succeed. It shouldn't. This is a case where the test *is expected to fail*.
