package com.bank.loan.bulk;

import java.util.ArrayList;
import java.util.List;

final class CsvParser {
    private CsvParser() {
    }

    static List<List<String>> parse(String input) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean afterQuote = false;

        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);
            if (inQuotes) {
                if (character == '"') {
                    if (index + 1 < input.length() && input.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        inQuotes = false;
                        afterQuote = true;
                    }
                } else {
                    field.append(character);
                }
                continue;
            }

            if (afterQuote) {
                if (character == ',') {
                    addField(record, field);
                    afterQuote = false;
                } else if (character == '\n' || character == '\r') {
                    addField(record, field);
                    addRecord(records, record);
                    afterQuote = false;
                    if (character == '\r' && index + 1 < input.length() && input.charAt(index + 1) == '\n') {
                        index++;
                    }
                } else if (!Character.isWhitespace(character)) {
                    throw new IllegalArgumentException("Unexpected character after a quoted CSV field");
                }
                continue;
            }

            if (character == '"') {
                if (!field.isEmpty()) {
                    throw new IllegalArgumentException("Quote must begin at the start of a CSV field");
                }
                inQuotes = true;
            } else if (character == ',') {
                addField(record, field);
            } else if (character == '\n' || character == '\r') {
                addField(record, field);
                addRecord(records, record);
                if (character == '\r' && index + 1 < input.length() && input.charAt(index + 1) == '\n') {
                    index++;
                }
            } else {
                field.append(character);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException("CSV contains an unterminated quoted field");
        }
        if (!field.isEmpty() || !record.isEmpty() || afterQuote) {
            addField(record, field);
            addRecord(records, record);
        }
        if (records.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }
        return records;
    }

    private static void addField(List<String> record, StringBuilder field) {
        record.add(field.toString().trim());
        field.setLength(0);
    }

    private static void addRecord(List<List<String>> records, List<String> record) {
        if (record.stream().anyMatch(value -> !value.isBlank())) {
            records.add(List.copyOf(record));
        }
        record.clear();
    }
}
