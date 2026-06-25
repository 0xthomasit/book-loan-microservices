package com.ion.book_service.command.controller;

import com.ion.book_service.command.service.BookCommandService;
import com.ion.common_service.command.RollBackBookStatusCommand;
import com.ion.common_service.command.UpdateBookStatusCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/books")
public class BookInternalController {

    @Autowired
    private BookCommandService bookCommandService;

    @PutMapping("/{bookId}/status")
    public String updateBookStatus(@PathVariable String bookId, @RequestBody UpdateBookStatusCommand command) {
        command.setBookId(bookId);
        return bookCommandService.updateBookStatus(command);
    }

    @PutMapping("/{bookId}/status/rollback")
    public String rollbackBookStatus(@PathVariable String bookId, @RequestBody RollBackBookStatusCommand command) {
        command.setBookId(bookId);
        return bookCommandService.rollbackBookStatus(command);
    }

}
