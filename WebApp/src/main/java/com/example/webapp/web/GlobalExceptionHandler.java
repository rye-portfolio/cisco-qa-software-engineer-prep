package com.example.webapp.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Catches foreseeable bad-input exceptions thrown from controllers/services (unknown ids,
 * duplicate usernames) that would otherwise surface as a whitelabel 500, and instead redirects
 * back to the originating page with a flash-attribute error message.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + redirectTarget(request);
    }

    // The SQLite community dialect does not classify unique-constraint failures as a
    // ConstraintViolationException, so Hibernate/Spring surface them as a generic
    // JpaSystemException rather than DataIntegrityViolationException. Handle both so a
    // duplicate username never reaches the user as a whitelabel 500.
    @ExceptionHandler({DataIntegrityViolationException.class, JpaSystemException.class})
    public String handleDataAccessException(RuntimeException ex, HttpServletRequest request,
                                             RedirectAttributes redirectAttributes) {
        String target = redirectTarget(request);
        String message = "/users".equals(target)
                ? "That username is already taken."
                : "That change could not be saved because it conflicts with existing data.";
        redirectAttributes.addFlashAttribute("error", message);
        return "redirect:" + target;
    }

    private String redirectTarget(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/users")) {
            return "/users";
        }
        if (uri.startsWith("/stock")) {
            return "/stock";
        }
        return "/orders";
    }
}
