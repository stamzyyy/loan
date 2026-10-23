package com.bank.loan.bulk;

public record CsvImportRow(int rowNumber, String status, String message, Object data) {
}
