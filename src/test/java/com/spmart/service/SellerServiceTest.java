package com.spmart.service;

import com.spmart.dto.SellerRegistrationDto;
import com.spmart.entity.Role;
import com.spmart.entity.SellerProfile;
import com.spmart.entity.SellerStatus;
import com.spmart.entity.User;
import com.spmart.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SellerServiceTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Seller registration creates user with ROLE_SELLER and PENDING_APPROVAL profile")
    void testSellerRegistration() {
        SellerRegistrationDto dto = new SellerRegistrationDto();
        dto.setFullName("Test Merchant");
        dto.setEmail("merchant.test@spmart.com");
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("Secret@123");
        dto.setStoreName("AeroTech Gadgets");
        dto.setPhone("+91 9988776655");
        dto.setBusinessAddress("100 Tech Park, Bengaluru, Karnataka");
        dto.setGstin("29AAAAA0000A1Z5");

        SellerProfile profile = sellerService.registerSeller(dto);

        assertNotNull(profile);
        assertEquals("AeroTech Gadgets", profile.getStoreName());
        assertEquals(SellerStatus.PENDING_APPROVAL, profile.getStatus());
        assertEquals(Role.ROLE_SELLER, profile.getUser().getRole());
        assertFalse(sellerService.isApprovedSeller(profile.getUser()));
    }

    @Test
    @DisplayName("Admin approval enables seller and sets status to APPROVED")
    void testApproveSeller() {
        SellerRegistrationDto dto = new SellerRegistrationDto();
        dto.setFullName("Approval Merchant");
        dto.setEmail("approval.test@spmart.com");
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("Secret@123");
        dto.setStoreName("VoltEdge Electronics");
        dto.setPhone("+91 9123456780");
        dto.setBusinessAddress("200 Cyber Hub, Gurugram, Haryana");
        dto.setGstin("07AAAAA0000A1Z5");

        SellerProfile profile = sellerService.registerSeller(dto);
        SellerProfile approved = sellerService.approveSeller(profile.getId());

        assertEquals(SellerStatus.APPROVED, approved.getStatus());
        assertTrue(sellerService.isApprovedSeller(approved.getUser()));
    }

    @Test
    @DisplayName("Admin rejection sets status to REJECTED")
    void testRejectSeller() {
        SellerRegistrationDto dto = new SellerRegistrationDto();
        dto.setFullName("Rejected Merchant");
        dto.setEmail("reject.test@spmart.com");
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("Secret@123");
        dto.setStoreName("Shady Tech Outlet");
        dto.setPhone("+91 9111222333");
        dto.setBusinessAddress("Invalid address");
        dto.setGstin("000000000000000");

        SellerProfile profile = sellerService.registerSeller(dto);
        SellerProfile rejected = sellerService.rejectSeller(profile.getId(), "Incomplete documentation");

        assertEquals(SellerStatus.REJECTED, rejected.getStatus());
        assertFalse(sellerService.isApprovedSeller(rejected.getUser()));
    }
}
