package com.company.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author XE on 07.10.2019
 * @project directory
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDto {
    private Long id;
    private String name;
    private boolean isNode;
    private int numberOfEmployees;
    private Long parentId;
}
