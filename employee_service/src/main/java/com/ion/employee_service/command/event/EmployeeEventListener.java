package com.ion.employee_service.command.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmployeeEventListener {

    @EventListener
    public void on(EmployeeCreatedEvent event) {
        log.info("Employee created — id: {}, firstName: {}, lastName: {}, kin: {}, isDisciplined: {}",
                event.getId(), event.getFirstName(), event.getLastName(),
                event.getKin(), event.getIsDisciplined());
    }

    @EventListener
    public void on(EmployeeUpdatedEvent event) {
        log.info("Employee updated — id: {}, firstName: {}, lastName: {}, kin: {}, isDisciplined: {}",
                event.getId(), event.getFirstName(), event.getLastName(),
                event.getKin(), event.getIsDisciplined());
    }

    @EventListener
    public void on(EmployeeDeletedEvent event) {
        log.info("Employee deleted — id: {}", event.getId());
    }
}
