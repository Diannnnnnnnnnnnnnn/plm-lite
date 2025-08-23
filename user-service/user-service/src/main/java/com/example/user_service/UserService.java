package com.example.user_service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.user_service.client.GraphClient;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GraphClient graphClient;

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

       
    // Cache the result of this method in Redis
    //@Cacheable(value = "tasks", key = "'tasks'")  // Cache tasks under the "tasks" cache
    //public List<Task> getAllTasks() {          //I added "(List<Task> tasks" for testing
    //    System.out.println("Fetching tasks from the database...");
    //    List<Task> tasks = taskRepository.findAll();  // After added "(List<Task> tasks", I hide this
    //    System.out.println("Fetched tasks: " + tasks);  // Check if tasks are returned
        //return taskRepository.findAll();  //I am hiding this for testing the redis issue
    //    return tasks;
    //}
}
