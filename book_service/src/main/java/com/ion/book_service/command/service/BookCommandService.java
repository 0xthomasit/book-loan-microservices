package com.ion.book_service.command.service;

import com.ion.book_service.command.command.CreateBookCommand;
import com.ion.book_service.command.command.DeleteBookCommand;
import com.ion.book_service.command.command.UpdateBookCommand;
import com.ion.book_service.command.data.Book;
import com.ion.book_service.command.data.BookRepository;
import com.ion.common_service.command.RollBackBookStatusCommand;
import com.ion.common_service.command.UpdateBookStatusCommand;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookCommandService {

    @Autowired
    private BookRepository bookRepository;

    public String createBook(CreateBookCommand command) {
        Book book = new Book();
        BeanUtils.copyProperties(command, book);
        bookRepository.save(book);
        return command.getId();
    }

    public String updateBook(UpdateBookCommand command) {
        Book book = bookRepository.findById(command.getId())
                .orElseThrow(() -> new RuntimeException("Book not found with BookId: " + command.getId()));
        book.setName(command.getName());
        book.setAuthor(command.getAuthor());
        book.setIsReady(command.getIsReady());
        bookRepository.save(book);
        return command.getId();
    }

    public String deleteBook(DeleteBookCommand command) {
        bookRepository.findById(command.getId())
                .ifPresent(book -> bookRepository.delete(book));
        return command.getId();
    }

    public String updateBookStatus(UpdateBookStatusCommand command) {
        Book book = bookRepository.findById(command.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with BookId: " + command.getBookId()));
        book.setIsReady(command.getIsReady());
        bookRepository.save(book);
        return command.getBookId();
    }

    public String rollbackBookStatus(RollBackBookStatusCommand command) {
        Book book = bookRepository.findById(command.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with BookId: " + command.getBookId()));
        book.setIsReady(command.getIsReady());
        bookRepository.save(book);
        return command.getBookId();
    }
}
