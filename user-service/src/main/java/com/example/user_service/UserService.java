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
import com.example.user_service.client.UserSyncDto;

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
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        // Send to graph service - with graceful failure handling
        if (graphClient != null) {
            try {
                UserSyncDto syncDto = new UserSyncDto(
                    String.valueOf(savedUser.getId()),
                    savedUser.getUsername(),
                    null, // email - not in User entity
                    null, // department - not in User entity
                    savedUser.getRoles() != null && !savedUser.getRoles().isEmpty() 
                        ? savedUser.getRoles().get(0) : null, // role - first role from list
                    null // managerId - can be added later if needed
                );
                graphClient.syncUser(syncDto);
                System.out.println("✅ User " + savedUser.getUsername() + " synced to graph successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to sync user to graph: " + e.getMessage());
                // Don't throw exception - allow user creation to continue even if graph service fails
            }
        }

        return savedUser;
    }

    @CacheEvict(value = "users", allEntries = true)
    public User updateUser(Long id, User user) {
        if (userRepository.existsById(id)) {
            user.setId(id);
            // Encrypt password if it's being updated
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            User updatedUser = userRepository.save(user);
            
            // Sync update to graph service
            if (graphClient != null) {
                try {
                    UserSyncDto syncDto = new UserSyncDto(
                        String.valueOf(updatedUser.getId()),
                        updatedUser.getUsername(),
                        null, // email - not in User entity
                        null, // department - not in User entity
                        updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty() 
                            ? updatedUser.getRoles().get(0) : null, // role - first role from list
                        null // managerId
                    );
                    graphClient.syncUser(syncDto);
                    System.out.println("✅ User " + updatedUser.getUsername() + " update synced to graph successfully");
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to sync user update to graph: " + e.getMessage());
                }
            }
            
            return updatedUser;
        } else {
            return null; // Handle non-existent ID
        }
    }

    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        // Delete from graph service first
        if (graphClient != null) {
            try {
                graphClient.deleteUser(String.valueOf(id));
                System.out.println("✅ User " + id + " deleted from graph successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete user from graph: " + e.getMessage());
            }
        }
        
        userRepository.deleteById(id);
    }

    // ==============================
    // Methods for Auth Service
    // ==============================
    @Cacheable(value = "users", key = "'username:' + #username")
    public User findByUsername(String username) {
        System.out.println("Fetching user by username from database: " + username);
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

}
