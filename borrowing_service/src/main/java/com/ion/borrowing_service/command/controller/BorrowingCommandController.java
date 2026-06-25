package com.ion.borrowing_service.command.controller;

import com.ion.borrowing_service.command.command.CreateBorrowingCommand;
import com.ion.borrowing_service.command.model.BorrowingCreateModel;
import com.ion.borrowing_service.command.service.BorrowingCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/borrowing")
public class BorrowingCommandController {

    @Autowired
    private BorrowingCommandService borrowingCommandService;
    @PostMapping
    public String createBorrowing(@RequestBody BorrowingCreateModel model) {
        CreateBorrowingCommand command = new CreateBorrowingCommand(
                UUID.randomUUID().toString(),
                model.getBookId(),
                model.getEmployeeId(),
                new Date()
        );
        return borrowingCommandService.createBorrowing(command);
    }
}