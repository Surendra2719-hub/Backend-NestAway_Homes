package com.nestaway.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nestaway.entity.User;
import com.nestaway.repository.UserRepository;
import com.nestaway.exception.ResourceNotFoundException;
import com.nestaway.exception.UnauthorizedOperationException;
import com.nestaway.exception.UserAlreadyExistsException;
import com.nestaway.config.AuthUtil;

@Service
public class UserServiceImpl implements UserService{
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthUtil authUtil;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthUtil authUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authUtil = authUtil;
    }
	
	@Override
	public User registerUser(User user) {
		    if(userRepository.findByEmail(user.getEmail()).isPresent()) {
		    	throw new UserAlreadyExistsException("Email is already registered");
	        }

	        user.setRole("GUEST");
	        user.setPassword(passwordEncoder.encode(user.getPassword()));

	        return userRepository.save(user);
	}

	@Override
	public User getUserById(Long id) {
		return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	@Override
	public User updateUser(Long id, User user) {
		User existingUser = getUserById(id);

        if (!existingUser.getEmail().equals(user.getEmail())
                && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setPhone(user.getPhone());

        return userRepository.save(existingUser);
	}

	@Override
	public void deleteUser(Long id) {
		User user = getUserById(id);
        userRepository.delete(user);
	}
	
	@Override
	public User registerHost(User user) {

	    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
	        throw new UserAlreadyExistsException("Email is already registered");
	    }

	    user.setRole("HOST");
	    user.setPassword(passwordEncoder.encode(user.getPassword()));

	    return userRepository.save(user);
	}

	@Override
	public User upgradeToHost(Long userId) {

	    // Token ka asli user hi apna account upgrade kar sakta hai, kisi aur ka nahi
	    Long actualLoggedInUserId = authUtil.getCurrentUserId();
	    if (!actualLoggedInUserId.equals(userId)) {
	        throw new UnauthorizedOperationException(
	                "You can only upgrade your own account");
	    }

	    User user = getUserById(userId);

	    if ("HOST".equalsIgnoreCase(user.getRole())) {
	        throw new UserAlreadyExistsException("You are already a host");
	    }

	    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
	        throw new UnauthorizedOperationException("Admin accounts cannot become a host");
	    }

	    user.setRole("HOST");

	    return userRepository.save(user);
	}

}