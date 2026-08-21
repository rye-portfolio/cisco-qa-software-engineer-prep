package com.example.webapp.web;

import com.example.webapp.model.Order;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.InsufficientStockException;
import com.example.webapp.service.OrderRequestLine;
import com.example.webapp.service.OrderService;
import com.example.webapp.service.StockService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.IntStream;

@Controller
public class OrdersController {

    private final OrderService orderService;
    private final StockService stockService;
    private final UserRepository userRepository;

    public OrdersController(OrderService orderService, StockService stockService, UserRepository userRepository) {
        this.orderService = orderService;
        this.stockService = stockService;
        this.userRepository = userRepository;
    }

    @GetMapping("/orders")
    public String listOrders(@RequestParam(name = "all", required = false, defaultValue = "false") boolean all,
                              Authentication authentication, Model model) {
        User currentUser = currentUser(authentication);
        boolean canViewAll = hasAuthority(authentication, "VIEW_ALL_ORDERS");
        boolean viewingAll = all && canViewAll;

        List<Order> orders = viewingAll ? orderService.findAllOrders() : orderService.findOrdersByCreator(currentUser);

        model.addAttribute("orders", orders);
        model.addAttribute("viewingAll", viewingAll);
        model.addAttribute("canViewAll", canViewAll);
        model.addAttribute("stockItems", stockService.findAll());
        return "orders";
    }

    @PostMapping("/orders")
    public String createOrder(@RequestParam("stockItemId") List<Long> stockItemIds,
                               @RequestParam("quantity") List<Integer> quantities,
                               Authentication authentication, Model model) {
        User currentUser = currentUser(authentication);

        if (stockItemIds.size() != quantities.size()) {
            return showOrderError(currentUser, authentication, model, "Invalid order submission.");
        }

        List<OrderRequestLine> lines = buildLines(stockItemIds, quantities);

        try {
            orderService.createOrder(currentUser, lines);
        } catch (InsufficientStockException e) {
            return showOrderError(currentUser, authentication, model, e.getMessage());
        }

        return "redirect:/orders";
    }

    private String showOrderError(User currentUser, Authentication authentication, Model model, String message) {
        model.addAttribute("error", message);
        model.addAttribute("orders", orderService.findOrdersByCreator(currentUser));
        model.addAttribute("viewingAll", false);
        model.addAttribute("canViewAll", hasAuthority(authentication, "VIEW_ALL_ORDERS"));
        model.addAttribute("stockItems", stockService.findAll());
        return "orders";
    }

    private List<OrderRequestLine> buildLines(List<Long> stockItemIds, List<Integer> quantities) {
        return IntStream.range(0, stockItemIds.size())
                .filter(i -> quantities.get(i) != null && quantities.get(i) > 0)
                .mapToObj(i -> new OrderRequestLine(stockItemIds.get(i), quantities.get(i)))
                .toList();
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseThrow();
    }
}
