package com.ion.borrowing_service.command.saga;

import com.ion.borrowing_service.command.command.DeleteBorrowingCommand;
import com.ion.borrowing_service.command.event.BorrowingCreatedEvent;
import com.ion.borrowing_service.command.event.BorrowingDeletedEvent;
import com.ion.common_service.command.RollBackBookStatusCommand;
import com.ion.common_service.command.UpdateBookStatusCommand;
import com.ion.common_service.event.BookStatusRollBackedEvent;
import com.ion.common_service.event.BookStatusUpdatedEvent;
import com.ion.common_service.model.BookResponseCommonModel;
import com.ion.common_service.model.EmployeeResponseCommonModel;
import com.ion.common_service.queries.GetBookDetailQuery;
import com.ion.common_service.queries.GetEmployeeDetailQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Saga
public class BorrowingSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingCreatedEvent event) {
        log.info("BorrowingCreatedEvent in saga for BookId: {} : EmployeeId: {}", event.getBookId(), event.getEmployeeId());
        try {
            GetBookDetailQuery getBookDetailQuery = new GetBookDetailQuery(event.getBookId());
            BookResponseCommonModel bookResponseCommonModel = queryGateway.query(
                    getBookDetailQuery,
                    ResponseTypes.instanceOf(BookResponseCommonModel.class)
            ).join();

            if (!bookResponseCommonModel.getIsReady()) {
                throw new Exception("This book has been borrowed by someone!!");
            } else {
                SagaLifecycle.associateWith("bookId", event.getBookId());
                UpdateBookStatusCommand command = new UpdateBookStatusCommand(
                        event.getBookId(),
                        false,
                        event.getEmployeeId(),
                        event.getId()
                );

                commandGateway.sendAndWait(command);
            }
        } catch (Exception ex) {
            rollbackBorrowingRecord(event.getId());
            log.error(ex.getMessage());
        }
    }

    @SagaEventHandler(associationProperty = "bookId")
    private void handler(BookStatusUpdatedEvent event) {
        log.info("BookStatusUpdatedEvent in Saga for BookId : {}", event.getBookId());
        try {
            GetEmployeeDetailQuery query = new GetEmployeeDetailQuery(event.getEmployeeId());
            EmployeeResponseCommonModel employeeModel = queryGateway.query(
                    query,
                    ResponseTypes.instanceOf(EmployeeResponseCommonModel.class)
            ).join();

            if (employeeModel.getIsDisciplined()) {
                throw new Exception("This employee is disciplined and not allowed to borrow!!");
            } else {
                log.info("Borrowed successfully.");
                SagaLifecycle.end();
            }
        } catch (Exception ex) {
            rollBackBookStatus(event.getBookId(), event.getEmployeeId(), event.getBorrowingId());
            log.error(ex.getMessage());
        }
    }

    private void rollbackBorrowingRecord(String id) {
        DeleteBorrowingCommand command = new DeleteBorrowingCommand(id);
        commandGateway.sendAndWait(command);
    }

    private void rollBackBookStatus(String bookId, String employeeId, String borrowingId) {
        SagaLifecycle.associateWith("bookId", bookId);
        RollBackBookStatusCommand command = new RollBackBookStatusCommand(
                bookId,
                true,
                employeeId,
                borrowingId
        );

        commandGateway.sendAndWait(command);
    }

    @SagaEventHandler(associationProperty = "bookId")
    private void handle(BookStatusRollBackedEvent event) {
        log.info("BookStatusRollBackedEvent in Saga for book Id : {} ", event.getBookId());
        rollbackBorrowingRecord(event.getBorrowingId());
    }

    @SagaEventHandler(associationProperty = "id")
    @EndSaga
    private void handle(BorrowingDeletedEvent event) {
        log.info("BorrowingDeletedEvent in Saga for Borrowing Id : {} ", event.getId());
        SagaLifecycle.end();
    }

}
