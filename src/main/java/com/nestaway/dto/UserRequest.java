package com.nestaway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequest {
	
	@NotBlank(message = "Name is required")
	private String name;
	
	@NotBlank(message = "Email is required")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
	        message = "Email must end with @gmail.com")
	private String email;
	
	@NotBlank(message = "Password is required")
	private String password;
	
	private String phone;

}
