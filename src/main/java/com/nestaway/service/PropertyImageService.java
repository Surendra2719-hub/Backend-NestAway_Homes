package com.nestaway.service;

import java.util.List;

import com.nestaway.entity.PropertyImage;

public interface PropertyImageService {

    PropertyImage addImage(
            Long propertyId,
            Long hostId,
            PropertyImage propertyImage);

    List<PropertyImage> getImagesByPropertyId(Long propertyId);

    void deleteImage(Long imageId, Long hostId);
}