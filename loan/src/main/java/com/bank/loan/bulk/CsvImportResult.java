package com.bank.loan.bulk;

import java.util.List;

public record CsvImportResult(
        int totalRows,
        int importedRows,
        int failedRows,
        List<CsvImportRow> rows
) {
}
