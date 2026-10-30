package com.bank.loan.customer;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomerCsvRepresentation {
    @CsvBindByName(column = "fullName")
    private String fullName;

    @CsvBindByName(column = "nationalId")
    private String nationalId;

    @CsvBindByName(column = "phoneNumber")
    private String phone;

    @CsvBindByName(column = "email")
    private String email;
}
