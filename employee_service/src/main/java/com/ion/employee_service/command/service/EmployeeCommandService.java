package com.ion.employee_service.command.service;

import com.ion.employee_service.command.command.CreateEmployeeCommand;
import com.ion.employee_service.command.command.DeleteEmployeeCommand;
import com.ion.employee_service.command.command.UpdateEmployeeCommand;
import com.ion.employee_service.command.data.Employee;
import com.ion.employee_service.command.data.EmployeeRepository;
import com.ion.employee_service.command.event.EmployeeCreatedEvent;
import com.ion.employee_service.command.event.EmployeeDeletedEvent;
import com.ion.employee_service.command.event.EmployeeUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class EmployeeCommandService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public String createEmployee(CreateEmployeeCommand command) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(command, employee);
        employeeRepository.save(employee);
        eventPublisher.publishEvent(new EmployeeCreatedEvent(
                employee.getId(), employee.getFirstName(), employee.getLastName(),
                employee.getLastName(), employee.getKin(), employee.getKin(), employee.getIsDisciplined()));
        return command.getId();
    }

    public String updateEmployee(UpdateEmployeeCommand command) throws Exception {
        Employee employee = employeeRepository.findById(command.getId())
                .orElseThrow(() -> new Exception("Employee not found"));
        employee.setFirstName(command.getFirstName());
        employee.setLastName(command.getLastName());
        employee.setKin(command.getKin());
        employee.setIsDisciplined(command.getIsDisciplined());
        employeeRepository.save(employee);
        eventPublisher.publishEvent(new EmployeeUpdatedEvent(
                employee.getId(), employee.getFirstName(), employee.getLastName(),
                employee.getKin(), employee.getIsDisciplined()));
        return command.getId();
    }

    public String deleteEmployee(DeleteEmployeeCommand command) {
        try {
            Employee employee = employeeRepository.findById(command.getId())
                    .orElseThrow(() -> new Exception("Employee not found"));
            employeeRepository.deleteById(command.getId());
            eventPublisher.publishEvent(new EmployeeDeletedEvent(employee.getId()));
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return command.getId();
    }
}