package com.example.sportbooking.controller.admin;

import com.example.sportbooking.entity.*;
import com.example.sportbooking.repository.BookingRepository;
import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldImageRepository;
import com.example.sportbooking.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin/fields")
@RequiredArgsConstructor
public class AdminFieldController {

    private final FieldRepository fieldRepository;
    private final CategoryRepository categoryRepository;
    private final BookingRepository bookingRepository;
    private final FieldImageRepository fieldImageRepository;

    /**
     * READ (List): Search and filter facility fields.
     */
    @GetMapping
    public String listFields(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             @RequestParam(value = "status", required = false) FieldStatus status,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "15") int size,
                             Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Field> fieldsPage = fieldRepository.searchAdminFields(keyword, categoryId, status, pageable);

        model.addAttribute("activeMenu", "fields");
        model.addAttribute("fieldsPage", fieldsPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statuses", FieldStatus.values());

        return "admin/fields/list";
    }

    /**
     * CREATE (Form): Show form to add a new sport facility field.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("activeMenu", "fields");
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statuses", FieldStatus.values());
        return "admin/fields/create";
    }

    /**
     * CREATE (Action): Save new sport facility field.
     */
    @PostMapping("/create")
    @Transactional
    public String createField(@RequestParam("name") String name,
                              @RequestParam("categoryId") Long categoryId,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam("pricePerSlot") BigDecimal pricePerSlot,
                              @RequestParam(value = "peakPrice", required = false) BigDecimal peakPrice,
                              @RequestParam(value = "peakStartTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime peakStartTime,
                              @RequestParam(value = "slotMinutes", defaultValue = "60") Integer slotMinutes,
                              @RequestParam("openTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime openTime,
                              @RequestParam("closeTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime closeTime,
                              @RequestParam(value = "depositPercent", defaultValue = "0") Integer depositPercent,
                              @RequestParam(value = "status", defaultValue = "ACTIVE") FieldStatus status,
                              @RequestParam(value = "imageUrl", required = false) String imageUrl,
                              RedirectAttributes redirectAttributes) {

        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid category selected.");
            return "redirect:/admin/fields/create";
        }

        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Field name is required.");
            return "redirect:/admin/fields/create";
        }

        Field field = Field.builder()
                .name(name.trim())
                .category(category)
                .description(description != null ? description.trim() : null)
                .pricePerSlot(pricePerSlot)
                .peakPrice(peakPrice)
                .peakStartTime(peakStartTime)
                .slotMinutes(slotMinutes != null && slotMinutes > 0 ? slotMinutes : 60)
                .openTime(openTime != null ? openTime : LocalTime.of(6, 0))
                .closeTime(closeTime != null ? closeTime : LocalTime.of(23, 0))
                .depositPercent(depositPercent != null ? depositPercent : 0)
                .status(status)
                .build();

        field = fieldRepository.save(field);

        if (imageUrl != null && !imageUrl.isBlank()) {
            FieldImage fi = FieldImage.builder()
                    .field(field)
                    .imagePath(imageUrl.trim())
                    .isMain(true)
                    .sortOrder(0)
                    .build();
            fieldImageRepository.save(fi);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Facility '" + field.getName() + "' created successfully!");
        return "redirect:/admin/fields";
    }

    /**
     * UPDATE (Form): Show edit form for existing field.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Field field = fieldRepository.findById(id).orElse(null);
        if (field == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Field not found.");
            return "redirect:/admin/fields";
        }

        model.addAttribute("activeMenu", "fields");
        model.addAttribute("field", field);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statuses", FieldStatus.values());
        model.addAttribute("mainImageUrl", field.getMainImagePath());

        return "admin/fields/edit";
    }

    /**
     * UPDATE (Action): Save modifications to sports field.
     */
    @PostMapping("/{id}/edit")
    @Transactional
    public String updateField(@PathVariable("id") Long id,
                              @RequestParam("name") String name,
                              @RequestParam("categoryId") Long categoryId,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam("pricePerSlot") BigDecimal pricePerSlot,
                              @RequestParam(value = "peakPrice", required = false) BigDecimal peakPrice,
                              @RequestParam(value = "peakStartTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime peakStartTime,
                              @RequestParam(value = "slotMinutes", defaultValue = "60") Integer slotMinutes,
                              @RequestParam("openTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime openTime,
                              @RequestParam("closeTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime closeTime,
                              @RequestParam(value = "depositPercent", defaultValue = "0") Integer depositPercent,
                              @RequestParam("status") FieldStatus status,
                              @RequestParam(value = "imageUrl", required = false) String imageUrl,
                              RedirectAttributes redirectAttributes) {

        Field field = fieldRepository.findById(id).orElse(null);
        if (field == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Field not found.");
            return "redirect:/admin/fields";
        }

        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid category selected.");
            return "redirect:/admin/fields/" + id + "/edit";
        }

        field.setName(name.trim());
        field.setCategory(category);
        field.setDescription(description != null ? description.trim() : null);
        field.setPricePerSlot(pricePerSlot);
        field.setPeakPrice(peakPrice);
        field.setPeakStartTime(peakStartTime);
        field.setSlotMinutes(slotMinutes != null && slotMinutes > 0 ? slotMinutes : 60);
        field.setOpenTime(openTime != null ? openTime : LocalTime.of(6, 0));
        field.setCloseTime(closeTime != null ? closeTime : LocalTime.of(23, 0));
        field.setDepositPercent(depositPercent != null ? depositPercent : 0);
        field.setStatus(status);

        fieldRepository.save(field);

        // Update image if specified
        if (imageUrl != null && !imageUrl.isBlank()) {
            List<FieldImage> images = fieldImageRepository.findByFieldIdOrderBySortOrderAscIdAsc(id);
            if (!images.isEmpty()) {
                FieldImage mainImg = images.get(0);
                mainImg.setImagePath(imageUrl.trim());
                fieldImageRepository.save(mainImg);
            } else {
                FieldImage fi = FieldImage.builder()
                        .field(field)
                        .imagePath(imageUrl.trim())
                        .isMain(true)
                        .sortOrder(0)
                        .build();
                fieldImageRepository.save(fi);
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Facility '" + field.getName() + "' updated successfully!");
        return "redirect:/admin/fields";
    }

    /**
     * UPDATE (Quick Status Toggle): Toggle Active / Inactive.
     */
    @PostMapping("/{id}/toggle-status")
    @Transactional
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Field field = fieldRepository.findById(id).orElse(null);
        if (field == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Field not found.");
            return "redirect:/admin/fields";
        }

        FieldStatus newStatus = (field.getStatus() == FieldStatus.ACTIVE) ? FieldStatus.INACTIVE : FieldStatus.ACTIVE;
        field.setStatus(newStatus);
        fieldRepository.save(field);

        redirectAttributes.addFlashAttribute("successMessage",
                "Facility '" + field.getName() + "' status changed to " + newStatus.name() + ".");
        return "redirect:/admin/fields";
    }

    /**
     * DELETE (Action): Permanently remove sports facility.
     */
    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteField(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Field field = fieldRepository.findById(id).orElse(null);
        if (field == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Field not found.");
            return "redirect:/admin/fields";
        }

        // Verify if active/future bookings exist
        boolean hasActiveBookings = bookingRepository.existsByFieldIdAndBookingDateGreaterThanEqualAndStatusIn(
                id, LocalDate.now(), List.of(BookingStatus.PENDING, BookingStatus.DEPOSIT_PAID, BookingStatus.PAID));

        if (hasActiveBookings) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete '" + field.getName() + "': active or upcoming bookings exist for this facility. Please complete or cancel them first.");
            return "redirect:/admin/fields";
        }

        String fieldName = field.getName();
        fieldRepository.delete(field);

        redirectAttributes.addFlashAttribute("successMessage", "Facility '" + fieldName + "' was successfully deleted.");
        return "redirect:/admin/fields";
    }
}
