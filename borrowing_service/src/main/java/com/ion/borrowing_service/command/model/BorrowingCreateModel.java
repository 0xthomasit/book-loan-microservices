package com.ion.borrowing_service.command.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingCreateModel {

    private String bookId;
    private String employeeId;
    private Date borrowingDate;

}
