# Automated testing for WebApp

Used to exercise automated testing skills in Python with Selenium.

In this directory:

* To install, execute `uv sync`.
* To run smoke tests, execute `uv run pytest ./tests/smoke`.
* To run all tests, execute `uv run pytest`.
* To run all tests and capture Allure results, execute `uv run pytest --alluredir=allure-results`, then `allure generate ./allure-results --clean -o ./allure-report` (requires the [Allure commandline](https://allurereport.org/docs/install/), e.g. `npm install -g allure-commandline`) and open `allure-report/index.html`.

To contribute, use [VSCodium](https://vscodium.com/#install) with the [Python](https://open-vsx.org/vscode/item?itemName=ms-python.python) and [Python Debugger](https://open-vsx.org/vscode/item?itemName=ms-python.debugpy) extensions. Use `uv sync` to create `.venv`, and ensure VSCode's Python interpreter is `.venv/Scripts/python`.
