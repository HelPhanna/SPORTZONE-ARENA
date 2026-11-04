package com.example.sportbooking.controller.admin;

import com.example.sportbooking.entity.Booking;
import com.example.sportbooking.entity.BookingStatus;
import com.example.sportbooking.entity.FieldStatus;
import com.example.sportbooking.repository.BookingRepository;
import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldRepository;
import com.example.sportbooking.repository.PaymentRepository;
import com.example.sportbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    private static final List<BookingStatus> REVENUE_STATUSES = List.of(
            BookingStatus.DEPOSIT_PAID,
            BookingStatus.PAID,
            BookingStatus.COMPLETED
    );

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.DEPOSIT_PAID,
            BookingStatus.PAID,
            BookingStatus.COMPLETED
    );

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();

        // Financial & Booking Metrics
        BigDecimal todayRevenue = bookingRepository.sumRevenueOnDate(today, REVENUE_STATUSES);
        BigDecimal totalRevenue = bookingRepository.sumTotalRevenue(REVENUE_STATUSES);
        long todayBookingsCount = bookingRepository.countBookingsOnDate(today, ACTIVE_BOOKING_STATUSES);
        long totalBookingsCount = bookingRepository.count();
        long pendingBookingsCount = bookingRepository.countByStatus(BookingStatus.PENDING);
        long paidBookingsCount = bookingRepository.countByStatus(BookingStatus.PAID);
        long completedBookingsCount = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelledBookingsCount = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        // Facility & User Metrics
        long totalFieldsCount = fieldRepository.count();
        long activeFieldsCount = fieldRepository.findByStatus(FieldStatus.ACTIVE).size();
        long totalUsersCount = userRepository.count();
        long totalCategoriesCount = categoryRepository.count();

        // Recent Bookings (Top 10)
        List<Booking> recentBookings = bookingRepository.findTop10ByOrderByCreatedAtDesc();

        // 7-day trend metrics for interactive chart
        List<String> last7DaysLabels = new ArrayList<>();
        List<Long> last7DaysBookings = new ArrayList<>();
        List<BigDecimal> last7DaysRevenue = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            last7DaysLabels.add(date.format(dayFormatter));
            long count = bookingRepository.countBookingsOnDate(date, ACTIVE_BOOKING_STATUSES);
            BigDecimal rev = bookingRepository.sumRevenueOnDate(date, REVENUE_STATUSES);
            last7DaysBookings.add(count);
            last7DaysRevenue.add(rev != null ? rev : BigDecimal.ZERO);
        }

        List<com.example.sportbooking.entity.Category> allCategories = categoryRepository.findAll();
        List<String> categoryNames = new ArrayList<>();
        List<Integer> categoryFieldCounts = new ArrayList<>();
        for (com.example.sportbooking.entity.Category cat : allCategories) {
            categoryNames.add(cat.getName());
            categoryFieldCounts.add(cat.getFields() != null ? cat.getFields().size() : 0);
        }

        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("today", today);
        model.addAttribute("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("todayBookingsCount", todayBookingsCount);
        model.addAttribute("totalBookingsCount", totalBookingsCount);
        model.addAttribute("pendingBookingsCount", pendingBookingsCount);
        model.addAttribute("paidBookingsCount", paidBookingsCount);
        model.addAttribute("completedBookingsCount", completedBookingsCount);
        model.addAttribute("cancelledBookingsCount", cancelledBookingsCount);
        model.addAttribute("totalFieldsCount", totalFieldsCount);
        model.addAttribute("activeFieldsCount", activeFieldsCount);
        model.addAttribute("totalUsersCount", totalUsersCount);
        model.addAttribute("totalCategoriesCount", totalCategoriesCount);
        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("categories", allCategories);
        model.addAttribute("fields", fieldRepository.findAll());
        model.addAttribute("chartLabels", last7DaysLabels);
        model.addAttribute("chartBookings", last7DaysBookings);
        model.addAttribute("chartRevenue", last7DaysRevenue);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("categoryFieldCounts", categoryFieldCounts);

        return "admin/dashboard";
    }
}
