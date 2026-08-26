import pytest
from web_app_testing.calc import add

def test_pass() -> None:
    assert add(1, 2.5) == 3.5
    print("Tests passed!")
