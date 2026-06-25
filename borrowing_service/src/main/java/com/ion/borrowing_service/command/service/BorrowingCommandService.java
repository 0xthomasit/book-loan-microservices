package com.ion.borrowing_service.command.service;

import com.ion.borrowing_service.command.command.CreateBorrowingCommand;
import com.ion.borrowing_service.command.command.DeleteBorrowingCommand;
import com.ion.borrowing_service.command.data.Borrowing;
import com.ion.borrowing_service.command.data.BorrowingRepository;
import com.ion.common_service.model.BookResponseCommonModel;
import com.ion.common_service.model.EmployeeResponseCommonModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Transactional
public class BorrowingCommandService {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public String createBorrowing(CreateBorrowingCommand command) {
        // Step 1: Fetch book details and check availability
        BookResponseCommonModel book = webClientBuilder.build()
                .get()
                .uri("http://book-service/api/v1/books/" + command.getBookId())
                .retrieve()
                .bodyToMono(BookResponseCommonModel.class)
                .block();

        if (book == null || !book.getIsReady()) {
            throw new RuntimeException("This book has been borrowed by someone!!");
        }

        // Step 2: Fetch employee details and check discipline status
        EmployeeResponseCommonModel employee = webClientBuilder.build()
                .get()
                .uri("http://employee-service/api/v1/employees/" + command.getEmployeeId())
                .retrieve()
                .bodyToMono(EmployeeResponseCommonModel.class)
                .block();

        if (employee == null || employee.getIsDisciplined()) {
            throw new RuntimeException("This employee is disciplined and not allowed to borrow!!");
        }

        // Steps 3 & 4 are wrapped together — if either fails after book status was updated, roll back
        boolean bookStatusUpdated = false;
        try {
            // Step 3: Update book status to not ready (isReady = false)
            webClientBuilder.build()
                    .put()
                    .uri("http://book-service/api/internal/books/" + command.getBookId() + "/status")
                    .bodyValue(new BookStatusRequest(false, command.getEmployeeId(), command.getId()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            bookStatusUpdated = true;

            // Step 4: Save borrowing record
            Borrowing borrowing = new Borrowing();
            borrowing.setId(command.getId());
            borrowing.setBookId(command.getBookId());
            borrowing.setEmployeeId(command.getEmployeeId());
            borrowing.setBorrowingDate(command.getBorrowingDate());
            borrowingRepository.save(borrowing);
            log.info("Borrowing created successfully for bookId: {}, employeeId: {}", command.getBookId(), command.getEmployeeId());
            return command.getId();
        } catch (Exception ex) {
            // Rollback book status if it was already updated
            if (bookStatusUpdated) {
                log.error("Failed after book status update, rolling back. Error: {}", ex.getMessage());
                webClientBuilder.build()
                        .put()
                        .uri("http://book-service/api/internal/books/" + command.getBookId() + "/status/rollback")
                        .bodyValue(new BookStatusRequest(true, command.getEmployeeId(), command.getId()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
            throw new RuntimeException("Failed to create borrowing: " + ex.getMessage(), ex);
        }
    }

    public String deleteBorrowing(DeleteBorrowingCommand command) {
        borrowingRepository.findById(command.getId())
                .ifPresent(borrowing -> borrowingRepository.delete(borrowing));
        return command.getId();
    }

    // Inner record used as request body for book status updates
    public record BookStatusRequest(Boolean isReady, String employeeId, String borrowingId) {}
}

