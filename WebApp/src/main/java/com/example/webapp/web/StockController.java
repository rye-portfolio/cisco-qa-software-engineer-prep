package com.example.webapp.web;

import com.example.webapp.service.StockService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/stock")
    public String listStock(Model model) {
        model.addAttribute("stockItems", stockService.findAll());
        return "stock";
    }

    @PostMapping("/stock/create")
    @PreAuthorize("hasAuthority('MANAGE_STOCK')")
    public String createItem(@RequestParam String name, @RequestParam int quantity) {
        stockService.createItem(name, quantity);
        return "redirect:/stock";
    }

    @PostMapping("/stock/update")
    @PreAuthorize("hasAuthority('MANAGE_STOCK')")
    public String updateQuantity(@RequestParam Long id, @RequestParam int quantity) {
        stockService.updateQuantity(id, quantity);
        return "redirect:/stock";
    }
}
