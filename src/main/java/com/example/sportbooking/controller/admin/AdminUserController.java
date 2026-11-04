package com.example.sportbooking.controller.admin;

import com.example.sportbooking.config.CustomUserDetails;
import com.example.sportbooking.entity.Role;
import com.example.sportbooking.entity.User;
import com.example.sportbooking.entity.UserStatus;
import com.example.sportbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * READ (List): Search and filter customer and admin user accounts.
     */
    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "role", required = false) Role role,
                            @RequestParam(value = "status", required = false) UserStatus status,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "15") int size,
                            Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> usersPage = userRepository.searchUsers(keyword, role, status, pageable);

        model.addAttribute("activeMenu", "users");
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("roles", Role.values());
        model.addAttribute("statuses", UserStatus.values());

        return "admin/users/list";
    }

    /**
     * CREATE (Form): Show create user account form.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("activeMenu", "users");
        model.addAttribute("roles", Role.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/users/create";
    }

    /**
     * CREATE (Action): Create user/administrator account.
     */
    @PostMapping("/create")
    @Transactional
    public String createUser(@RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam("password") String password,
                             @RequestParam(value = "role", defaultValue = "USER") Role role,
                             @RequestParam(value = "status", defaultValue = "ACTIVE") UserStatus status,
                             RedirectAttributes redirectAttributes) {

        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Name, email, and password are required.");
            return "redirect:/admin/users/create";
        }

        String trimmedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(trimmedEmail)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email '" + trimmedEmail + "' is already registered.");
            return "redirect:/admin/users/create";
        }

        User user = User.builder()
                .name(name.trim())
                .email(trimmedEmail)
                .phone(phone != null ? phone.trim() : null)
                .password(passwordEncoder.encode(password))
                .role(role)
                .status(status)
                .build();

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Account for '" + user.getName() + "' created successfully!");
        return "redirect:/admin/users";
    }

    /**
     * UPDATE (Form): Show edit form for user account.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        model.addAttribute("activeMenu", "users");
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        model.addAttribute("statuses", UserStatus.values());

        return "admin/users/edit";
    }

    /**
     * UPDATE (Action): Save modified user account details.
     */
    @PostMapping("/{id}/edit")
    @Transactional
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "newPassword", required = false) String newPassword,
                             @RequestParam("role") Role role,
                             @RequestParam("status") UserStatus status,
                             RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        String trimmedEmail = email.trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(trimmedEmail) && userRepository.existsByEmail(trimmedEmail)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email '" + trimmedEmail + "' is already in use by another account.");
            return "redirect:/admin/users/" + id + "/edit";
        }

        user.setName(name.trim());
        user.setEmail(trimmedEmail);
        user.setPhone(phone != null ? phone.trim() : null);
        user.setRole(role);
        user.setStatus(status);

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "User account '" + user.getName() + "' updated successfully!");
        return "redirect:/admin/users";
    }

    /**
     * UPDATE (Quick Status Toggle): Toggle Active / Blocked.
     */
    @PostMapping("/{id}/toggle-status")
    @Transactional
    public String toggleStatus(@PathVariable("id") Long id,
                               @AuthenticationPrincipal CustomUserDetails currentUser,
                               RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot block your own administrator account.");
            return "redirect:/admin/users";
        }

        UserStatus newStatus = (user.getStatus() == UserStatus.ACTIVE) ? UserStatus.BLOCKED : UserStatus.ACTIVE;
        user.setStatus(newStatus);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage",
                "User '" + user.getName() + "' status changed to " + newStatus.name() + ".");
        return "redirect:/admin/users";
    }

    /**
     * DELETE (Action): Delete user account.
     */
    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteUser(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails currentUser,
                             RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own administrator account.");
            return "redirect:/admin/users";
        }

        String userName = user.getName();
        userRepository.delete(user);

        redirectAttributes.addFlashAttribute("successMessage", "Account for '" + userName + "' was deleted successfully.");
        return "redirect:/admin/users";
    }
}
