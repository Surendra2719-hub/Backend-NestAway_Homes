package com.nestaway.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponse {
	
	private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
