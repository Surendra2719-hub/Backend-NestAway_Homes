package com.nestaway.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import com.nestaway.entity.Property;
import com.nestaway.entity.User;
import com.nestaway.exception.ResourceNotFoundException;
import com.nestaway.exception.UnauthorizedOperationException;
import com.nestaway.repository.PropertyRepository;
import com.nestaway.repository.UserRepository;
import com.nestaway.config.AuthUtil;

@Service
@Transactional
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    public PropertyServiceImpl(
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            AuthUtil authUtil) {

        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.authUtil = authUtil;
    }

    @Override
    public Property addProperty(Long hostId, Property property) {

        User host = getHostById(hostId);
        verifySameUser(hostId);

        property.setHost(host);
        property.setStatus("PENDING");

        return propertyRepository.save(property);
    }

    @Override
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found with id: " + id));
    }

    @Override
    public List<Property> getAllApprovedProperties() {
        return propertyRepository.findByStatus("APPROVED");
    }

    @Override
    public Page<Property> searchProperties(
            String city, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return propertyRepository.searchApprovedProperties(
                city, minPrice, maxPrice, pageable);
    }

    @Override
    public List<Property> getPropertiesByHostId(Long hostId) {

        getHostById(hostId);

        return propertyRepository.findByHostId(hostId);
    }

    @Override
    public Property updateProperty(
            Long propertyId,
            Long hostId,
            Property property) {

        getHostById(hostId);
        verifySameUser(hostId);

        Property existingProperty = getPropertyById(propertyId);

        if (!existingProperty.getHost().getId().equals(hostId)) {
            throw new UnauthorizedOperationException(
                    "You can update only your own property");
        }

        existingProperty.setTitle(property.getTitle());
        existingProperty.setDescription(property.getDescription());
        existingProperty.setPropertyType(property.getPropertyType());
        existingProperty.setAddress(property.getAddress());
        existingProperty.setCity(property.getCity());
        existingProperty.setState(property.getState());
        existingProperty.setCountry(property.getCountry());
        existingProperty.setPricePerNight(property.getPricePerNight());
        existingProperty.setMaxGuests(property.getMaxGuests());
        existingProperty.setBedrooms(property.getBedrooms());
        existingProperty.setBeds(property.getBeds());
        existingProperty.setBathrooms(property.getBathrooms());

        return propertyRepository.save(existingProperty);
    }

    @Override
    public void deleteProperty(Long propertyId, Long hostId) {

        getHostById(hostId);
        verifySameUser(hostId);

        Property property = getPropertyById(propertyId);

        if (!property.getHost().getId().equals(hostId)) {
            throw new UnauthorizedOperationException(
                    "You can delete only your own property");
        }

        propertyRepository.delete(property);
    }

    // Yeh check karta hai ki JWT token wala asli user, wahi hai
    // jiska ID URL mein path-variable ke roop mein bheja gaya hai.
    private void verifySameUser(Long claimedId) {
        Long actualLoggedInUserId = authUtil.getCurrentUserId();
        if (!actualLoggedInUserId.equals(claimedId)) {
            throw new UnauthorizedOperationException(
                    "You are not authorized to perform this action for another user");
        }
    }

    private User getHostById(Long hostId) {

        User user = userRepository.findById(hostId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + hostId));

        if (!"HOST".equalsIgnoreCase(user.getRole())) {
            throw new UnauthorizedOperationException(
                    "Only users with HOST role can perform this action");
        }

        return user;
    }
    
    @Override
    public List<Property> getAllPendingProperties(Long adminId) {

        getAdminById(adminId);

        return propertyRepository.findByStatus("PENDING");
    }

    @Override
    public Property approveProperty(Long propertyId, Long adminId) {

        getAdminById(adminId);

        Property property = getPropertyById(propertyId);
        property.setStatus("APPROVED");

        return propertyRepository.save(property);
    }

    @Override
    public Property rejectProperty(Long propertyId, Long adminId) {

        getAdminById(adminId);

        Property property = getPropertyById(propertyId);
        property.setStatus("REJECTED");

        return propertyRepository.save(property);
    }
    
    
    
    private User getAdminById(Long adminId) {

        User user = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + adminId));

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new UnauthorizedOperationException(
                    "Only users with ADMIN role can perform this action");
        }

        return user;
    }
}