package com.spmart.service;

import com.spmart.dto.AddressDto;
import com.spmart.entity.Address;
import com.spmart.entity.User;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.exception.UnauthorizedAccessException;
import com.spmart.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<Address> getUserAddresses(User user) {
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Address getAddressByIdAndUser(Long id, User user) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to access this address.");
        }

        return address;
    }

    public Address createAddress(User user, AddressDto dto) {
        long existingCount = addressRepository.countByUser(user);
        boolean shouldBeDefault = dto.isDefault() || existingCount == 0;

        if (shouldBeDefault) {
            clearDefaultAddress(user);
        }

        Address address = new Address(
                user,
                dto.getRecipientName().trim(),
                dto.getPhone().trim(),
                dto.getAddressLine1().trim(),
                dto.getAddressLine2() != null ? dto.getAddressLine2().trim() : null,
                dto.getLocality().trim(),
                dto.getCity().trim(),
                dto.getState().trim(),
                dto.getPostalCode().trim(),
                dto.getLandmark() != null ? dto.getLandmark().trim() : null,
                shouldBeDefault
        );

        return addressRepository.save(address);
    }

    public Address updateAddress(User user, Long id, AddressDto dto) {
        Address address = getAddressByIdAndUser(id, user);

        if (dto.isDefault() && !address.isDefault()) {
            clearDefaultAddress(user);
            address.setDefault(true);
        }

        address.setRecipientName(dto.getRecipientName().trim());
        address.setPhone(dto.getPhone().trim());
        address.setAddressLine1(dto.getAddressLine1().trim());
        address.setAddressLine2(dto.getAddressLine2() != null ? dto.getAddressLine2().trim() : null);
        address.setLocality(dto.getLocality().trim());
        address.setCity(dto.getCity().trim());
        address.setState(dto.getState().trim());
        address.setPostalCode(dto.getPostalCode().trim());
        address.setLandmark(dto.getLandmark() != null ? dto.getLandmark().trim() : null);

        return addressRepository.save(address);
    }

    public void deleteAddress(User user, Long id) {
        Address address = getAddressByIdAndUser(id, user);
        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remaining.isEmpty()) {
                remaining.get(0).setDefault(true);
                addressRepository.save(remaining.get(0));
            }
        }
    }

    public Address setDefaultAddress(User user, Long id) {
        Address address = getAddressByIdAndUser(id, user);
        clearDefaultAddress(user);
        address.setDefault(true);
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public Address getDefaultAddress(User user) {
        return addressRepository.findByUserAndIsDefaultTrue(user)
                .orElseGet(() -> {
                    List<Address> all = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
                    return all.isEmpty() ? null : all.get(0);
                });
    }

    private void clearDefaultAddress(User user) {
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        for (Address a : addresses) {
            if (a.isDefault()) {
                a.setDefault(false);
                addressRepository.save(a);
            }
        }
    }
}
