package com.example.sportbooking.controller.admin;

import com.example.sportbooking.entity.Payment;
import com.example.sportbooking.entity.PaymentStatus;
import com.example.sportbooking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping
    public String listPayments(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "status", required = false) PaymentStatus status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "15") int size,
                               Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Payment> paymentsPage = paymentRepository.searchPayments(keyword, status, pageable);

        model.addAttribute("activeMenu", "payments");
        model.addAttribute("paymentsPage", paymentsPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PaymentStatus.values());

        return "admin/payments/list";
    }
}
