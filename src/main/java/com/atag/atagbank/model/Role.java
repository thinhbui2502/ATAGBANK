package com.atag.atagbank.model;

import javax.persistence.*;

@Table (name = "roles")
@Entity
public class Role {
    @Id
    @GeneratedValue
    private int id;

    private String role;

    public Role() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}