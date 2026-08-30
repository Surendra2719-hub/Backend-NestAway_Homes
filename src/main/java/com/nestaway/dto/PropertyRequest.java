package com.nestaway.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PropertyRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Property type is required")
    private String propertyType;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "1.0", message = "Price per night must be greater than zero")
    private BigDecimal pricePerNight;

    @NotNull(message = "Maximum guests is required")
    @Min(value = 1, message = "Maximum guests must be at least 1")
    private Integer maxGuests;

    @Min(value = 1, message = "Bedrooms must be at least 1")
    private Integer bedrooms;

    @Min(value = 1, message = "Beds must be at least 1")
    private Integer beds;

    @Min(value = 1, message = "Bathrooms must be at least 1")
    private Integer bathrooms;
}