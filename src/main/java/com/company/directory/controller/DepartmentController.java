package com.company.directory.controller;

import com.company.directory.dto.DepartmentDto;
import com.company.directory.mapper.Mapper;
import com.company.directory.model.Department;
import com.company.directory.request.CreationRequest;
import com.company.directory.service.DepartmentService;
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
@RequestMapping("department")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final Mapper mapper;

    @Autowired
    public DepartmentController(DepartmentService departmentService, Mapper mapper) {
        this.departmentService = departmentService;
        this.mapper = mapper;
    }

    @GetMapping("all")
    public List<DepartmentDto> getAll() {
        List<Department> departments = departmentService.findAll();
        return departments.stream()
                .map(mapper::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("all/parent/{id}")
    public List<DepartmentDto> getAllByParent(@PathVariable("id") Department department) {
        List<Department> departments = departmentService.findAllByParent(department);
        return departments.stream()
                .map(mapper::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public DepartmentDto getById(@PathVariable("id") Long id) {
        return mapper.convertToDto(departmentService.findById(id));
    }

    @GetMapping("parent/{id}")
    public DepartmentDto getByParentId(@PathVariable("id") Department parent) {
        return mapper.convertToDto(departmentService.findByParent(parent));
    }

    @PostMapping
    public DepartmentDto createHead(@RequestBody CreationRequest request) {
        Department department = mapper.convertDepartmentToEntity(request);
        Department departmentCreated = departmentService.save(department,null);
        return mapper.convertToDto(departmentCreated);
    }

    @PostMapping("{id}")
    public DepartmentDto create(
            @RequestBody CreationRequest request,
            @PathVariable("id") Department parent
    ) {
        Department department = mapper.convertDepartmentToEntity(request);
        Department departmentCreated = departmentService.save(department, parent);
        return mapper.convertToDto(departmentCreated);
    }

    @PostMapping("{departmentId}/{parentId}")
    public ResponseEntity changeParent(
            @PathVariable("departmentId") Department department,
            @PathVariable("parentId") Department newParent

    ) {
        return departmentService.changeParent(department,newParent);
    }

    @PostMapping("swap/{firstId}/{secondId}")
    public ResponseEntity swap(
            @PathVariable("firstId") Department firstDepartment,
            @PathVariable("secondId") Department secondDepartment
    ) {
        return departmentService.swap(firstDepartment, secondDepartment);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> update(
            @PathVariable("id") Department departmentFromDb,
            @RequestBody CreationRequest request
    ) {
        return departmentService.update(request, departmentFromDb);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteWithFiring(@PathVariable("id") Department department) {
        return departmentService.deleteWithFiring(department);
    }

    @DeleteMapping("{id}/{transferId}")
    public ResponseEntity deleteWithTransfer(
            @PathVariable("id") Department department,
            @PathVariable("transferId") Department departmentToTransfer
    ) {
        return departmentService.deleteWithTransfer(department, departmentToTransfer);
    }
}
