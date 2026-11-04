package com.example.sportbooking.controller.user;

import com.example.sportbooking.config.CustomUserDetails;
import com.example.sportbooking.entity.BookingStatus;
import com.example.sportbooking.entity.User;
import com.example.sportbooking.repository.BookingRepository;
import com.example.sportbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        long activeBookings = bookingRepository.countByUserIdAndStatusInAndBookingDateGreaterThanEqual(
                user.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.DEPOSIT_PAID, BookingStatus.PAID),
                LocalDate.now()
        );

        model.addAttribute("user", user);
        model.addAttribute("activeBookingsCount", activeBookings);
        model.addAttribute("activeMenu", "profile");

        return "user/profile";
    }

    @PostMapping("/update")
    @Transactional
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("name") String name,
                                @RequestParam(value = "phone", required = false) String phone,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (name == null || name.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Name must be at least 2 characters.");
            return "redirect:/profile";
        }

        user.setName(name.trim());
        if (phone != null) {
            user.setPhone(phone.trim());
        }
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Profile details updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    @Transactional
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
            return "redirect:/profile";
        }

        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters.");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        return "redirect:/profile";
    }
}
