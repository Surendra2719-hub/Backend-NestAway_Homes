package com.nestaway.config;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.nestaway.entity.User;
import com.nestaway.exception.ResourceNotFoundException;
import com.nestaway.repository.UserRepository;

@Component
public class AuthUtil {

   private final UserRepository userRepository;

   public AuthUtil(UserRepository userRepository) {
       this.userRepository = userRepository;
   }

   // Token se email nikal ke, DB se us email wale user ki
   // ASLI id return karta hai — URL ke path-variable pe bharosa nahi.
   public Long getCurrentUserId() {

       Authentication authentication =
               SecurityContextHolder.getContext().getAuthentication();

       String email = authentication.getName();

       User user = userRepository.findByEmail(email)
               .orElseThrow(() ->
                       new ResourceNotFoundException(
                               "Logged-in user not found: " + email));

       return user.getId();
   }
}
