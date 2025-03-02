package com.ion.book_service.command.aggregate;

import com.ion.book_service.command.command.CreateBookCommand;
import com.ion.book_service.command.command.DeleteBookCommand;
import com.ion.book_service.command.command.UpdateBookCommand;
import com.ion.book_service.command.event.BookCreatedEvent;
import com.ion.book_service.command.event.BookDeletedEvent;
import com.ion.book_service.command.event.BookUpdatedEvent;
import com.ion.common_service.command.RollBackBookStatusCommand;
import com.ion.common_service.command.UpdateBookStatusCommand;
import com.ion.common_service.event.BookRollBackStatusEvent;
import com.ion.common_service.event.BookUpdateStatusEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

// Sau khi dispatch commands, commands sẽ dc gắn vào trong aggregates.
// Aggregate: xử lý những commands và phát ra events để phản ánh các thay đổi trạng thái của nó
// Thực thể aggregate: Order aggregate (gồm order details, order các bảng shopping)
@Aggregate
@NoArgsConstructor // Must have
@Getter
@Setter
public class BookAggregate {

    @AggregateIdentifier
    private String id; // Khoá duy nhất của thực thể Book Aggregate, nhằm giúp q.lý các trạng thái thay đổi
    private String name;
    private String author;
    private Boolean isReady;

    @CommandHandler
    public BookAggregate(CreateBookCommand command) {
        BookCreatedEvent bookCreatedEvent = new BookCreatedEvent();
        BeanUtils.copyProperties(command, bookCreatedEvent);

        AggregateLifecycle.apply(bookCreatedEvent); // Publish an event for Event Handler to process
    }

    @CommandHandler
    public void handle(UpdateBookCommand command) {
        BookUpdatedEvent bookUpdatedEvent = new BookUpdatedEvent();
        BeanUtils.copyProperties(command, bookUpdatedEvent);
        AggregateLifecycle.apply(bookUpdatedEvent);
    }

    @CommandHandler
    public void handle(DeleteBookCommand command) {
        BookDeletedEvent bookDeletedEvent = new BookDeletedEvent();
        BeanUtils.copyProperties(command, bookDeletedEvent);
        AggregateLifecycle.apply(bookDeletedEvent);
    }

    @CommandHandler
    public void handler(UpdateBookStatusCommand command) {
        BookUpdateStatusEvent event = new BookUpdateStatusEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void handler(RollBackBookStatusCommand command) {
        BookRollBackStatusEvent event = new BookRollBackStatusEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(BookRollBackStatusEvent event) {
        this.id = event.getBookId();
        this.isReady = event.getIsReady();
    }

    @EventSourcingHandler
    public void on(BookUpdateStatusEvent event) {
        this.id = event.getBookId();
        this.isReady = event.getIsReady();
    }

    // Lắng nghe cái Event đã dc publish và thay đổi trạng thái của Aggregate.
    @EventSourcingHandler
    public void on(BookCreatedEvent event) {
        this.id = event.getId();
        this.name = event.getName();
        this.author = event.getAuthor();
        this.isReady = event.getIsReady();
    }
}
