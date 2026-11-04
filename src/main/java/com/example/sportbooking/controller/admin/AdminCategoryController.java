package com.example.sportbooking.controller.admin;

import com.example.sportbooking.entity.Category;
import com.example.sportbooking.entity.CategoryStatus;
import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final FieldRepository fieldRepository;

    /**
     * READ (List): Search and display sports categories.
     */
    @GetMapping
    public String listCategories(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "15") int size,
                                 Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Category> categoriesPage = categoryRepository.searchCategories(keyword, pageable);

        model.addAttribute("activeMenu", "categories");
        model.addAttribute("categoriesPage", categoriesPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", CategoryStatus.values());

        return "admin/categories/list";
    }

    /**
     * CREATE (Form): Show create category form.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("activeMenu", "categories");
        model.addAttribute("statuses", CategoryStatus.values());
        return "admin/categories/create";
    }

    /**
     * CREATE (Action): Save new category.
     */
    @PostMapping("/create")
    @Transactional
    public String createCategory(@RequestParam("name") String name,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "image", required = false) String image,
                                 @RequestParam(value = "status", defaultValue = "ACTIVE") CategoryStatus status,
                                 RedirectAttributes redirectAttributes) {

        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name is required.");
            return "redirect:/admin/categories/create";
        }

        if (categoryRepository.existsByNameIgnoreCase(name.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "A category named '" + name.trim() + "' already exists.");
            return "redirect:/admin/categories/create";
        }

        Category category = Category.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .image(image != null ? image.trim() : null)
                .status(status)
                .build();

        categoryRepository.save(category);

        redirectAttributes.addFlashAttribute("successMessage", "Category '" + category.getName() + "' created successfully!");
        return "redirect:/admin/categories";
    }

    /**
     * UPDATE (Form): Show edit form for existing category.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found.");
            return "redirect:/admin/categories";
        }

        model.addAttribute("activeMenu", "categories");
        model.addAttribute("category", category);
        model.addAttribute("statuses", CategoryStatus.values());

        return "admin/categories/edit";
    }

    /**
     * UPDATE (Action): Update category details.
     */
    @PostMapping("/{id}/edit")
    @Transactional
    public String updateCategory(@PathVariable("id") Long id,
                                 @RequestParam("name") String name,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "image", required = false) String image,
                                 @RequestParam("status") CategoryStatus status,
                                 RedirectAttributes redirectAttributes) {

        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found.");
            return "redirect:/admin/categories";
        }

        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name is required.");
            return "redirect:/admin/categories/" + id + "/edit";
        }

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Another category named '" + name.trim() + "' already exists.");
            return "redirect:/admin/categories/" + id + "/edit";
        }

        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : null);
        category.setImage(image != null ? image.trim() : null);
        category.setStatus(status);

        categoryRepository.save(category);

        redirectAttributes.addFlashAttribute("successMessage", "Category '" + category.getName() + "' updated successfully!");
        return "redirect:/admin/categories";
    }

    /**
     * UPDATE (Quick Status Toggle): Toggle Active / Inactive.
     */
    @PostMapping("/{id}/toggle-status")
    @Transactional
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found.");
            return "redirect:/admin/categories";
        }

        CategoryStatus newStatus = (category.getStatus() == CategoryStatus.ACTIVE) ? CategoryStatus.INACTIVE : CategoryStatus.ACTIVE;
        category.setStatus(newStatus);
        categoryRepository.save(category);

        redirectAttributes.addFlashAttribute("successMessage",
                "Category '" + category.getName() + "' status changed to " + newStatus.name() + ".");
        return "redirect:/admin/categories";
    }

    /**
     * DELETE (Action): Delete sports category.
     */
    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found.");
            return "redirect:/admin/categories";
        }

        long fieldCount = fieldRepository.countByCategoryId(id);
        if (fieldCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete category '" + category.getName() + "': it currently has " + fieldCount + " associated sports fields. Please reassign or delete them first.");
            return "redirect:/admin/categories";
        }

        String catName = category.getName();
        categoryRepository.delete(category);

        redirectAttributes.addFlashAttribute("successMessage", "Category '" + catName + "' deleted successfully!");
        return "redirect:/admin/categories";
    }
}
