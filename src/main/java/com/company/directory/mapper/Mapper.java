package com.company.directory.mapper;

import com.company.directory.dto.DepartmentDto;
import com.company.directory.dto.EmployeeDto;
import com.company.directory.model.Department;
import com.company.directory.model.Employee;
import com.company.directory.request.CreationRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author XE on 07.10.2019
 * @project directory
 */

@Component
public class Mapper {

    private final ModelMapper modelMapper;

    @Autowired
    public Mapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public EmployeeDto convertToDto(Employee employee) {
        EmployeeDto employeeDto = modelMapper.map(employee, EmployeeDto.class);
        DepartmentDto departmentDto = modelMapper.map(employee.getDepartment(), DepartmentDto.class);
        employeeDto.setDepartmentDto(departmentDto);
        return employeeDto;
    }

    public DepartmentDto convertToDto(Department department) {
        return modelMapper.map(department, DepartmentDto.class);
    }

    public Employee convertEmployeeToEntity(CreationRequest request) {
        return modelMapper.map(request, Employee.class);
    }

    public Department convertDepartmentToEntity(CreationRequest request) {
        return modelMapper.map(request, Department.class);
    }

    public List<EmployeeDto> convertToDto(List<Employee> byDepartment) {
        return byDepartment.stream().map(this::convertToDto).collect(Collectors.toList());
    }
}
