package com.nestaway.service;

import com.nestaway.entity.User;

public interface UserService {
	
	User registerUser(User user);
	
	User getUserById(Long id);
	
	User updateUser(Long id, User user);
	
	void deleteUser(Long id);
	
	User registerHost(User user);

	User upgradeToHost(Long userId);

}