import pytest
from selenium.webdriver.remote.webdriver import WebDriver
from web_app_testing.components import navbar
from web_app_testing.models.login import ADMIN, ORDER_VIEWER, STOCK_MANAGER, USER
from web_app_testing.models.stock import AddStockItemRequest
from web_app_testing.poms.login_page import LoginPage
from web_app_testing.poms.stock_page import StockPage

@pytest.fixture(params=[ADMIN, STOCK_MANAGER], scope="module")
def logged_in_with_permission(driver: WebDriver, request: pytest.FixtureRequest) -> LoginPage:
    return LoginPage(driver).assert_log_in(request.param)

def test_adding_stock_item_with_permission_succeeds(logged_in_with_permission: LoginPage):
    driver = logged_in_with_permission.driver

    navbar.assert_navigate_to_stock(driver)

    page = StockPage(driver)
    page.try_add_stock_item(AddStockItemRequest("test-stock-item", 10))

    # params: with Admin user and StockManager user
    # extract some common logic between test cases:
    #   LoginPage as user, assert login, navbar method to click Stock link,
    #   name should include a guid so it never overlaps
    #   add stock using new pom + models/AddStockItemRequest
    # for this test, refresh page, verify new addition is in the grid
    pass

@pytest.fixture(params=[USER, ORDER_VIEWER], scope="module")
def logged_in_without_permission(driver: WebDriver, request: pytest.FixtureRequest) -> LoginPage:
    return LoginPage(driver).assert_log_in(request.param)

def test_adding_stock_item_without_permission_fails(logged_in_without_permission: LoginPage):
    # params: with OrderViewer and User user
    # ditto
    pass

# also test boundary cases:
# empty name? empty quantity? 0 quantity? able to enter -1 quantity?
# also create test_update_stock_quantity.py
# Note: Empty name will succeed. It shouldn't. This is a case where the test *is expected to fail*.
