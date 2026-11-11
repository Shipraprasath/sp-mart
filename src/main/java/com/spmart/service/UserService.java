package com.spmart.service;

import com.spmart.dto.ChangePasswordDto;
import com.spmart.dto.RegistrationDto;
import com.spmart.dto.UserProfileDto;
import com.spmart.entity.Role;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerCustomer(RegistrationDto dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("An account with this email address already exists.");
        }

        if (!dto.isPasswordMatching()) {
            throw new BusinessRuleException("Password confirmation does not match.");
        }

        User user = new User(
                dto.getFullName().trim(),
                normalizedEmail,
                passwordEncoder.encode(dto.getPassword()),
                dto.getPhone().trim(),
                Role.ROLE_CUSTOMER
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User updateProfile(User user, UserProfileDto dto) {
        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone().trim());
        return userRepository.save(user);
    }

    public void changePassword(User user, ChangePasswordDto dto) {
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BusinessRuleException("The current password you entered is incorrect.");
        }

        if (!dto.isPasswordMatching()) {
            throw new BusinessRuleException("New password and confirmation do not match.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public User toggleUserStatus(Long userId) {
        User user = findById(userId);
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new BusinessRuleException("Administrator account status cannot be toggled.");
        }
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public long getCustomerCount() {
        return userRepository.countByRole(Role.ROLE_CUSTOMER);
    }
}
