from dataclasses import dataclass

@dataclass
class AddStockItemRequest:
    name: str
    quantity: int | None
