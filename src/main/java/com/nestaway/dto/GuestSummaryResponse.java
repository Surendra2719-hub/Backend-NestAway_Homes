package com.nestaway.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GuestSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private long bookingCount;
    private LocalDateTime createdAt;
}