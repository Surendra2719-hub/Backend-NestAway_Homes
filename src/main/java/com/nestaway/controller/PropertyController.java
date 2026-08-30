package com.nestaway.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nestaway.dto.PropertyRequest;
import com.nestaway.dto.PropertyResponse;
import com.nestaway.entity.Property;
import com.nestaway.service.PropertyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PreAuthorize("hasRole('HOST')")
    @PostMapping("/host/{hostId}")
    public PropertyResponse addProperty(@PathVariable Long hostId, @Valid @RequestBody PropertyRequest propertyRequest) {

        Property property = convertToProperty(propertyRequest);

        Property savedProperty = propertyService.addProperty(hostId, property);

        return convertToPropertyResponse(savedProperty);
    }
    
    @GetMapping("/{id}")
    public PropertyResponse getPropertyById(@PathVariable Long id) {

        Property property = propertyService.getPropertyById(id);

        return convertToPropertyResponse(property);
    }
    
    @GetMapping
    public List<PropertyResponse> getAllApprovedProperties() {

        return propertyService.getAllApprovedProperties()
                .stream()
                .map(this::convertToPropertyResponse)
                .toList();
    }

    @GetMapping("/search")
    public Page<PropertyResponse> searchProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return propertyService
                .searchProperties(city, minPrice, maxPrice, page, size)
                .map(this::convertToPropertyResponse);
    }
    
    @GetMapping("/host/{hostId}")
    public List<PropertyResponse> getPropertiesByHostId(
            @PathVariable Long hostId) {

        return propertyService.getPropertiesByHostId(hostId)
                .stream()
                .map(this::convertToPropertyResponse)
                .toList();
    }
    
    @PreAuthorize("hasRole('HOST')")
    @PutMapping("/{propertyId}/host/{hostId}")
    public PropertyResponse updateProperty(
            @PathVariable Long propertyId,
            @PathVariable Long hostId,
            @Valid @RequestBody PropertyRequest propertyRequest) {

        Property property = convertToProperty(propertyRequest);

        Property updatedProperty = propertyService.updateProperty(
                propertyId,
                hostId,
                property
        );

        return convertToPropertyResponse(updatedProperty);
    }
    
    @PreAuthorize("hasRole('HOST')")
    @DeleteMapping("/{propertyId}/host/{hostId}")
    public void deleteProperty(
            @PathVariable Long propertyId,
            @PathVariable Long hostId) {

        propertyService.deleteProperty(propertyId, hostId);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{adminId}/pending")
    public List<PropertyResponse> getAllPendingProperties(
            @PathVariable Long adminId) {

        return propertyService.getAllPendingProperties(adminId)
                .stream()
                .map(this::convertToPropertyResponse)
                .toList();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{propertyId}/admin/{adminId}/approve")
    public PropertyResponse approveProperty(
            @PathVariable Long propertyId,
            @PathVariable Long adminId) {

        Property property = propertyService.approveProperty(propertyId, adminId);

        return convertToPropertyResponse(property);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{propertyId}/admin/{adminId}/reject")
    public PropertyResponse rejectProperty(
            @PathVariable Long propertyId,
            @PathVariable Long adminId) {

        Property property = propertyService.rejectProperty(propertyId, adminId);

        return convertToPropertyResponse(property);
    }
    
    

    //=====================================Helper Methods ===========================================
    private Property convertToProperty(PropertyRequest propertyRequest) {

        Property property = new Property();

        property.setTitle(propertyRequest.getTitle());
        property.setDescription(propertyRequest.getDescription());
        property.setPropertyType(propertyRequest.getPropertyType());
        property.setAddress(propertyRequest.getAddress());
        property.setCity(propertyRequest.getCity());
        property.setState(propertyRequest.getState());
        property.setCountry(propertyRequest.getCountry());
        property.setPricePerNight(propertyRequest.getPricePerNight());
        property.setMaxGuests(propertyRequest.getMaxGuests());
        property.setBedrooms(propertyRequest.getBedrooms());
        property.setBeds(propertyRequest.getBeds());
        property.setBathrooms(propertyRequest.getBathrooms());

        return property;
    }

    private PropertyResponse convertToPropertyResponse(Property property) {

        PropertyResponse propertyResponse = new PropertyResponse();

        propertyResponse.setId(property.getId());
        propertyResponse.setTitle(property.getTitle());
        propertyResponse.setDescription(property.getDescription());
        propertyResponse.setPropertyType(property.getPropertyType());
        propertyResponse.setAddress(property.getAddress());
        propertyResponse.setCity(property.getCity());
        propertyResponse.setState(property.getState());
        propertyResponse.setCountry(property.getCountry());
        propertyResponse.setPricePerNight(property.getPricePerNight());
        propertyResponse.setMaxGuests(property.getMaxGuests());
        propertyResponse.setBedrooms(property.getBedrooms());
        propertyResponse.setBeds(property.getBeds());
        propertyResponse.setBathrooms(property.getBathrooms());
        propertyResponse.setStatus(property.getStatus());
        propertyResponse.setHostId(property.getHost().getId());
        propertyResponse.setHostName(property.getHost().getName());
        propertyResponse.setCreatedAt(property.getCreatedAt());
        propertyResponse.setUpdatedAt(property.getUpdatedAt());

        return propertyResponse;
    }
}