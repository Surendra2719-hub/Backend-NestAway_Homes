package com.nestaway.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BookingResponse {

    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private Long guestId;
    private String guestName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalGuests;
    private String occupantName;
    private String occupantPhone;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}