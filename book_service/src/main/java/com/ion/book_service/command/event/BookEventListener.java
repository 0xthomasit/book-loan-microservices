package com.ion.book_service.command.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookEventListener {

    @EventListener
    public void on(BookCreatedEvent event) {
        log.info("Book created — id: {}, name: {}, author: {}, isReady: {}",
                event.getId(), event.getName(), event.getAuthor(), event.getIsReady());
    }

    @EventListener
    public void on(BookUpdatedEvent event) {
        log.info("Book updated — id: {}, name: {}, author: {}, isReady: {}",
                event.getId(), event.getName(), event.getAuthor(), event.getIsReady());
    }

    @EventListener
    public void on(BookDeletedEvent event) {
        log.info("Book deleted — id: {}", event.getId());
    }
}
