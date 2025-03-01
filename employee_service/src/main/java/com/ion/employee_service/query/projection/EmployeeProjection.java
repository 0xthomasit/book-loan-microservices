package com.ion.employee_service.query.projection;

import com.ion.common_service.model.EmployeeResponseCommonModel;
import com.ion.common_service.queries.GetEmployeeDetailQuery;
import com.ion.employee_service.command.data.Employee;
import com.ion.employee_service.command.data.EmployeeRepository;
import com.ion.employee_service.query.queries.GetAllEmployeeQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeProjection {
    @Autowired
    private EmployeeRepository employeeRepository;

    @QueryHandler
    public List<EmployeeResponseCommonModel> handle(GetAllEmployeeQuery query) {
        List<Employee> listEmployee = employeeRepository.findAllByIsDisciplined(query.getIsDisciplined());
        return listEmployee.stream().map(employee -> {
            EmployeeResponseCommonModel model = new EmployeeResponseCommonModel();
            BeanUtils.copyProperties(employee, model);
            return model;
        }).toList();
    }

    @QueryHandler
    public EmployeeResponseCommonModel handle(GetEmployeeDetailQuery query) throws Exception {
        Employee employee = employeeRepository.findById(
                query.getId()).orElseThrow(() -> new Exception("Employee not found")
        );
        EmployeeResponseCommonModel model = new EmployeeResponseCommonModel();
        BeanUtils.copyProperties(employee, model);
        return model;
    }
}