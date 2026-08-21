package com.example.webapp.service;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderLine;
import com.example.webapp.model.OrderStatus;
import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import com.example.webapp.repository.OrderRepository;
import com.example.webapp.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockItemRepository stockItemRepository;

    public OrderService(OrderRepository orderRepository, StockItemRepository stockItemRepository) {
        this.orderRepository = orderRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @Transactional
    public Order createOrder(User creator, List<OrderRequestLine> requestLines) {
        for (OrderRequestLine requestLine : requestLines) {
            StockItem item = stockItemRepository.findById(requestLine.stockItemId())
                    .orElseThrow(() -> new InsufficientStockException(
                            "Unknown stock item: " + requestLine.stockItemId()));
            if (item.getQuantity() < requestLine.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + item.getName()
                                + ": requested " + requestLine.quantity()
                                + ", available " + item.getQuantity());
            }
        }

        Order order = new Order(creator, Instant.now(), OrderStatus.COMPLETED);
        for (OrderRequestLine requestLine : requestLines) {
            StockItem item = stockItemRepository.findById(requestLine.stockItemId()).orElseThrow();
            item.setQuantity(item.getQuantity() - requestLine.quantity());
            order.addLine(new OrderLine(item, requestLine.quantity()));
        }

        return orderRepository.save(order);
    }

    public List<Order> findOrdersByCreator(User creator) {
        return orderRepository.findByCreator(creator);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }
}
