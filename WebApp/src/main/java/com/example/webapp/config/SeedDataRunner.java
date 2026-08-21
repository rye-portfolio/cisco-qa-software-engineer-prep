package com.example.webapp.config;

import com.example.webapp.model.StockItem;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final StockItemRepository stockItemRepository;

    public SeedDataRunner(UserRepository userRepository, UserService userService, StockItemRepository stockItemRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.stockItemRepository = stockItemRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        userService.createUser("admin", "password", true, true, true);
        userService.createUser("user", "password", false, false, false);
        userService.createUser("stockmanager", "password", false, true, false);
        userService.createUser("orderviewer", "password", false, false, true);

        stockItemRepository.save(new StockItem("Widget", 50));
        stockItemRepository.save(new StockItem("Gadget", 20));
        stockItemRepository.save(new StockItem("Gizmo", 5));
        stockItemRepository.save(new StockItem("Sprocket", 0));
    }
}
