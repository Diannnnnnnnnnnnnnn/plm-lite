package com.example.user_service;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "User")
public final class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

   // Internal JSON string storage (not directly serialized)
    @JsonIgnore
    private String roles;

    // Constructors
    public User() {}

    public User(String username, List<String> roles, String password) {
        this.username = username;
        setRoles(roles); // ✅ safe call, no warning
        this.password = password;
    }

    // Getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // JSON <-> List conversion
    @JsonGetter("roles")
    public List<String> getRoles() {
        if (this.roles == null || this.roles.isBlank()) { // check null or empty
            return List.of();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.roles, List.class);
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    public void setRoles(List<String> roleList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.roles = mapper.writeValueAsString(roleList);
        } catch (JsonProcessingException e) {
            this.roles = "[]";
        }
    }
}
