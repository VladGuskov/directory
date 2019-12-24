package com.company.directory.repository;

import com.company.directory.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author XE on 07.10.2019
 * @project directory
 */

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    Department findFirstByParent(Department department);
    List<Department> findAllByParent(Department department);
    Department findByName(String name);
    Department findByParent(Department department);
}
