package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "department_infos")
public class DepartmentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "location", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String location;

    @Column(name = "working_hours", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String workingHours;

    @Column(name = "hotline", length = 30)
    private String hotline;

    @Column(name = "description", length = 500, columnDefinition = "NVARCHAR(500)")
    private String description;
}
