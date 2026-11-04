package com.example.sportbooking;

import com.example.sportbooking.config.CustomUserDetails;
import com.example.sportbooking.controller.admin.AdminUserController;
import com.example.sportbooking.entity.Role;
import com.example.sportbooking.entity.User;
import com.example.sportbooking.entity.UserStatus;
import com.example.sportbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminUserController adminUserController;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(2L)
                .name("John Player")
                .email("john@sportbooking.com")
                .phone("+85512345678")
                .password("encoded_pass")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        adminUser = User.builder()
                .id(1L)
                .name("Admin Boss")
                .email("admin@sportbooking.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void listUsers_ReturnsListView() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.searchUsers(any(), any(), any(), any(PageRequest.class))).thenReturn(page);

        String view = adminUserController.listUsers(null, null, null, 0, 15, model);

        assertEquals("admin/users/list", view);
        verify(model).addAttribute(eq("usersPage"), eq(page));
    }

    @Test
    void createUser_Success_EncodesPasswordAndRedirects() {
        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("hashedSecret");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String view = adminUserController.createUser(
                "Alex Doe",
                "alex@example.com",
                "+85587654321",
                "Secret123",
                Role.USER,
                UserStatus.ACTIVE,
                redirectAttributes
        );

        assertEquals("redirect:/admin/users", view);
        verify(passwordEncoder).encode("Secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_Success_UpdatesAndRedirects() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String view = adminUserController.updateUser(
                2L,
                "John Player Updated",
                "john@sportbooking.com",
                "+85599887766",
                null, // no password change
                Role.USER,
                UserStatus.ACTIVE,
                redirectAttributes
        );

        assertEquals("redirect:/admin/users", view);
        assertEquals("John Player Updated", testUser.getName());
        assertEquals("+85599887766", testUser.getPhone());
        verify(userRepository).save(testUser);
    }

    @Test
    void toggleStatus_TogglesActiveToBlocked() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        CustomUserDetails currentUser = new CustomUserDetails(adminUser);

        String view = adminUserController.toggleStatus(2L, currentUser, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals(UserStatus.BLOCKED, testUser.getStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    void toggleStatus_SelfBlock_Forbidden() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        CustomUserDetails currentUser = new CustomUserDetails(adminUser);

        String view = adminUserController.toggleStatus(1L, currentUser, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success_WhenNotSelf() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        CustomUserDetails currentUser = new CustomUserDetails(adminUser);

        String view = adminUserController.deleteUser(2L, currentUser, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_SelfDelete_Blocked() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        CustomUserDetails currentUser = new CustomUserDetails(adminUser);

        String view = adminUserController.deleteUser(1L, currentUser, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        verify(userRepository, never()).delete(any(User.class));
    }
}
