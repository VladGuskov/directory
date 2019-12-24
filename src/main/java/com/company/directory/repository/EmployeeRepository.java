package com.company.directory.repository;

import com.company.directory.model.Department;
import com.company.directory.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author XE on 07.10.2019
 * @project directory
 */

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    List<Employee> findAllByDepartment(Department department);
    Employee findByName(String name);
//    @Query("SELECT e FROM Employee e WHERE e.fired = true and e.department = ?1")
//    List<Employee> findAllNotFiredEmployeesInDepartment(Department department);
}
