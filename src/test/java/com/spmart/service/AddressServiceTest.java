package com.spmart.service;

import com.spmart.dto.AddressDto;
import com.spmart.entity.Address;
import com.spmart.entity.Role;
import com.spmart.entity.User;
import com.spmart.exception.UnauthorizedAccessException;
import com.spmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    private User customer1;
    private User customer2;

    @BeforeEach
    void setUp() {
        customer1 = userRepository.save(new User("Alice", "alice@example.com", "pass", "9876543210", Role.ROLE_CUSTOMER));
        customer2 = userRepository.save(new User("Bob", "bob@example.com", "pass", "9123456789", Role.ROLE_CUSTOMER));
    }

    @Test
    @DisplayName("11. Address creation sets first address as default automatically")
    void testCreateAddress_DefaultBehavior() {
        AddressDto dto = new AddressDto();
        dto.setRecipientName("Alice In Wonderland");
        dto.setPhone("9876543210");
        dto.setAddressLine1("123 Rabbit Hole Way");
        dto.setLocality("Fantasy Land");
        dto.setCity("Bengaluru");
        dto.setState("Karnataka");
        dto.setPostalCode("560001");
        dto.setDefault(false);

        Address created = addressService.createAddress(customer1, dto);

        assertNotNull(created.getId());
        assertTrue(created.isDefault(), "First address must automatically become the default");

        List<Address> addresses = addressService.getUserAddresses(customer1);
        assertEquals(1, addresses.size());
    }

    @Test
    @DisplayName("12. IDOR Prevention: Users cannot access or modify another user's address")
    void testAddressOwnershipSecurity() {
        AddressDto dto = new AddressDto();
        dto.setRecipientName("Alice");
        dto.setPhone("9876543210");
        dto.setAddressLine1("Alice Street");
        dto.setLocality("Area");
        dto.setCity("Bengaluru");
        dto.setState("Karnataka");
        dto.setPostalCode("560001");

        Address aliceAddr = addressService.createAddress(customer1, dto);

        // Bob attempts to read or mutate Alice's address
        assertThrows(UnauthorizedAccessException.class, () ->
                addressService.getAddressByIdAndUser(aliceAddr.getId(), customer2)
        );

        assertThrows(UnauthorizedAccessException.class, () ->
                addressService.deleteAddress(customer2, aliceAddr.getId())
        );
    }
}
