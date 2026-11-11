package com.spmart.service;

import com.spmart.dto.ChangePasswordDto;
import com.spmart.dto.RegistrationDto;
import com.spmart.entity.Role;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("1. Successfully register new customer with hashed password")
    void testRegisterCustomer_Success() {
        RegistrationDto dto = new RegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("john.doe.test@example.com");
        dto.setPhone("9876543210");
        dto.setPassword("Secret123");
        dto.setConfirmPassword("Secret123");

        User registered = userService.registerCustomer(dto);

        assertNotNull(registered.getId());
        assertEquals("john.doe.test@example.com", registered.getEmail());
        assertEquals("John Doe", registered.getFullName());
        assertEquals(Role.ROLE_CUSTOMER, registered.getRole());
        assertTrue(registered.isEnabled());

        // Verify password hashing with BCrypt
        assertNotEquals("Secret123", registered.getPassword());
        assertTrue(passwordEncoder.matches("Secret123", registered.getPassword()));
    }

    @Test
    @DisplayName("2. Prevent duplicate email registration")
    void testRegisterCustomer_DuplicateEmail() {
        RegistrationDto dto1 = new RegistrationDto();
        dto1.setFullName("John Doe");
        dto1.setEmail("duplicate.test@example.com");
        dto1.setPhone("9876543210");
        dto1.setPassword("Secret123");
        dto1.setConfirmPassword("Secret123");
        userService.registerCustomer(dto1);

        RegistrationDto dto2 = new RegistrationDto();
        dto2.setFullName("Jane Smith");
        dto2.setEmail("duplicate.test@example.com");
        dto2.setPhone("9123456789");
        dto2.setPassword("AnotherPass");
        dto2.setConfirmPassword("AnotherPass");

        assertThrows(BusinessRuleException.class, () -> userService.registerCustomer(dto2));
    }

    @Test
    @DisplayName("3. Reject registration when passwords do not match")
    void testRegisterCustomer_PasswordMismatch() {
        RegistrationDto dto = new RegistrationDto();
        dto.setFullName("Mismatch User");
        dto.setEmail("mismatch.test@example.com");
        dto.setPhone("9876543210");
        dto.setPassword("Secret123");
        dto.setConfirmPassword("Different123");

        assertThrows(BusinessRuleException.class, () -> userService.registerCustomer(dto));
    }

    @Test
    @DisplayName("4. Change password validates current password")
    void testChangePassword() {
        RegistrationDto reg = new RegistrationDto();
        reg.setFullName("Pass User");
        reg.setEmail("passuser.test@example.com");
        reg.setPhone("9876543210");
        reg.setPassword("OldPassword");
        reg.setConfirmPassword("OldPassword");
        User user = userService.registerCustomer(reg);

        ChangePasswordDto badDto = new ChangePasswordDto();
        badDto.setCurrentPassword("WrongPass");
        badDto.setNewPassword("NewSecret123");
        badDto.setConfirmPassword("NewSecret123");

        assertThrows(BusinessRuleException.class, () -> userService.changePassword(user, badDto));

        ChangePasswordDto goodDto = new ChangePasswordDto();
        goodDto.setCurrentPassword("OldPassword");
        goodDto.setNewPassword("NewSecret123");
        goodDto.setConfirmPassword("NewSecret123");

        userService.changePassword(user, goodDto);

        User updated = userService.findById(user.getId());
        assertTrue(passwordEncoder.matches("NewSecret123", updated.getPassword()));
    }

    @Test
    @DisplayName("5. Toggle customer account enabled/disabled")
    void testToggleCustomerStatus() {
        RegistrationDto reg = new RegistrationDto();
        reg.setFullName("Toggle User");
        reg.setEmail("toggle.test@example.com");
        reg.setPhone("9876543210");
        reg.setPassword("Password123");
        reg.setConfirmPassword("Password123");
        User user = userService.registerCustomer(reg);
        assertTrue(user.isEnabled());

        User toggled = userService.toggleUserStatus(user.getId());
        assertFalse(toggled.isEnabled());

        User reactivated = userService.toggleUserStatus(user.getId());
        assertTrue(reactivated.isEnabled());
    }
}
