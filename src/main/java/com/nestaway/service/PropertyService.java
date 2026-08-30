package com.nestaway.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;

import com.nestaway.entity.Property;

public interface PropertyService {

    Property addProperty(Long hostId, Property property);

    Property getPropertyById(Long id);

    List<Property> getAllApprovedProperties();

    Page<Property> searchProperties(
            String city, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size);

    List<Property> getPropertiesByHostId(Long hostId);

    Property updateProperty(Long propertyId, Long hostId, Property property);

    void deleteProperty(Long propertyId, Long hostId);
    
    List<Property> getAllPendingProperties(Long adminId);

    Property approveProperty(Long propertyId, Long adminId);

    Property rejectProperty(Long propertyId, Long adminId);
}