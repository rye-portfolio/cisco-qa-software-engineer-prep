import platform
from pathlib import Path
from typing import Callable, Generator

import pytest
from selenium import webdriver
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.remote.webdriver import WebDriver

# --- For if executables exist at `web_app_testing/drivers` (Windows only) ---
DRIVERS_DIR = Path(__file__).resolve().parent.parent / "drivers"


def edge() -> WebDriver:
    driver_path = DRIVERS_DIR / "msedgedriver.exe"
    if platform.system() == "Windows" and driver_path.is_file():
        return webdriver.Edge(service=EdgeService(executable_path=str(driver_path)))
    return webdriver.Edge()


def firefox() -> WebDriver:
    driver_path = DRIVERS_DIR / "geckodriver.exe"
    if platform.system() == "Windows" and driver_path.is_file():
        return webdriver.Firefox(service=FirefoxService(executable_path=str(driver_path)))
    return webdriver.Firefox()


# Shared fixture. Runs all tests on Edge and Firefox
@pytest.fixture(params=[edge, firefox], scope="module")
def driver(request: pytest.FixtureRequest) -> Generator[WebDriver, None, None]:
    driver: WebDriver = request.param()
    yield driver
    driver.quit()
