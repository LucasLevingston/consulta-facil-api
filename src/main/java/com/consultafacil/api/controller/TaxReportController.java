package com.consultafacil.api.controller;

import com.consultafacil.api.dto.tax.TaxReportDTO;
import com.consultafacil.application.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class TaxReportController {

    private final TaxCalculationService taxCalculationService;

    @GetMapping("/tax")
    @PreAuthorize("@policy.canAccessAdminPanel(authentication)")
    public ResponseEntity<TaxReportDTO> taxReport(
            @RequestParam(required = false) String month) {
        YearMonth ym = parseMonth(month);
        return ResponseEntity.ok(taxCalculationService.monthlyReport(ym.getYear(), ym.getMonthValue()));
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) return YearMonth.now();
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }
}
