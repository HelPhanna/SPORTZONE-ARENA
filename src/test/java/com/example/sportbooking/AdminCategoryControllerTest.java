package com.example.sportbooking;

import com.example.sportbooking.controller.admin.AdminCategoryController;
import com.example.sportbooking.entity.Category;
import com.example.sportbooking.entity.CategoryStatus;
import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryControllerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminCategoryController adminCategoryController;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Tennis")
                .description("Tennis courts")
                .image("/img/categories/tennis.jpg")
                .status(CategoryStatus.ACTIVE)
                .build();
    }

    @Test
    void listCategories_ReturnsListView() {
        Page<Category> page = new PageImpl<>(List.of(testCategory));
        when(categoryRepository.searchCategories(any(), any(PageRequest.class))).thenReturn(page);

        String view = adminCategoryController.listCategories(null, 0, 15, model);

        assertEquals("admin/categories/list", view);
        verify(model).addAttribute(eq("categoriesPage"), eq(page));
    }

    @Test
    void createCategory_Success_RedirectsToList() {
        when(categoryRepository.existsByNameIgnoreCase("Basketball")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        String view = adminCategoryController.createCategory(
                "Basketball",
                "Indoor and outdoor basketball courts",
                "/img/categories/basketball.jpg",
                CategoryStatus.ACTIVE,
                redirectAttributes
        );

        assertEquals("redirect:/admin/categories", view);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_Success_RedirectsToList() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Pro Tennis", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        String view = adminCategoryController.updateCategory(
                1L,
                "Pro Tennis",
                "Updated description",
                "/img/categories/tennis.jpg",
                CategoryStatus.ACTIVE,
                redirectAttributes
        );

        assertEquals("redirect:/admin/categories", view);
        assertEquals("Pro Tennis", testCategory.getName());
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void toggleStatus_TogglesActiveToInactive() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        String view = adminCategoryController.toggleStatus(1L, redirectAttributes);

        assertEquals("redirect:/admin/categories", view);
        assertEquals(CategoryStatus.INACTIVE, testCategory.getStatus());
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void deleteCategory_Success_WhenNoFieldsAssigned() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(fieldRepository.countByCategoryId(1L)).thenReturn(0L);

        String view = adminCategoryController.deleteCategory(1L, redirectAttributes);

        assertEquals("redirect:/admin/categories", view);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_Blocks_WhenFieldsAssigned() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(fieldRepository.countByCategoryId(1L)).thenReturn(3L);

        String view = adminCategoryController.deleteCategory(1L, redirectAttributes);

        assertEquals("redirect:/admin/categories", view);
        verify(categoryRepository, never()).delete(testCategory);
    }
}
