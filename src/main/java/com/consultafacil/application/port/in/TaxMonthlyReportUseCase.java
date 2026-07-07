package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.tax.TaxReportDTO;

public interface TaxMonthlyReportUseCase {
    TaxReportDTO execute(int year, int month);
}
