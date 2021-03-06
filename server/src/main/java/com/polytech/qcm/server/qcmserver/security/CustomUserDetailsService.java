package com.polytech.qcm.server.qcmserver.security;

import com.polytech.qcm.server.qcmserver.exception.NotFoundException;
import com.polytech.qcm.server.qcmserver.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component //used by spring boot to get users when checking for authentication
public class CustomUserDetailsService implements UserDetailsService {

  private UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws NotFoundException {
    return this.userRepository.findByUsername(username)
      .orElseThrow(() -> new NotFoundException("Username: " + username + " not found"));
  }
}