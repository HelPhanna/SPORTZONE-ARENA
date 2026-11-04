package com.example.sportbooking.controller.user;

import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoryRepository categoryRepository;
    private final FieldRepository fieldRepository;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("featuredFields", fieldRepository.findAll());
        model.addAttribute("activeMenu", "home");
        return "user/home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("totalFields", fieldRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("activeMenu", "about");
        return "user/about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activeMenu", "contact");
        return "user/contact";
    }

    @org.springframework.web.bind.annotation.PostMapping("/contact")
    public String submitContact(@org.springframework.web.bind.annotation.RequestParam("name") String name,
                                @org.springframework.web.bind.annotation.RequestParam("email") String email,
                                @org.springframework.web.bind.annotation.RequestParam(value = "phone", required = false) String phone,
                                @org.springframework.web.bind.annotation.RequestParam(value = "subject", required = false) String subject,
                                @org.springframework.web.bind.annotation.RequestParam("message") String message,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        if (name == null || name.isBlank() || message == null || message.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide your name and a message.");
            return "redirect:/contact";
        }

        String contactTarget = (phone != null && !phone.isBlank()) ? phone.trim() : email.trim();
        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you, " + name.trim() + "! Your message has been received by our Phnom Penh operations team. We will reach out via " + contactTarget + " shortly.");
        return "redirect:/contact";
    }
}
