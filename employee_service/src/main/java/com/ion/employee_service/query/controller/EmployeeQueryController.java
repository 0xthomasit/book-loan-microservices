package com.ion.employee_service.query.controller;

import com.ion.common_service.model.EmployeeResponseCommonModel;
import com.ion.common_service.queries.GetEmployeeDetailQuery;
import com.ion.employee_service.query.queries.GetAllEmployeeQuery;
import com.ion.employee_service.query.service.EmployeeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee service for Query")
@Slf4j
public class EmployeeQueryController {

    @Autowired
    private EmployeeQueryService employeeQueryService;
    @Operation(
            summary = "Get a list of Employees",
            description = "Get endpoint for a list of employees with filter",
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized / Invalid Token")
            }
    )
    @GetMapping
    public List<EmployeeResponseCommonModel> getAllEmployee(@RequestParam(required = false, defaultValue = "false") Boolean isDisciplined) {
        return employeeQueryService.getAllEmployees(new GetAllEmployeeQuery(isDisciplined));
    }

    @Operation(
            summary = "Get an Employee's detail",
            description = "Get endpoint for detail about employee information",
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized / Invalid Token")
            }
    )
    @GetMapping("/{employeeId}")
    public EmployeeResponseCommonModel getEmployeeDetail(@PathVariable String employeeId) throws Exception {
        log.info("Calling to getEmployeeDetail");
        return employeeQueryService.getEmployeeDetail(new GetEmployeeDetailQuery(employeeId));
    }
}