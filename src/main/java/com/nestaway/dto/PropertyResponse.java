package com.nestaway.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PropertyResponse {

    private Long id;
    private String title;
    private String description;
    private String propertyType;
    private String address;
    private String city;
    private String state;
    private String country;
    private BigDecimal pricePerNight;
    private Integer maxGuests;
    private Integer bedrooms;
    private Integer beds;
    private Integer bathrooms;
    private String status;
    private Long hostId;
    private String hostName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}