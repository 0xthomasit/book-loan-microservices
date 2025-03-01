package com.ion.employee_service.query.controller;

import com.ion.common_service.model.EmployeeResponseCommonModel;
import com.ion.common_service.queries.GetEmployeeDetailQuery;
import com.ion.employee_service.query.queries.GetAllEmployeeQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee service for Query")
@Slf4j
public class EmployeeQueryController {
    @Autowired
    private QueryGateway queryGateway;

    @Operation(
            summary = "Get a list of Employees",
            description = "Get endpoint for a list of employees with filter",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized / Invalid Token"
                    )
            }
    )
    @GetMapping
    public List<EmployeeResponseCommonModel> getAllEmployee(@RequestParam(required = false, defaultValue = "false") Boolean isDisciplined) {
        return queryGateway.query(
                new GetAllEmployeeQuery(isDisciplined),
                ResponseTypes.multipleInstancesOf(EmployeeResponseCommonModel.class)
        ).join();
    }

    @Operation(
            summary = "Get an Employee's detail",
            description = "Get endpoint for detail about employee information",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized / Invalid Token"
                    )
            }
    )
    @GetMapping("/{employeeId}")
    public EmployeeResponseCommonModel getEmployeeDetail(@PathVariable String employeeId) {
        log.info("Calling to getAllEmployee");
        return queryGateway.query(
                new GetEmployeeDetailQuery(employeeId),
                ResponseTypes.instanceOf(EmployeeResponseCommonModel.class)
        ).join();
    }
}