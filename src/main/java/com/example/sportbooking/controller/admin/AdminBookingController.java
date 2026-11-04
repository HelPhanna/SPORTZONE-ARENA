package com.example.sportbooking.controller.admin;

import com.example.sportbooking.entity.*;
import com.example.sportbooking.repository.*;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final FieldRepository fieldRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    /**
     * READ (List): Search, filter, and paginate bookings.
     */
    @GetMapping
    public String listBookings(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "fieldId", required = false) Long fieldId,
                               @RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(value = "status", required = false) BookingStatus status,
                               @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "15") int size,
                               Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingDate", "startTime"));
        Page<Booking> bookingsPage = bookingRepository.searchAdminBookings(keyword, fieldId, categoryId, status, startDate, endDate, pageable);

        // Status KPI Counts for quick filter tabs
        long totalBookingsCount = bookingRepository.count();
        long pendingCount = bookingRepository.countByStatus(BookingStatus.PENDING);
        long paidCount = bookingRepository.countByStatus(BookingStatus.PAID);
        long completedCount = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelledCount = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        model.addAttribute("activeMenu", "bookings");
        model.addAttribute("bookingsPage", bookingsPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedFieldId", fieldId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("fields", fieldRepository.findAll());
        model.addAttribute("statuses", BookingStatus.values());

        model.addAttribute("totalBookingsCount", totalBookingsCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "admin/bookings/list";
    }

    /**
     * CREATE (Form): Show Walk-in / Admin booking form.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<Field> activeFields = fieldRepository.findByStatus(FieldStatus.ACTIVE);
        List<User> users = userRepository.findAll();

        model.addAttribute("activeMenu", "bookings");
        model.addAttribute("fields", activeFields);
        model.addAttribute("users", users);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("sources", BookingSource.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("today", LocalDate.now());

        return "admin/bookings/create";
    }

    /**
     * CREATE (Action): Process new booking creation by admin.
     */
    @PostMapping("/create")
    @Transactional
    public String createBooking(@RequestParam("fieldId") Long fieldId,
                                @RequestParam("bookingDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                                @RequestParam(value = "slots", required = false) List<String> selectedSlotStrings,
                                @RequestParam("customerName") String customerName,
                                @RequestParam("customerPhone") String customerPhone,
                                @RequestParam(value = "userId", required = false) Long userId,
                                @RequestParam(value = "status", defaultValue = "PAID") BookingStatus status,
                                @RequestParam(value = "source", defaultValue = "WALK_IN") BookingSource source,
                                @RequestParam(value = "customPrice", required = false) BigDecimal customPrice,
                                @RequestParam(value = "paidAmount", required = false) BigDecimal paidAmount,
                                @RequestParam(value = "paymentMethod", defaultValue = "CASH") PaymentMethod paymentMethod,
                                RedirectAttributes redirectAttributes) {

        Field field = fieldRepository.findById(fieldId).orElse(null);
        if (field == null || field.getStatus() != FieldStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected sport facility is invalid or inactive.");
            return "redirect:/admin/bookings/create";
        }

        if (selectedSlotStrings == null || selectedSlotStrings.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one time slot for the reservation.");
            return "redirect:/admin/bookings/create";
        }

        if (customerName == null || customerName.isBlank() || customerPhone == null || customerPhone.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer name and contact phone number are required.");
            return "redirect:/admin/bookings/create";
        }

        // Parse slots
        List<LocalTime> startTimes = new ArrayList<>();
        List<LocalTime> endTimes = new ArrayList<>();
        for (String slotStr : selectedSlotStrings) {
            String[] parts = slotStr.split("-");
            if (parts.length == 2) {
                startTimes.add(LocalTime.parse(parts[0].trim()));
                endTimes.add(LocalTime.parse(parts[1].trim()));
            }
        }
        Collections.sort(startTimes);
        Collections.sort(endTimes);

        LocalTime earliestStart = startTimes.get(0);
        LocalTime latestEnd = endTimes.get(endTimes.size() - 1);

        // Check if any slot is already booked
        List<BookingSlot> existingSlots = bookingSlotRepository.findByFieldIdAndBookingDate(fieldId, bookingDate);
        Set<LocalTime> bookedTimes = new HashSet<>();
        for (BookingSlot bs : existingSlots) {
            if (bs.getBooking() != null &&
                    bs.getBooking().getStatus() != BookingStatus.CANCELLED &&
                    bs.getBooking().getStatus() != BookingStatus.EXPIRED) {
                bookedTimes.add(bs.getStartTime());
            }
        }

        for (LocalTime st : startTimes) {
            if (bookedTimes.contains(st)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Time slot (" + st + ") is already reserved. Please select an available slot.");
                return "redirect:/admin/bookings/create";
            }
        }

        // Calculate total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (customPrice != null && customPrice.compareTo(BigDecimal.ZERO) >= 0) {
            totalPrice = customPrice;
        } else {
            for (LocalTime st : startTimes) {
                BigDecimal slotPrice = field.getPricePerSlot();
                if (field.getPeakStartTime() != null && field.getPeakPrice() != null) {
                    if (!st.isBefore(field.getPeakStartTime())) {
                        slotPrice = field.getPeakPrice();
                    }
                }
                totalPrice = totalPrice.add(slotPrice);
            }
        }

        // User association
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // Determine paid amount
        BigDecimal finalPaid = paidAmount;
        if (finalPaid == null) {
            finalPaid = (status == BookingStatus.PAID || status == BookingStatus.COMPLETED) ? totalPrice : BigDecimal.ZERO;
        }

        // Unique booking code
        String bookingCode = "SB-" + bookingDate.toString().replace("-", "") + "-"
                + String.format("%05d", new Random().nextInt(100000));

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .field(field)
                .customerName(customerName.trim())
                .customerPhone(customerPhone.trim())
                .bookingDate(bookingDate)
                .startTime(earliestStart)
                .endTime(latestEnd)
                .totalPrice(totalPrice)
                .paidAmount(finalPaid)
                .status(status)
                .source(source)
                .build();

        booking = bookingRepository.save(booking);

        // Save individual booking slots if not cancelled
        if (status != BookingStatus.CANCELLED && status != BookingStatus.EXPIRED) {
            for (int i = 0; i < startTimes.size(); i++) {
                BookingSlot bs = BookingSlot.builder()
                        .booking(booking)
                        .field(field)
                        .bookingDate(bookingDate)
                        .startTime(startTimes.get(i))
                        .endTime(endTimes.get(i))
                        .build();
                bookingSlotRepository.save(bs);
            }
        }

        // Create Payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(finalPaid.compareTo(BigDecimal.ZERO) > 0 ? finalPaid : totalPrice)
                .method(paymentMethod != null ? paymentMethod : PaymentMethod.CASH)
                .status(finalPaid.compareTo(BigDecimal.ZERO) > 0 ? PaymentStatus.SUCCESS : PaymentStatus.PENDING)
                .paidAt(finalPaid.compareTo(BigDecimal.ZERO) > 0 ? LocalDateTime.now() : null)
                .transactionRef("ADM-" + bookingCode)
                .build();
        paymentRepository.save(payment);

        redirectAttributes.addFlashAttribute("successMessage",
                "Booking #" + bookingCode + " created successfully!");
        return "redirect:/admin/bookings/" + booking.getId();
    }

    /**
     * READ (Detail): View comprehensive booking information.
     */
    @GetMapping("/{id}")
    public String viewBookingDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/admin/bookings";
        }

        List<Payment> payments = paymentRepository.findByBookingId(id);
        BigDecimal remainingBalance = booking.getTotalPrice().subtract(booking.getPaidAmount());
        if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            remainingBalance = BigDecimal.ZERO;
        }

        model.addAttribute("activeMenu", "bookings");
        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments);
        model.addAttribute("remainingBalance", remainingBalance);

        return "admin/bookings/detail";
    }

    /**
     * UPDATE (Form): Show edit booking form.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/admin/bookings";
        }

        List<User> users = userRepository.findAll();
        List<Field> fields = fieldRepository.findAll();

        model.addAttribute("activeMenu", "bookings");
        model.addAttribute("booking", booking);
        model.addAttribute("users", users);
        model.addAttribute("fields", fields);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("sources", BookingSource.values());

        return "admin/bookings/edit";
    }

    /**
     * UPDATE (Action): Save modifications to booking.
     */
    @PostMapping("/{id}/edit")
    @Transactional
    public String updateBooking(@PathVariable("id") Long id,
                                @RequestParam("customerName") String customerName,
                                @RequestParam("customerPhone") String customerPhone,
                                @RequestParam(value = "userId", required = false) Long userId,
                                @RequestParam("status") BookingStatus status,
                                @RequestParam(value = "paidAmount", required = false) BigDecimal paidAmount,
                                @RequestParam(value = "totalPrice", required = false) BigDecimal totalPrice,
                                @RequestParam(value = "source", required = false) BookingSource source,
                                RedirectAttributes redirectAttributes) {

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/admin/bookings";
        }

        BookingStatus oldStatus = booking.getStatus();

        booking.setCustomerName(customerName.trim());
        booking.setCustomerPhone(customerPhone.trim());
        if (userId != null) {
            booking.setUser(userRepository.findById(userId).orElse(null));
        } else {
            booking.setUser(null);
        }

        if (totalPrice != null && totalPrice.compareTo(BigDecimal.ZERO) >= 0) {
            booking.setTotalPrice(totalPrice);
        }

        if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) >= 0) {
            booking.setPaidAmount(paidAmount);
        }

        if (source != null) {
            booking.setSource(source);
        }

        booking.setStatus(status);

        // Handle slots according to status transition
        if (status == BookingStatus.CANCELLED || status == BookingStatus.EXPIRED) {
            // Liberate reserved slots so they become bookable again
            bookingSlotRepository.deleteByBookingId(booking.getId());
        } else if (oldStatus == BookingStatus.CANCELLED || oldStatus == BookingStatus.EXPIRED) {
            // Reactivating a cancelled booking: verify slots are not taken
            List<BookingSlot> existingSlots = bookingSlotRepository.findByFieldIdAndBookingDate(
                    booking.getField().getId(), booking.getBookingDate());
            Set<LocalTime> takenTimes = new HashSet<>();
            for (BookingSlot bs : existingSlots) {
                if (bs.getBooking() != null &&
                        !bs.getBooking().getId().equals(booking.getId()) &&
                        bs.getBooking().getStatus() != BookingStatus.CANCELLED &&
                        bs.getBooking().getStatus() != BookingStatus.EXPIRED) {
                    takenTimes.add(bs.getStartTime());
                }
            }

            // Check conflict
            if (takenTimes.contains(booking.getStartTime())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Cannot reactivate booking: slot at " + booking.getStartTime() + " is already taken by another booking.");
                return "redirect:/admin/bookings/" + id + "/edit";
            }

            // Recreate slot
            BookingSlot bs = BookingSlot.builder()
                    .booking(booking)
                    .field(booking.getField())
                    .bookingDate(booking.getBookingDate())
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .build();
            bookingSlotRepository.save(bs);
        }

        // If marked PAID and paidAmount is less than total, synchronize paidAmount
        if (status == BookingStatus.PAID && booking.getPaidAmount().compareTo(booking.getTotalPrice()) < 0) {
            booking.setPaidAmount(booking.getTotalPrice());
        }

        bookingRepository.save(booking);

        redirectAttributes.addFlashAttribute("successMessage",
                "Booking #" + booking.getBookingCode() + " updated successfully!");
        return "redirect:/admin/bookings/" + id;
    }

    /**
     * UPDATE (Quick Status): Convenient 1-click status update.
     */
    @PostMapping("/{id}/status")
    @Transactional
    public String updateBookingStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") BookingStatus status,
                                      @RequestParam(value = "paymentMethod", defaultValue = "CASH") PaymentMethod paymentMethod,
                                      RedirectAttributes redirectAttributes) {

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/admin/bookings";
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(status);

        if (status == BookingStatus.PAID) {
            booking.setPaidAmount(booking.getTotalPrice());

            // Mark pending payment or create new payment record
            List<Payment> payments = paymentRepository.findByBookingId(id);
            if (!payments.isEmpty()) {
                Payment p = payments.get(payments.size() - 1);
                p.setStatus(PaymentStatus.SUCCESS);
                p.setPaidAt(LocalDateTime.now());
                paymentRepository.save(p);
            } else {
                Payment p = Payment.builder()
                        .booking(booking)
                        .amount(booking.getTotalPrice())
                        .method(paymentMethod)
                        .status(PaymentStatus.SUCCESS)
                        .paidAt(LocalDateTime.now())
                        .transactionRef("ADM-PAY-" + booking.getBookingCode())
                        .build();
                paymentRepository.save(p);
            }
        } else if (status == BookingStatus.CANCELLED || status == BookingStatus.EXPIRED) {
            bookingSlotRepository.deleteByBookingId(booking.getId());
        } else if (oldStatus == BookingStatus.CANCELLED || oldStatus == BookingStatus.EXPIRED) {
            // Re-allocate slot
            BookingSlot bs = BookingSlot.builder()
                    .booking(booking)
                    .field(booking.getField())
                    .bookingDate(booking.getBookingDate())
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .build();
            bookingSlotRepository.save(bs);
        }

        bookingRepository.save(booking);

        redirectAttributes.addFlashAttribute("successMessage",
                "Status for Booking #" + booking.getBookingCode() + " changed to " + status.name() + ".");
        return "redirect:/admin/bookings/" + id;
    }

    /**
     * DELETE (Action): Permanently remove booking record.
     */
    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/admin/bookings";
        }

        String code = booking.getBookingCode();
        bookingRepository.delete(booking);

        redirectAttributes.addFlashAttribute("successMessage",
                "Booking #" + code + " has been permanently deleted.");
        return "redirect:/admin/bookings";
    }
}
