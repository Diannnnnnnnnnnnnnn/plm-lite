package com.example.user_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "graph-service")
public interface GraphClient {

    @PostMapping("/user")
    void createUser(@RequestParam("id") String id, @RequestParam("name") String name);
}
