package com.example.user_service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_service.client.GraphClient;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GraphClient graphClient;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Cacheable(value = "users", key = "'all'") 
    public List<User> getAllUsers() {
        System.out.println("Fetching all users from the database...");
        List<User> users = userRepository.findAll(); 
        System.out.println("Fetched tasks: " + users);
        //return userRepository.findAll(); 
        return users;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @CacheEvict(value = "users", key = "'all'")
    public User addUser(User user) {
     //  return userRepository.save(user); this code made before the neo4j
        User savedUser = userRepository.save(user);
     // Send to graph service
        graphClient.createUser(String.valueOf(savedUser.getId()), savedUser.getUsername());
        return savedUser;
    }

    @CacheEvict(value = "users", key = "'all'")
    public User updateUser(Long id, User user) {
        if (userRepository.existsById(id)) {
            user.setId(id);
            return userRepository.save(user);
        } else {
            return null; // Handle non-existent ID
        }
    }

    @CacheEvict(value = "users", key = "'all'")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ==============================
    // Methods for Auth Service
    // ==============================
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

}
