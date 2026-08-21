package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_lines")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Column(nullable = false)
    private int quantity;

    protected OrderLine() {
    }

    public OrderLine(StockItem stockItem, int quantity) {
        this.stockItem = stockItem;
        this.quantity = quantity;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public StockItem getStockItem() { return stockItem; }
    public int getQuantity() { return quantity; }
}
