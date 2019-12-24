package com.company.directory.service;

import com.company.directory.exception.NotFoundException;
import com.company.directory.model.Department;
import com.company.directory.model.Employee;
import com.company.directory.repository.DepartmentRepository;
import com.company.directory.repository.EmployeeRepository;
import com.company.directory.request.CreationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author XE on 07.10.2019
 * @project directory
 */

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee findById(Long id) { return employeeRepository.findById(id).orElseThrow(NotFoundException::new); }

    public List<Employee> findByDepartment(Department department) {
        return employeeRepository.findAllByDepartment(department);
    }

    @Transactional
    public Employee save(Employee employee, Long id) {
        Employee same = employeeRepository.findByName(employee.getName());
        if (same != null) {
            return same;
        } else {
            employee.setFired(false);
            Department department;
            if (id == null) {
                department = departmentRepository.findByParent(null);
            } else {
                department = departmentRepository.findById(id).orElseThrow(NotFoundException::new);
            }
            employee.setDepartment(department);
            List<Department> toSave = new ArrayList<>();
            while (department != null) {
                department.setNumberOfEmployees(department.getNumberOfEmployees() + 1);
                toSave.add(department);
                department = department.getParent();
            }
            departmentRepository.saveAll(toSave);
            return employeeRepository.save(employee);
        }
    }

    @Transactional
    public ResponseEntity<String> transfer(Employee employee, Department department) {
        if (employee != null && department != null) {
            if (!employee.isFired() && !employee.getDepartment().equals(department)) {
                long departmentId = department.getId();
                Department departmentFrom = employee.getDepartment();
                employee.setDepartment(department);
                employeeRepository.save(employee);
                List<Department> toSave = new ArrayList<>();
                while (departmentFrom != null) {
                    departmentFrom.setNumberOfEmployees(departmentFrom.getNumberOfEmployees() - 1);
                    toSave.add(departmentFrom);
                    departmentFrom = departmentFrom.getParent();
                }
                while (department != null) {
                    department.setNumberOfEmployees(department.getNumberOfEmployees() + 1);
                    toSave.add(department);
                    department = department.getParent();
                }
                departmentRepository.saveAll(toSave);
                return ResponseEntity.ok("Employee id = " + employee.getId() + " transfer"
                        + " to department id = " + departmentId);
            } else {
                return new ResponseEntity<>("Employee is fired ||  old department == new department", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Employee or Department not found",HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<String> update(CreationRequest request, Employee employeeFromDb) {
        if (employeeFromDb != null && request != null && employeeRepository.findByName(request.getName()) == null) {
            String oldName = employeeFromDb.getName();
            employeeFromDb.setName(request.getName());
            employeeRepository.save(employeeFromDb);
            return  ResponseEntity.ok("Employee id = " + employeeFromDb.getId() + " updated." +
                    " Old name = " + oldName + ", new name = " + request.getName());
        } else {
            return new ResponseEntity<>("Employee not found || New name not found  || " +
                    "Employee with new name already exists", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<String> fire(Employee employee) {
        if (employee != null) {
            if (!employee.isFired()) {
                employee.setFired(true);
                employeeRepository.save(employee);
                recount(employee);
                return ResponseEntity.ok("Employee id=" + employee.getId() + " is fired");
            } else {
                return new ResponseEntity<>("Employee already fired!", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Employee not found!", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<String> delete(Employee employee) {
        if (employee != null) {
            if (employee.isFired()) {
                employeeRepository.delete(employee);
            } else {
                recount(employee);
                employeeRepository.delete(employee);
            }
            return ResponseEntity.ok("Employee id = " + employee.getId() + " is deleted");
        } else {
            return new ResponseEntity<>("Employee not found!", HttpStatus.NOT_FOUND);
        }
    }

    private void recount(Employee employee) {
        Department department = employee.getDepartment();
        List<Department> toSave = new ArrayList<>();
        while (department != null) {
            department.setNumberOfEmployees(department.getNumberOfEmployees() - 1);
            toSave.add(department);
            department = department.getParent();
        }
        departmentRepository.saveAll(toSave);
    }
}
