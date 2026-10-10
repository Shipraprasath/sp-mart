package com.spmart.controller;

import com.spmart.entity.SellerProfile;
import com.spmart.entity.SellerStatus;
import com.spmart.service.SellerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final SellerService sellerService;

    public AdminSellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    public String listSellers(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "status", required = false) SellerStatus status,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              Model model) {
        Page<SellerProfile> sellerPage = sellerService.searchSellers(keyword, status, PageRequest.of(page, 15));
        model.addAttribute("sellerPage", sellerPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingCount", sellerService.getPendingSellerCount());
        return "admin/sellers";
    }

    @PostMapping("/{id}/approve")
    public String approveSeller(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SellerProfile profile = sellerService.approveSeller(id);
            redirectAttributes.addFlashAttribute("successMessage", "Seller '" + profile.getStoreName() + "' has been approved!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @PostMapping("/{id}/reject")
    public String rejectSeller(@PathVariable Long id,
                               @RequestParam(value = "reason", required = false) String reason,
                               RedirectAttributes redirectAttributes) {
        try {
            SellerProfile profile = sellerService.rejectSeller(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Seller '" + profile.getStoreName() + "' application rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @PostMapping("/{id}/suspend")
    public String suspendSeller(@PathVariable Long id,
                                @RequestParam(value = "reason", required = false) String reason,
                                RedirectAttributes redirectAttributes) {
        try {
            SellerProfile profile = sellerService.suspendSeller(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Seller '" + profile.getStoreName() + "' suspended.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/sellers";
    }
}
