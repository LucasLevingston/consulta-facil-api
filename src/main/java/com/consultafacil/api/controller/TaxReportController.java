package com.consultafacil.api.controller;

import com.consultafacil.api.dto.tax.TaxReportDTO;
import com.consultafacil.application.port.in.TaxMonthlyReportUseCase;
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

    private final TaxMonthlyReportUseCase taxMonthlyReport;

    @GetMapping("/tax")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    public ResponseEntity<TaxReportDTO> taxReport(
            @RequestParam(required = false) String month) {
        YearMonth ym = parseMonth(month);
        return ResponseEntity.ok(taxMonthlyReport.execute(ym.getYear(), ym.getMonthValue()));
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
