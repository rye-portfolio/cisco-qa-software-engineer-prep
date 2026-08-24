package com.example.webapp.web;

import com.example.webapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username, @RequestParam String password,
                              @RequestParam(required = false, defaultValue = "false") boolean manageUsers,
                              @RequestParam(required = false, defaultValue = "false") boolean manageStock,
                              @RequestParam(required = false, defaultValue = "false") boolean viewAllOrders) {
        userService.createUser(username, password, manageUsers, manageStock, viewAllOrders);
        return "redirect:/users";
    }

    @PostMapping("/users/update")
    public String updatePermissions(@RequestParam Long id,
                                     @RequestParam(required = false, defaultValue = "false") boolean manageUsers,
                                     @RequestParam(required = false, defaultValue = "false") boolean manageStock,
                                     @RequestParam(required = false, defaultValue = "false") boolean viewAllOrders) {
        userService.updatePermissions(id, manageUsers, manageStock, viewAllOrders);
        return "redirect:/users";
    }
}
