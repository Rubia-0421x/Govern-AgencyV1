package com.govagency.model;

/**
 * Citizen Model - Represents a citizen user in the system
 */
public class Citizen {

    private final String id;
    private String name;
    private String number;
    private String email;
    private String password;

    /**
     * Constructor - Create a new citizen
     */
    public Citizen(String id, String name, String number, String email) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.email = email;
        this.password = "password"; // Default password
    }

    /**
     * Constructor - Create a new citizen with password
     */
    public Citizen(String id, String name, String number, String email, String password) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.email = email;
        this.password = password;
    }

    // =============== GETTERS ===============

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // =============== SETTERS ===============

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // =============== UTILITY METHODS ===============

    /**
     * Verify if the provided password matches citizen's password
     */
    public boolean verifyPassword(String providedPassword) {
        return this.password != null && this.password.equals(providedPassword);
    }

    @Override
    public String toString() {
        return "Citizen{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}