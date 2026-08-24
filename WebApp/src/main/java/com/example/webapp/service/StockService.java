package com.example.webapp.service;

import com.example.webapp.model.StockItem;
import com.example.webapp.repository.StockItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockItemRepository stockItemRepository;

    public StockService(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    public List<StockItem> findAll() {
        return stockItemRepository.findAll();
    }

    public StockItem createItem(String name, int quantity) {
        return stockItemRepository.save(new StockItem(name, quantity));
    }

    public StockItem updateQuantity(Long id, int quantity) {
        StockItem item = stockItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown stock item: " + id));
        item.setQuantity(quantity);
        return stockItemRepository.save(item);
    }
}
