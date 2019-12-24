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
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Department parent;

    private String name;
    private boolean isNode;
    private int numberOfEmployees;
}
