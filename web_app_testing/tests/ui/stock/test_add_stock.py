from selenium.webdriver.remote.webdriver import WebDriver

def test_adding_stock_with_permission_persists_stock(driver: WebDriver):
    # params: with Admin user and StockManager user
    # extract some common logic between test cases:
    #   LoginPage as user, assert login, navbar method to click Stock link,
    #   name should include a guid so it never overlaps
    #   add stock using new pom + models/AddStockItemRequest
    # for this test, refresh page, verify new addition is in the grid
    pass

def test_adding_stock_without_permission_does_not_persist_stock(driver: WebDriver):
    # params: with OrderViewer and User user
    # ditto
    pass

# also test boundary cases:
# empty name? 0 quantity? able to enter -1 quantity?
