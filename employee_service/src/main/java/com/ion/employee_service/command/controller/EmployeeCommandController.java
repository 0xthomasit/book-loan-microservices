package com.ion.employee_service.command.controller;

import com.ion.employee_service.command.command.CreateEmployeeCommand;
import com.ion.employee_service.command.command.DeleteEmployeeCommand;
import com.ion.employee_service.command.command.UpdateEmployeeCommand;
import com.ion.employee_service.command.model.CreateEmployeeModel;
import com.ion.employee_service.command.model.UpdateEmployeeModel;
import com.ion.employee_service.command.service.EmployeeCommandService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee service for Command")
public class EmployeeCommandController {

    @Autowired
    private EmployeeCommandService employeeCommandService;

    @PostMapping
    public String addEmployee(@Valid @RequestBody CreateEmployeeModel model) throws Exception {
        CreateEmployeeCommand command = new CreateEmployeeCommand(
                UUID.randomUUID().toString(),
                model.getFirstName(),
                model.getLastName(),
                model.getKin(),
                false);
        return employeeCommandService.createEmployee(command);
    }

    @PutMapping("/{employeeId}")
    public String updateEmployee(@Valid @RequestBody UpdateEmployeeModel model, @PathVariable String employeeId) throws Exception {
        UpdateEmployeeCommand command = new UpdateEmployeeCommand(
                employeeId, model.getFirstName(), model.getLastName(), model.getKin(), model.getIsDisciplined());
        return employeeCommandService.updateEmployee(command);
    }

    @DeleteMapping("/{employeeId}")
    @Hidden
    public String deleteEmployee(@PathVariable String employeeId) {
        DeleteEmployeeCommand command = new DeleteEmployeeCommand(employeeId);
        return employeeCommandService.deleteEmployee(command);
    }
}