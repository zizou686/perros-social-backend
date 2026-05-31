package com.perros.app.dogsapi.controllers;

import com.perros.app.dogsapi.models.Dog;
import com.perros.app.dogsapi.repositories.DogRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dogs")
public class DogController {

    @Autowired
    private DogRepository dogRepository;

    // GET: Obtener todos los perros
    @GetMapping
    public List<Dog> getAllDogs() {
        return dogRepository.findAll();
    }

    // GET: Obtener un perro por ID
    @GetMapping("/{id}")
    public Dog getDogById(@PathVariable Long id) {
        return dogRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perro no encontrado con id: " + id));
    }

    // POST: Crear un nuevo perro
    @PostMapping
    public Dog createDog(@Valid @RequestBody Dog dog) {
        Dog newDog = new Dog(
            dog.getName(),
            dog.getBreed(),
            dog.getAge(),
            dog.getDescription()
        );
        return dogRepository.save(newDog);
    }

    // PUT: Actualizar un perro existente
    @PutMapping("/{id}")
    public Dog updateDog(@PathVariable Long id, @Valid @RequestBody Dog dogDetails) {
        Dog existingDog = dogRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perro no encontrado con id: " + id));
        
        existingDog.setName(dogDetails.getName());
        existingDog.setBreed(dogDetails.getBreed());
        existingDog.setAge(dogDetails.getAge());
        existingDog.setDescription(dogDetails.getDescription());
        
        return dogRepository.save(existingDog);
    }

    // DELETE: Eliminar un perro
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDog(@PathVariable Long id) {
        Dog dog = dogRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perro no encontrado con id: " + id));
        
        dogRepository.delete(dog);
        return ResponseEntity.ok().body("Perro eliminado correctamente con id: " + id);
    }
}
