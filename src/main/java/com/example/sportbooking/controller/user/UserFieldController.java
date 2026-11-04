package com.example.sportbooking.controller.user;

import com.example.sportbooking.dto.DayScheduleDto;
import com.example.sportbooking.dto.SlotDto;
import com.example.sportbooking.entity.Category;
import com.example.sportbooking.entity.CategoryStatus;
import com.example.sportbooking.entity.Field;
import com.example.sportbooking.entity.FieldStatus;
import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldRepository;
import com.example.sportbooking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/fields")
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class UserFieldController {

    private final FieldRepository fieldRepository;
    private final CategoryRepository categoryRepository;
    private final SlotService slotService;

    @GetMapping
    public String listFields(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                             @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                             @RequestParam(value = "sort", defaultValue = "newest") String sort,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "9") int size,
                             Model model) {

        Sort sortObj = switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "pricePerSlot");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "pricePerSlot");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };

        PageRequest pageable = PageRequest.of(page, size, sortObj);
        Page<Field> fieldsPage = fieldRepository.searchActiveFields(categoryId, keyword, minPrice, maxPrice, pageable);
        List<Category> categories = categoryRepository.findByStatusOrderByNameAsc(CategoryStatus.ACTIVE);

        model.addAttribute("fieldsPage", fieldsPage);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentSort", sort);
        model.addAttribute("activeMenu", "fields");

        return "user/fields/list";
    }

    @GetMapping("/{id}")
    public String viewFieldDetail(@PathVariable("id") Long id,
                                  @RequestParam(value = "date", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  Model model) {

        Field field = fieldRepository.findById(id).orElse(null);
        if (field == null || field.getStatus() != FieldStatus.ACTIVE) {
            return "redirect:/fields";
        }

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(7);
        LocalDate selectedDate = (date != null && !date.isBefore(today) && !date.isAfter(maxDate)) ? date : today;

        List<SlotDto> slots = slotService.getSlotsForFieldAndDate(field, selectedDate);
        List<DayScheduleDto> next7Days = slotService.getNext7DaysSchedule(field, selectedDate);
        List<Field> relatedFields = fieldRepository.findByCategoryIdAndStatus(field.getCategory().getId(), FieldStatus.ACTIVE)
                .stream().filter(f -> !f.getId().equals(field.getId())).limit(3).toList();

        model.addAttribute("field", field);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("today", today);
        model.addAttribute("maxDate", maxDate);
        model.addAttribute("next7Days", next7Days);
        model.addAttribute("slots", slots);
        model.addAttribute("relatedFields", relatedFields);
        model.addAttribute("activeMenu", "fields");

        return "user/fields/detail";
    }
}
