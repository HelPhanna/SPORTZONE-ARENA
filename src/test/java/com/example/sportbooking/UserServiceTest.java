package com.example.sportbooking;

import com.example.sportbooking.dto.RegisterDto;
import com.example.sportbooking.entity.Role;
import com.example.sportbooking.entity.User;
import com.example.sportbooking.entity.UserStatus;
import com.example.sportbooking.exception.BadRequestException;
import com.example.sportbooking.repository.UserRepository;
import com.example.sportbooking.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterDto registerDto;

    @BeforeEach
    void setUp() {
        registerDto = RegisterDto.builder()
                .name("Alex Smith")
                .email("alex@example.com")
                .phone("+85512345678")
                .password("Password123")
                .confirmPassword("Password123")
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded_pass");

        User savedUser = User.builder()
                .id(10L)
                .name("Alex Smith")
                .email("alex@example.com")
                .phone("+85512345678")
                .password("encoded_pass")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(registerDto);

        assertNotNull(result);
        assertEquals("alex@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_PasswordMismatch_ThrowsException() {
        registerDto.setConfirmPassword("differentPass");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userService.registerUser(registerDto));

        assertEquals("Passwords do not match", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail("alex@example.com")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userService.registerUser(registerDto));

        assertEquals("Email is already registered", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_ReturnsUser() {
        User user = User.builder().id(1L).email("alex@example.com").build();
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByEmail("alex@example.com");

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }
}
