package com.perros.app.dogsapi.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "dogs")
public class Dog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String breed;

    private Integer age;

    @Size(max = 500)
    private String description;

    // Constructor vacío (requerido por JPA)
    public Dog() {}

    // Constructor con parámetros
    public Dog(String name, String breed, Integer age, String description) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.description = description;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Dog{id=" + id + ", name='" + name + "', breed='" + breed + "', age=" + age + "}";
    }
}
