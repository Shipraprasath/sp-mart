package com.spmart.controller;

import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.InsufficientStockException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.exception.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public String handleUnauthorizedAccess(UnauthorizedAccessException ex, Model model, HttpServletRequest request) {
        log.warn("Unauthorized access attempt at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler({InsufficientStockException.class, BusinessRuleException.class})
    public String handleBusinessRuleException(RuntimeException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model, HttpServletRequest request) {
        log.error("Unhandled error processing request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", "An unexpected error occurred while processing your request. Please try again.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/500";
    }
}
