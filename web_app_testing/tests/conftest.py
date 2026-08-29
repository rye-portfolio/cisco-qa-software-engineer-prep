import platform
from pathlib import Path
from typing import Callable, Generator

import pytest
from selenium import webdriver
from selenium.webdriver.edge.options import Options as EdgeOptions
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.remote.webdriver import WebDriver

# ====================================================================================
# Setting up pytest to use string repr for test IDs automatically
# ====================================================================================

def pytest_make_parametrize_id(config: pytest.Config, val: object, argname: str):
    if val == edge:
        return "edge"
    if val == firefox:
        return "firefox"
    return repr(val)

# ====================================================================================
# Setting up tests to run on Edge and Firefox, working on local machine and on CI/CD
# ====================================================================================

DRIVERS_DIR = Path(__file__).resolve().parent.parent / "drivers"

def edge() -> WebDriver:
    options = EdgeOptions()

    # CI/CD bs
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--inprivate")
    options.add_argument("--no-first-run")
    options.add_argument("--no-default-browser-check")

    driver_path = DRIVERS_DIR / "msedgedriver.exe"
    if platform.system() == "Windows" and driver_path.is_file():
        return webdriver.Edge(service=EdgeService(executable_path=str(driver_path)), options=options)
    return webdriver.Edge(options=options)

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
