package com.nestaway.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nestaway.entity.Property;
import com.nestaway.entity.PropertyImage;
import com.nestaway.exception.ResourceNotFoundException;
import com.nestaway.exception.UnauthorizedOperationException;
import com.nestaway.repository.PropertyImageRepository;
import com.nestaway.repository.PropertyRepository;
import com.nestaway.config.AuthUtil;

@Service
@Transactional
public class PropertyImageServiceImpl implements PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;
    private final PropertyRepository propertyRepository;
    private final AuthUtil authUtil;

    public PropertyImageServiceImpl(
            PropertyImageRepository propertyImageRepository,
            PropertyRepository propertyRepository,
            AuthUtil authUtil) {

        this.propertyImageRepository = propertyImageRepository;
        this.propertyRepository = propertyRepository;
        this.authUtil = authUtil;
    }

    @Override
    public PropertyImage addImage(
            Long propertyId,
            Long hostId,
            PropertyImage propertyImage) {

        verifySameUser(hostId);

        Property property = getOwnedProperty(propertyId, hostId);

        List<PropertyImage> existingImages =
                propertyImageRepository
                        .findByPropertyIdOrderByDisplayOrderAsc(propertyId);

        if (Boolean.TRUE.equals(propertyImage.getIsCover())) {
            existingImages.forEach(image -> image.setIsCover(false));
            propertyImageRepository.saveAll(existingImages);
        }

        if (propertyImage.getDisplayOrder() == null) {
            propertyImage.setDisplayOrder(existingImages.size() + 1);
        }

        propertyImage.setProperty(property);

        return propertyImageRepository.save(propertyImage);
    }

    @Override
    public List<PropertyImage> getImagesByPropertyId(Long propertyId) {

        getPropertyById(propertyId);

        return propertyImageRepository
                .findByPropertyIdOrderByDisplayOrderAsc(propertyId);
    }

    @Override
    public void deleteImage(Long imageId, Long hostId) {

        verifySameUser(hostId);

        PropertyImage propertyImage = propertyImageRepository.findById(imageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property image not found with id: " + imageId));

        if (!propertyImage.getProperty().getHost().getId().equals(hostId)) {
            throw new UnauthorizedOperationException(
                    "You can delete images only from your own property");
        }

        propertyImageRepository.delete(propertyImage);
    }

    private Property getPropertyById(Long propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property not found with id: " + propertyId));
    }

    private Property getOwnedProperty(Long propertyId, Long hostId) {

        Property property = getPropertyById(propertyId);

        if (!property.getHost().getId().equals(hostId)) {
            throw new UnauthorizedOperationException(
                    "You can add images only to your own property");
        }

        return property;
    }

    private void verifySameUser(Long claimedId) {
        Long actualLoggedInUserId = authUtil.getCurrentUserId();
        if (!actualLoggedInUserId.equals(claimedId)) {
            throw new UnauthorizedOperationException(
                    "You are not authorized to perform this action for another user");
        }
    }
}