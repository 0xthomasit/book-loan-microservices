package com.ion.book_service.query.controller;

import com.ion.book_service.query.queries.GetAllBookQuery;
import com.ion.book_service.query.service.BookQueryService;
import com.ion.common_service.model.BookResponseCommonModel;
import com.ion.common_service.queries.GetBookDetailQuery;
import com.ion.common_service.services.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookQueryController {

    @Autowired
    private BookQueryService bookQueryService;
    @Autowired
    private KafkaService kafkaService;

    @GetMapping
    public List<BookResponseCommonModel> getAllBooks() {
        return bookQueryService.getAllBooks(new GetAllBookQuery());
    }

    @PostMapping("/sendMessage")
    public void sendMessage(@RequestBody String message) {
        kafkaService.sendMessage("test", message);
    }

    @GetMapping("{bookId}")
    public BookResponseCommonModel getBookDetail(@PathVariable String bookId) throws Exception {
        return bookQueryService.getBookDetail(new GetBookDetailQuery(bookId));
    }
}