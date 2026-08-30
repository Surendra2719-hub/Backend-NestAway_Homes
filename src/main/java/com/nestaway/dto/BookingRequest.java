package com.nestaway.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Total guests is required")
    @Min(value = 1, message = "At least 1 guest is required")
    private Integer totalGuests;
    
    private String occupantName;
    
    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String occupantPhone;
}