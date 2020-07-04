package com.mautomic.rediblaster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String occupation;
    private final int salary;

    public Employee(String id, String firstName, String lastName, String email, String occupation, int salary) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.occupation = occupation;
        this.salary = salary;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("occupation")
    public String getOccupation() {
        return occupation;
    }

    @JsonProperty("salary")
    public int getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", occupation='" + occupation + '\'' +
                ", salary=" + salary +
                '}';
    }
}
