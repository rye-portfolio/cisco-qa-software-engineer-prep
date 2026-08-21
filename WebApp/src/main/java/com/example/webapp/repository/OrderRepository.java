package com.example.webapp.repository;

import com.example.webapp.model.Order;
import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"creator", "lines", "lines.stockItem"})
    List<Order> findByCreator(User creator);

    @EntityGraph(attributePaths = {"creator", "lines", "lines.stockItem"})
    @Query("select o from Order o")
    List<Order> findAllWithDetails();
}
