package com.nestaway.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PropertyImageResponse {

    private Long id;
    private String imageUrl;
    private Boolean isCover;
    private Integer displayOrder;
    private Long propertyId;
    private LocalDateTime createdAt;
}