package com.nestaway.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nestaway.dto.UserRequest;
import com.nestaway.dto.UserResponse;
import com.nestaway.entity.User;
import com.nestaway.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponse registerUser(@Valid @RequestBody UserRequest userRequest) {

        User user = new User();
        String name = userRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            name = userRequest.getEmail().split("@")[0];
        }
        user.setName(name);
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        user.setPhone(userRequest.getPhone());

        User registeredUser = userService.registerUser(user);

        return convertToUserResponse(registeredUser);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {

        User user = userService.getUserById(id);

        return convertToUserResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {

        User user = new User();
        String name = userRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            name = userRequest.getEmail().split("@")[0];
        }
        user.setName(name);
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        user.setPhone(userRequest.getPhone());

        User updatedUser = userService.updateUser(id, user);

        return convertToUserResponse(updatedUser);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
    
    @PostMapping("/register-host")
    public UserResponse registerHost(
            @Valid @RequestBody UserRequest userRequest) {

        User user = new User();
        String name = userRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            name = userRequest.getEmail().split("@")[0];
        }
        user.setName(name);
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        user.setPhone(userRequest.getPhone());

        User registeredHost = userService.registerHost(user);

        return convertToUserResponse(registeredHost);
    }

    @PutMapping("/{id}/upgrade-to-host")
    public UserResponse upgradeToHost(@PathVariable Long id) {

        User upgradedUser = userService.upgradeToHost(id);

        return convertToUserResponse(upgradedUser);
    }

    private UserResponse convertToUserResponse(User user) {

        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setRole(user.getRole());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());

        return userResponse;
    }
}