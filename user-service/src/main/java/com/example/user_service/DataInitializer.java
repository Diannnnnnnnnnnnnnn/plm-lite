package com.example.user_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Check if users already exist
        if (userRepository.count() > 0) {
            System.out.println("Users already exist in database. Skipping initialization.");
            return;
        }

        System.out.println("Initializing demo users...");

        // Create demo users
        createUserIfNotExists("demo", "demo", Arrays.asList("USER"));
        createUserIfNotExists("guodian", "password", Arrays.asList("REVIEWER"));
        createUserIfNotExists("labubu", "password", Arrays.asList("EDITOR"));
        createUserIfNotExists("vivi", "password", Arrays.asList("APPROVER"));

        System.out.println("Demo users initialized successfully!");
    }

    private void createUserIfNotExists(String username, String password, List<String> roles) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser == null) {
            User user = new User(username, roles, password);
            userService.addUser(user);
            System.out.println("Created user: " + username + " with roles: " + roles);
        } else {
            System.out.println("User already exists: " + username);
        }
    }
}

