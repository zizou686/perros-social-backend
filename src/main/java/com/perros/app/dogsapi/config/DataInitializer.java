package com.perros.app.dogsapi.config;

import com.perros.app.dogsapi.models.ERole;
import com.perros.app.dogsapi.models.Role;
import com.perros.app.dogsapi.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            roleRepository.save(new Role(ERole.ROLE_MODERATOR));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("✅ Roles creados: ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN");
        }
    }
}
