package com.example.webapp.service;

import com.example.webapp.model.StockItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Test
    void createItemPersistsNewStockItem() {
        StockItem created = stockService.createItem("Thingamajig", 7);

        assertThat(created.getId()).isNotNull();
        assertThat(stockService.findAll()).extracting(StockItem::getName).contains("Thingamajig");
    }

    @Test
    void updateQuantityChangesExistingItem() {
        StockItem created = stockService.createItem("Doohickey", 3);

        StockItem updated = stockService.updateQuantity(created.getId(), 9);

        assertThat(updated.getQuantity()).isEqualTo(9);
    }
}
