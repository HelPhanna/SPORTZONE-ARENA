package com.example.sportbooking.controller.admin;

import com.example.sportbooking.entity.Setting;
import com.example.sportbooking.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingController {

    private final SettingRepository settingRepository;

    @GetMapping
    public String viewSettings(Model model) {
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("settings", settingRepository.findAll());
        return "admin/settings/list";
    }

    @PostMapping
    public String updateSettings(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        params.forEach((key, value) -> {
            if (!key.startsWith("_")) { // ignore spring csrf / internal params
                settingRepository.findById(key).ifPresent(setting -> {
                    setting.setSettingValue(value);
                    settingRepository.save(setting);
                });
            }
        });
        redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully.");
        return "redirect:/admin/settings";
    }
}
