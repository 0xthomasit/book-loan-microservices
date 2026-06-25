package com.ion.book_service.query.service;

import com.ion.book_service.command.data.Book;
import com.ion.book_service.command.data.BookRepository;
import com.ion.book_service.query.queries.GetAllBookQuery;
import com.ion.common_service.model.BookResponseCommonModel;
import com.ion.common_service.queries.GetBookDetailQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookQueryService {

    @Autowired
    private BookRepository bookRepository;

    public List<BookResponseCommonModel> getAllBooks(GetAllBookQuery query) {
        List<Book> list = bookRepository.findAll();
        List<BookResponseCommonModel> listBookResponse = new ArrayList<>();
        list.forEach(book -> {
            BookResponseCommonModel model = new BookResponseCommonModel();
            BeanUtils.copyProperties(book, model);
            listBookResponse.add(model);
        });
        return listBookResponse;
    }

    public BookResponseCommonModel getBookDetail(GetBookDetailQuery query) throws Exception {
        BookResponseCommonModel bookResponseModel = new BookResponseCommonModel();
        Book book = bookRepository
                .findById(query.getId())
                .orElseThrow(() -> new Exception(
                        "Book not found with BookId: " + query.getId()
                ));
        BeanUtils.copyProperties(book, bookResponseModel);
        return bookResponseModel;
    }
}
