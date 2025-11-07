package com.example.auth_service.userclient;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {

  @PostMapping("/internal/auth/verify")
  UserDto verify(@RequestBody LoginRequest login);
}
