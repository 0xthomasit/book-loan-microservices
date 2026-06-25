package com.ion.borrowing_service.command.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBorrowingCommand {

    private String id;

    private String bookId;
    private String employeeId;
    private Date borrowingDate;

}