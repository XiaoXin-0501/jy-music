package com.jy.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleEntity implements Serializable {
    private Long id;
    private String roleName;
}
