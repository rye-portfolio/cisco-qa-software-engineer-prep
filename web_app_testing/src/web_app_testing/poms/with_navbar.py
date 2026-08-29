
from web_app_testing.components.navbar import NavBar
from web_app_testing.poms.base import BasePage

class PageWithNavBar(BasePage):
    def navbar(self) -> NavBar:
        return NavBar(self)
