package com.nestaway.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nestaway.dto.PropertyImageRequest;
import com.nestaway.dto.PropertyImageResponse;
import com.nestaway.entity.PropertyImage;
import com.nestaway.service.PropertyImageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PropertyImageController {

    private final PropertyImageService propertyImageService;

    public PropertyImageController(PropertyImageService propertyImageService) {
        this.propertyImageService = propertyImageService;
    }

    @PreAuthorize("hasRole('HOST')")
    @PostMapping("/properties/{propertyId}/host/{hostId}/images")
    public PropertyImageResponse addImage(
            @PathVariable Long propertyId,
            @PathVariable Long hostId,
            @Valid @RequestBody PropertyImageRequest propertyImageRequest) {

        PropertyImage propertyImage = convertToPropertyImage(propertyImageRequest);

        PropertyImage savedImage = propertyImageService.addImage(
                propertyId,
                hostId,
                propertyImage
        );

        return convertToPropertyImageResponse(savedImage);
    }

    @GetMapping("/properties/{propertyId}/images")
    public List<PropertyImageResponse> getImagesByPropertyId(
            @PathVariable Long propertyId) {

        return propertyImageService.getImagesByPropertyId(propertyId)
                .stream()
                .map(this::convertToPropertyImageResponse)
                .toList();
    }

    @PreAuthorize("hasRole('HOST')")
    @DeleteMapping("/images/{imageId}/host/{hostId}")
    public String deleteImage(
            @PathVariable Long imageId,
            @PathVariable Long hostId) {

        propertyImageService.deleteImage(imageId, hostId);

        return "Property image deleted successfully";
    }

    //=====================================Helper Methods ===========================================
    private PropertyImage convertToPropertyImage(PropertyImageRequest propertyImageRequest) {

        PropertyImage propertyImage = new PropertyImage();

        propertyImage.setImageUrl(propertyImageRequest.getImageUrl());
        propertyImage.setIsCover(propertyImageRequest.getIsCover());
        propertyImage.setDisplayOrder(propertyImageRequest.getDisplayOrder());

        return propertyImage;
    }

    private PropertyImageResponse convertToPropertyImageResponse(PropertyImage propertyImage) {

        PropertyImageResponse propertyImageResponse = new PropertyImageResponse();

        propertyImageResponse.setId(propertyImage.getId());
        propertyImageResponse.setImageUrl(propertyImage.getImageUrl());
        propertyImageResponse.setIsCover(propertyImage.getIsCover());
        propertyImageResponse.setDisplayOrder(propertyImage.getDisplayOrder());
        propertyImageResponse.setPropertyId(propertyImage.getProperty().getId());
        propertyImageResponse.setCreatedAt(propertyImage.getCreatedAt());

        return propertyImageResponse;
    }
}