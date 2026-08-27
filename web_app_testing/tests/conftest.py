import platform
from pathlib import Path
from typing import Callable, Generator

import pytest
from selenium import webdriver
from selenium.webdriver.edge.options import Options as EdgeOptions
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.remote.webdriver import WebDriver

# --- For if executables exist at `web_app_testing/drivers` (Windows only) ---
DRIVERS_DIR = Path(__file__).resolve().parent.parent / "drivers"


def edge() -> WebDriver:
    # Chromium's sandbox needs to create a user namespace, which CI runners
    # (e.g. GitHub Actions' Ubuntu 24.04 image) block via AppArmor, so Edge
    # otherwise crashes immediately on launch with "session not created:
    # Chrome instance exited". --no-sandbox avoids that; --disable-dev-shm-usage
    # avoids a similar crash from CI runners' small /dev/shm.
    options = EdgeOptions()
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")

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
