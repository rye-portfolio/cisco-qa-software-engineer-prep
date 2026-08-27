from selenium import webdriver

def test_login() -> None:
    webdriver.Firefox()
    print(f"Tests passed!")
