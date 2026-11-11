package com.spmart.service;

import com.spmart.dto.RegistrationDto;
import com.spmart.entity.User;
import com.spmart.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PasswordResetServiceTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Password reset token creation, validation, and password update")
    void testPasswordResetFlow() {
        RegistrationDto regDto = new RegistrationDto();
        regDto.setFullName("Reset Tester");
        regDto.setEmail("reset.tester@spmart.com");
        regDto.setPassword("OldPass@123");
        regDto.setConfirmPassword("OldPass@123");
        regDto.setPhone("+91 9888877777");
        User user = userService.registerCustomer(regDto);

        // 1. Create reset token
        String token = passwordResetService.createPasswordResetToken(user.getEmail());
        assertNotNull(token);
        assertFalse(token.isBlank());

        // 2. Validate token
        Optional<User> userOpt = passwordResetService.validatePasswordResetToken(token);
        assertTrue(userOpt.isPresent());
        assertEquals(user.getEmail(), userOpt.get().getEmail());

        // 3. Reset password
        passwordResetService.resetPassword(token, "NewSecret@456");

        // 4. Verify new password in DB
        User updated = userRepository.findByEmail(user.getEmail()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewSecret@456", updated.getPassword()));

        // 5. Token should now be consumed / invalid
        Optional<User> reusedOpt = passwordResetService.validatePasswordResetToken(token);
        assertFalse(reusedOpt.isPresent());
    }
}
