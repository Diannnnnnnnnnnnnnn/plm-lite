package com.example.auth_service.userclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.UserDto;

@FeignClient(name = "user-service", url = "${user-service.base-url:http://localhost:8083}")
public interface UserClient {

  @PostMapping("/internal/auth/verify")
  UserDto verify(@RequestBody LoginRequest login);
}
