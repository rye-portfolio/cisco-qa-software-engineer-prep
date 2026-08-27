import pytest
from typing import Generator
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.webdriver import LocalWebDriver

@pytest.fixture
def setup() -> Generator[LocalWebDriver, None, None]:
    driver = webdriver.Chrome()
    driver.get("http://localhost:8080/login")
    yield driver
    driver.quit()

def test_login_admin(setup: Generator[LocalWebDriver, None, None]) -> None:
    driver = setup.send(None)
    username = driver.find_element(By.ID, "username")
    print(f"Tests passed! {username.tag_name}")
