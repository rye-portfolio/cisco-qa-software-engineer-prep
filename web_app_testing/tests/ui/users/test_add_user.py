from selenium.webdriver.remote.webdriver import WebDriver

def setup_users(driver: WebDriver):
    # Create users
    yield driver

def test_adding_user_with_permission_succeeds_with_refresh(setup_users):
    # add users, should be parameterized
    pass
