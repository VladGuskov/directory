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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Transactional
    public List<Department> findAllByParent(Department department) {
        return departmentRepository.findAllByParent(department);
    }

    public Department findById(Long id){
        return departmentRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    public Department findByParent(Department department) {
        return departmentRepository.findByParent(department);
    }

    @Transactional
    public Department save(Department department, Department parent) {
        Department same = departmentRepository.findByName(department.getName());
        if (same != null) {
            return same;
        } else if (!department.equals(parent)) {
            if (parent != null) {
                department.setParent(parent);
                if (!parent.isNode()) {
                    parent.setNode(true);
                    departmentRepository.save(parent);
                }
                return departmentRepository.save(department);
            } else {
                department.setParent(departmentRepository.findByParent(null));
                return departmentRepository.save(department);
            }
        } else return department;
    }

    @Transactional
    public ResponseEntity changeParent(Department department, Department newParent) {
        if (department != null  && newParent != null) {
            if (department.getParent() != null) {
                if (!department.equals(newParent) && !department.getParent().equals(newParent)) {
                    long departmentId = department.getId();
                    long oldParentId = department.getParent().getId();
                    long newParentId = newParent.getId();
                    Department parent = department.getParent();
                    department.setParent(newParent);
                    List<Department> toSave = new ArrayList<>();
                    toSave.add(department);
                    if (parent.isNode() && departmentRepository.findAllByParent(parent).size() == 0) {
                        parent.setNode(false);
                    }
                    int sum = department.getNumberOfEmployees();
                    while (parent != null) {
                        parent.setNumberOfEmployees(parent.getNumberOfEmployees() - sum);
                        toSave.add(parent);
                        parent = parent.getParent();
                    }
                    if (!newParent.isNode()) {
                        newParent.setNode(true);
                    }
                    while (newParent != null) {
                        newParent.setNumberOfEmployees(newParent.getNumberOfEmployees() + sum);
                        toSave.add(newParent);
                        newParent = newParent.getParent();
                    }
                    departmentRepository.saveAll(toSave);
                    return ResponseEntity.ok("Department id = " + departmentId + " old parent id = " +
                            oldParentId + " changed to new parent id = " + newParentId);
                } else {
                    return new ResponseEntity<>("New parent can't equal department || New parent can't equal old parent", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Head department can't be transferred", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Department not found || New parent not found", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity swap(Department firstDepartment, Department secondDepartment) {
        if (firstDepartment != null && secondDepartment != null) {
            if (!firstDepartment.getParent().equals(secondDepartment.getParent())) {
                if (!firstDepartment.isNode() && !secondDepartment.isNode()) {
                    swapRecount(firstDepartment, secondDepartment);
                    return ResponseEntity.ok("Department(Leaf) id = " + firstDepartment.getId() + " successfully swapped with " +
                            "department(Leaf) id = " + secondDepartment.getId());
                } else if (firstDepartment.isNode() && !secondDepartment.isNode()) {
                      return swapLeafAndNode(firstDepartment,secondDepartment);
                } else if(!firstDepartment.isNode() && secondDepartment.isNode()){
                    return swapLeafAndNode(secondDepartment,firstDepartment);
                } else {
                    swapRecount(firstDepartment,secondDepartment);
                    return ResponseEntity.ok("Department(Node) id = " + firstDepartment.getId() + " successfully swapped with " +
                            "department(Node) id = " + secondDepartment.getId());
                }
            } else return new ResponseEntity<>("First Department and Second Department have same parent", HttpStatus.BAD_REQUEST);
        } else return new ResponseEntity<>("First Department or Second Department not found", HttpStatus.NOT_FOUND);
    }

    @Transactional
    public ResponseEntity<String> update(CreationRequest request, Department departmentFromDb) {
        if (departmentFromDb != null && request != null && departmentRepository.findByName(request.getName()) == null) {
            String oldName = departmentFromDb.getName();
            departmentFromDb.setName(request.getName());
            departmentRepository.save(departmentFromDb);
            return  ResponseEntity.ok("Department id = " + departmentFromDb.getId() + " updated." +
                    " Old name = " + oldName + ", new name = " + request.getName());
        } else {
            return new ResponseEntity<>("Department not found || New name not found  || " +
                    "Department with new name already exists", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity deleteWithFiring(Department department) {
        if (department != null) {
            if (!department.isNode()) {
                List<Employee> employees = employeeRepository.findAllByDepartment(department);
                Department parent = department.getParent();
                if (parent == null) {
                    employeeRepository.findAllByDepartment(department).forEach(employeeRepository::delete);
                    departmentRepository.delete(department);
                    return ResponseEntity.ok("Department(Head) id = " + department.getId() + " successfully deleted");
                }
                if (employees.size() == 0) {
                    departmentRepository.delete(department);
                    return ResponseEntity.ok("Department(Leaf) id = " + department.getId() + " successfully deleted");
                }
                employees.forEach(employee -> {
                    if (!employee.isFired()) {
                        employee.setFired(true);
                    }
                    employee.setDepartment(parent);
                    employeeRepository.save(employee);
                });
                int difference = employees.size() - (int)employees.stream().filter(Employee::isFired).count();
                if (((departmentRepository.findAllByParent(parent).size() - 1) == 0) && parent.isNode()) {
                    parent.setNode(false);
                }
                deleteWithFiringRecount(department, employees, parent, difference);
                return ResponseEntity.ok("Department(Leaf) id = " + department.getId() + " successfully deleted");
            } else {
                if (department.getParent() != null) {
                    List<Department> departments = departmentRepository.findAllByParent(department);
                    int childrenEmployees = departments.stream().mapToInt(Department::getNumberOfEmployees).sum();
                    Department parent = department.getParent();
                    List<Employee> employees = employeeRepository.findAllByDepartment(department);
                    if (employees.size() > 0) {
                        employees.forEach(employee -> {
                            if (!employee.isFired()) {
                                employee.setFired(true);
                            }
                            employee.setDepartment(parent);
                            employeeRepository.save(employee);
                        });
                    }
                    departments.forEach(childDepartment -> {
                        childDepartment.setParent(parent);
                        departmentRepository.save(childDepartment);
                    });
                    int difference = department.getNumberOfEmployees() - childrenEmployees;
                    deleteWithFiringRecount(department, employees, parent, difference);
                    return ResponseEntity.ok("Department(Node) id = " + department.getId() + " successfully deleted");
                } else {
                    if (departmentRepository.findAllByParent(department).size() > 0) {
                        return new ResponseEntity<>("Department(Head) with children can't be deleted", HttpStatus.BAD_REQUEST);
                    } else {
                        employeeRepository.findAllByDepartment(department).forEach(employeeRepository::delete);
                        departmentRepository.delete(department);
                        return ResponseEntity.ok("Department(Head) id = " + department.getId() + " successfully deleted");
                    }
                }
            }
        } else return new ResponseEntity<>("Department not found", HttpStatus.NOT_FOUND);
    }

    @Transactional
    public ResponseEntity deleteWithTransfer(Department department, Department departmentToTransfer) {
        if (department != null && departmentToTransfer != null) {
            if (!department.equals(departmentToTransfer)) {
                if (!department.isNode()) {
                    Department parent = department.getParent();
                    if (parent == null) {
                        return new ResponseEntity<>("Department(Head) can't be deleted with transferring", HttpStatus.BAD_REQUEST);
                    }
                    List<Department> departmentsToTransfer = departmentRepository.findAllByParent(parent);
                    departmentsToTransfer.add(parent);
                    departmentsToTransfer.remove(department);
                    if (departmentsToTransfer.size() == 1 && parent.isNode()) {
                        parent.setNode(false);
                        departmentRepository.save(parent);
                    }
                    if (departmentsToTransfer.contains(departmentToTransfer)) {
                        List<Employee> employees = employeeRepository.findAllByDepartment(department);
                        if (employees.size() > 0) {
                            employees.forEach(employee -> {
                                employee.setDepartment(departmentToTransfer);
                                employeeRepository.save(employee);
                            });
                        }
                        int transferCount = department.getNumberOfEmployees();
                        if (transferCount > 0 && !departmentToTransfer.equals(parent)) {
                            departmentToTransfer.setNumberOfEmployees(departmentToTransfer.getNumberOfEmployees() + transferCount);
                            departmentRepository.save(departmentToTransfer);
                        }
                        departmentRepository.delete(department);
                    } else new ResponseEntity<>("Department can't be deleted with transferring to this department", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.ok("Department(Leaf) id = " + department.getId() + " successfully deleted " +
                            " with transferring employees to Department id = " + departmentToTransfer.getId());
                } else {
                    if (department.getParent() != null) {
                        Department parent = department.getParent();
                        List<Department> departmentsToTransfer = departmentRepository.findAllByParent(parent);
                        departmentsToTransfer.add(parent);
                        departmentsToTransfer.remove(department);
                        if (departmentsToTransfer.contains(departmentToTransfer)) {
                            List<Employee> employees = employeeRepository.findAllByDepartment(department);
                            if (employees.size() > 0) {
                                employees.forEach(employee -> {
                                    employee.setDepartment(departmentToTransfer);
                                    employeeRepository.save(employee);
                                });
                            }
                            departmentRepository.findAllByParent(department).forEach(childDepartment -> {
                                childDepartment.setParent(departmentToTransfer);
                                departmentRepository.save(childDepartment);
                            });
                            int transferCount = department.getNumberOfEmployees();
                            if (transferCount > 0 && !departmentToTransfer.equals(parent)) {
                                departmentToTransfer.setNumberOfEmployees(departmentToTransfer.getNumberOfEmployees() + transferCount);
                                departmentRepository.save(departmentToTransfer);
                            }
                            departmentRepository.delete(department);
                        } else new ResponseEntity<>("Department and DepartmentToTransfer can't be same", HttpStatus.BAD_REQUEST);
                        return ResponseEntity.ok("Department(Node) id = " + department.getId() + " successfully deleted with " +
                                "transferring employees to department id = " + departmentToTransfer.getId());
                    } else return new ResponseEntity<>("Department(Head) can't be deleted with transferring", HttpStatus.BAD_REQUEST);
                }
            } else return new ResponseEntity<>("Department and DepartmentToTransfer can't be same", HttpStatus.BAD_REQUEST);
        } else return new ResponseEntity<>("Department or DepartmentToTransfer not found", HttpStatus.NOT_FOUND);
    }

    private ResponseEntity swapLeafAndNode(Department firstDepartment, Department secondDepartment) {
        if (firstDepartment.getParent() != null && secondDepartment.getParent() != null) {
            if (!secondDepartment.getParent().equals(firstDepartment)) {
                swapRecount(firstDepartment,secondDepartment);
                return ResponseEntity.ok("Department(Node) id = " + firstDepartment.getId() + " successfully swapped with " +
                        "department(Leaf) id = " + secondDepartment.getId());
            } else {
                List<Department> nodeDepartments = departmentRepository.findAllByParent(firstDepartment);
                nodeDepartments.remove(secondDepartment);
                nodeDepartments.forEach(department -> {
                    department.setParent(secondDepartment);
                    departmentRepository.save(department);
                });
                secondDepartment.setParent(firstDepartment.getParent());
                secondDepartment.setNumberOfEmployees(firstDepartment.getNumberOfEmployees());
                secondDepartment.setNode(true);
                firstDepartment.setParent(secondDepartment);
                List<Employee> employees = employeeRepository.findAllByDepartment(firstDepartment);
                int difference;
                if (employees.size() > 0) {
                    difference = employees.size() - (int)employees.stream().filter(Employee::isFired).count();
                } else {
                    difference = 0;
                }
                firstDepartment.setNumberOfEmployees(difference);
                firstDepartment.setNode(false);
                departmentRepository.save(secondDepartment);
                departmentRepository.save(firstDepartment);
                return ResponseEntity.ok("Department(Node) id = " + firstDepartment.getId() + " successfully swapped with " +
                        "department(Leaf) id = " + secondDepartment.getId());
            }
        } else return new ResponseEntity<>("Forbidden any swap with head", HttpStatus.FORBIDDEN);
    }

    private void swapRecount(Department firstDepartment, Department secondDepartment) {
        if (firstDepartment.getParent() != null && secondDepartment.getParent() != null) {
            Department firstParent = firstDepartment.getParent();
            Department secondParent = secondDepartment.getParent();
            Department fParent = firstParent;
            Department sParent = secondParent;
            int first = firstDepartment.getNumberOfEmployees();
            int second = secondDepartment.getNumberOfEmployees();
            int difference = Math.abs(first - second);
            if (difference != 0) {
                List<Department> toSave = new ArrayList<>();
                if (first > second) {
                    while (fParent != null) {
                        fParent.setNumberOfEmployees(fParent.getNumberOfEmployees() - difference);
                        toSave.add(fParent);
                        fParent = fParent.getParent();
                    }
                    while (sParent != null) {
                        sParent.setNumberOfEmployees(sParent.getNumberOfEmployees() + difference);
                        toSave.add(sParent);
                        sParent = sParent.getParent();
                    }
                } else {
                    while (fParent != null) {
                        fParent.setNumberOfEmployees(fParent.getNumberOfEmployees() + difference);
                        toSave.add(fParent);
                        fParent = fParent.getParent();
                    }
                    while (sParent != null) {
                        sParent.setNumberOfEmployees(sParent.getNumberOfEmployees() - difference);
                        toSave.add(sParent);
                        sParent = sParent.getParent();
                    }
                }
                departmentRepository.saveAll(toSave);
            }
            firstDepartment.setParent(secondParent);
            secondDepartment.setParent(firstParent);
            departmentRepository.save(firstDepartment);
            departmentRepository.save(secondDepartment);
        }
    }

    private void deleteWithFiringRecount(Department department, List<Employee> employees, Department parent, int difference) {
        if (difference > 0 && employees.size() > 0) {
            List<Department> toSave = new ArrayList<>();
            while (parent != null) {
                parent.setNumberOfEmployees(parent.getNumberOfEmployees() - difference);
                toSave.add(parent);
                parent = parent.getParent();
            }
            departmentRepository.saveAll(toSave);
        }
        departmentRepository.delete(department);
    }
}
