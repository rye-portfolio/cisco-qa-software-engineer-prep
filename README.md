## Cisco Prep
I made this project to get me familiar with Python and Selenium. It's only half-finished, since it's only important that I am familiar, not that the suite is complete.

It contains a simple web app written by Claude. I did not read this codebase (which is intentional). In the app, users can make orders for certain stock items, which reduces that stock item's inventory.

This web app is tested with `web-app-testing`, written by me, and a little by Claude.

Some notes:
* I use `uv` and `pytest` for writing and running tests.
* I specify smoke tests and general UI tests. In a bigger project I'd mark tests with P0, P1, etc.
* I made a simple `BasePage` POM with derived classes for the stock page, orders page, and users page.
* I use custom fixtures to test on Firefox and Edge.
* I use web driver waits to await the result of button clicks, e.g. awaiting a redirect or error for the Log In button
* There's CI/CD that runs when merging into `main`, autotagging from semver if smoke tests pass, and generating an Allure report.
* and more...

You can find my contact at ryefry.com.
