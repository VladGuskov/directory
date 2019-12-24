package com.company.directory.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author XE on 07.10.2019
 * @project directory
 */

@Entity
@Table
@Data
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private boolean isFired;
}
