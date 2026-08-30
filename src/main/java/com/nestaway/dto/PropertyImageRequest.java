package com.nestaway.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class PropertyImageRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private Boolean isCover = false;

    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;
}