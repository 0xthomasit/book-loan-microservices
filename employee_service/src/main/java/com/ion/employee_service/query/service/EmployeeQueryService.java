package com.ion.employee_service.query.service;

import com.ion.common_service.model.EmployeeResponseCommonModel;
import com.ion.common_service.queries.GetEmployeeDetailQuery;
import com.ion.employee_service.command.data.Employee;
import com.ion.employee_service.command.data.EmployeeRepository;
import com.ion.employee_service.query.queries.GetAllEmployeeQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeQueryService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<EmployeeResponseCommonModel> getAllEmployees(GetAllEmployeeQuery query) {
        List<Employee> listEmployee = employeeRepository.findAllByIsDisciplined(query.getIsDisciplined());
        return listEmployee.stream().map(employee -> {
            EmployeeResponseCommonModel model = new EmployeeResponseCommonModel();
            BeanUtils.copyProperties(employee, model);
            return model;
        }).toList();
    }

    public EmployeeResponseCommonModel getEmployeeDetail(GetEmployeeDetailQuery query) throws Exception {
        Employee employee = employeeRepository.findById(
                query.getId()).orElseThrow(() -> new Exception("Employee not found")
        );
        EmployeeResponseCommonModel model = new EmployeeResponseCommonModel();
        BeanUtils.copyProperties(employee, model);
        return model;
    }
}
