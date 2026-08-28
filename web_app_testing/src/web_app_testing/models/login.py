from dataclasses import dataclass

@dataclass
class LoginRequest:
    username: str
    password: str

    def __repr__(self) -> str:
        return self.username

ADMIN = LoginRequest("admin", "password")                   # all perms
USER = LoginRequest("user", "password")                     # no perms
STOCK_MANAGER = LoginRequest("stockmanager", "password")    # Manage Stock=true
ORDER_VIEWER = LoginRequest("orderviewer", "password")      # Manage Orders=true
