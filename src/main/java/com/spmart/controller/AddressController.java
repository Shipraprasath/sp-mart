package com.spmart.controller;

import com.spmart.dto.AddressDto;
import com.spmart.entity.Address;
import com.spmart.entity.User;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.AddressService;
import com.spmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/account/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    public AddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping
    public String listAddresses(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        List<Address> addresses = addressService.getUserAddresses(user);
        model.addAttribute("addresses", addresses);
        return "account/addresses";
    }

    @GetMapping("/add")
    public String addAddressForm(Model model) {
        if (!model.containsAttribute("addressDto")) {
            model.addAttribute("addressDto", new AddressDto());
        }
        return "account/address-form";
    }

    @PostMapping("/add")
    public String saveAddress(@Valid @ModelAttribute("addressDto") AddressDto dto,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "account/address-form";
        }
        User user = userService.findById(userDetails.getId());
        addressService.createAddress(user, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Address saved successfully.");
        return "redirect:/account/addresses";
    }

    @GetMapping("/edit/{id}")
    public String editAddressForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        User user = userService.findById(userDetails.getId());
        Address address = addressService.getAddressByIdAndUser(id, user);

        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setRecipientName(address.getRecipientName());
        dto.setPhone(address.getPhone());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setLocality(address.getLocality());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setLandmark(address.getLandmark());
        dto.setDefault(address.isDefault());

        model.addAttribute("addressDto", dto);
        return "account/address-form";
    }

    @PostMapping("/edit/{id}")
    public String updateAddress(@PathVariable Long id,
                                @Valid @ModelAttribute("addressDto") AddressDto dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "account/address-form";
        }
        User user = userService.findById(userDetails.getId());
        addressService.updateAddress(user, id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Address updated successfully.");
        return "redirect:/account/addresses";
    }

    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        addressService.deleteAddress(user, id);
        redirectAttributes.addFlashAttribute("successMessage", "Address deleted successfully.");
        return "redirect:/account/addresses";
    }

    @PostMapping("/default/{id}")
    public String setDefaultAddress(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        addressService.setDefaultAddress(user, id);
        redirectAttributes.addFlashAttribute("successMessage", "Default address updated.");
        return "redirect:/account/addresses";
    }
}
