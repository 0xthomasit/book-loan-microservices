package com.ion.borrowing_service.command.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BorrowingEventListener {

    @EventListener
    public void on(BorrowingCreatedEvent event) {
        log.info("Borrowing created — id: {}, bookId: {}, employeeId: {}, borrowingDate: {}",
                event.getId(), event.getBookId(), event.getEmployeeId(), event.getBorrowingDate());
    }

    @EventListener
    public void on(BorrowingDeletedEvent event) {
        log.info("Borrowing deleted — id: {}", event.getId());
    }
}
