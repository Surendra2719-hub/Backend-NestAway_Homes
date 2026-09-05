package com.nestaway.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class HostSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private long propertyCount;
    private LocalDateTime createdAt;
}