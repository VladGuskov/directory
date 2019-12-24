package com.company.directory.controller;

import com.company.directory.dto.EmployeeDto;
import com.company.directory.mapper.Mapper;
import com.company.directory.model.Department;
import com.company.directory.model.Employee;
import com.company.directory.request.CreationRequest;
import com.company.directory.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author XE on 07.10.2019
 * @project directory
 */

@RestController
@RequestMapping("employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final Mapper mapper;

    @Autowired
    public EmployeeController(EmployeeService employeeService, Mapper mapper) {
        this.employeeService = employeeService;
        this.mapper = mapper;
    }

    @GetMapping("all")
    public List<EmployeeDto> getAll() {
        List<Employee> employees = employeeService.findAll();
        return employees.stream()
                .map(mapper::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public EmployeeDto getById(@PathVariable("id") Long id) {
        return mapper.convertToDto(employeeService.findById(id));
    }

    @GetMapping("department/{id}")
    public List<EmployeeDto> getByDepartment(@PathVariable("id") Department department) {
        return mapper.convertToDto(employeeService.findByDepartment(department));
    }

    @PostMapping
    public EmployeeDto createToHead(
            @RequestBody CreationRequest request
    ){
        Employee employee = mapper.convertEmployeeToEntity(request);
        Employee employeeCreated = employeeService.save(employee,null);
        return mapper.convertToDto(employeeCreated);
    }

    @PostMapping("{id}")
    public EmployeeDto create(
            @RequestBody CreationRequest request,
            @PathVariable Long id
    ){
        Employee employee = mapper.convertEmployeeToEntity(request);
        Employee employeeCreated = employeeService.save(employee,id);
        return mapper.convertToDto(employeeCreated);
    }

    @PostMapping("{id}/{departmentId}")
    public ResponseEntity transfer(
            @PathVariable("id") Employee employee,
            @PathVariable("departmentId") Department department
            ){
        return employeeService.transfer(employee,department);
    }

    @PostMapping("fire/{id}")
    public ResponseEntity fire(@PathVariable("id") Employee employee) {
        return employeeService.fire(employee);
    }

    @PutMapping("{id}")
    public ResponseEntity update(@PathVariable("id") Employee employeeFromDb, @RequestBody CreationRequest request) {
        return employeeService.update(request, employeeFromDb);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") Employee employee) {
        return employeeService.delete(employee);
    }
}
