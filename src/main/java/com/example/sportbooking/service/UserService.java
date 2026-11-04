package com.example.sportbooking.service;

import com.example.sportbooking.dto.RegisterDto;
import com.example.sportbooking.entity.User;

import java.util.Optional;

public interface UserService {
    User registerUser(RegisterDto dto);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
}
