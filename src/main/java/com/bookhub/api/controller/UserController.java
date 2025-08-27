package com.bookhub.api.controller;

import com.bookhub.api.dto.ApiResponse;
import com.bookhub.api.dto.LoginRequestDTO;
import com.bookhub.api.dto.RegisterRequestDTO;
import com.bookhub.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequestDTO registerRequest) {
        try {
            String token = service.register(registerRequest);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.CREATED)
                    .message("User registration successful")
                    .data(token)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            // Generic error handler for unexpected issues during registration
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("{ \"status\": \"INTERNAL_SERVER_ERROR\", \"message\": \"" + e.getMessage() + "\" }")
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            String token = service.login(loginRequest);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.OK)
                    .message("Login successful")
                    .data(token)
                    .build();
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Invalid email or password")
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

}
