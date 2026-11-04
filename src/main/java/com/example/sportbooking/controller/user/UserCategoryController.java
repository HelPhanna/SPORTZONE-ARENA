package com.example.sportbooking.controller.user;

import com.example.sportbooking.entity.Category;
import com.example.sportbooking.entity.CategoryStatus;
import com.example.sportbooking.entity.Field;
import com.example.sportbooking.entity.FieldStatus;
import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class UserCategoryController {

    private final CategoryRepository categoryRepository;
    private final FieldRepository fieldRepository;

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryRepository.findActiveCategoriesWithFields(CategoryStatus.ACTIVE);
        model.addAttribute("categories", categories);
        model.addAttribute("activeMenu", "categories");
        return "user/categories/list";
    }

    @GetMapping("/{id}")
    public String viewCategory(@PathVariable("id") Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null || category.getStatus() != CategoryStatus.ACTIVE) {
            return "redirect:/categories";
        }

        List<Field> fields = fieldRepository.findByCategoryIdAndStatus(id, FieldStatus.ACTIVE);
        model.addAttribute("category", category);
        model.addAttribute("fields", fields);
        model.addAttribute("activeMenu", "categories");
        return "user/categories/detail";
    }
}
