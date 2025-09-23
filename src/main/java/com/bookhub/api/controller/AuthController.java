package com.bookhub.api.controller;

import com.bookhub.api.dto.*;
import com.bookhub.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("register")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> register(@RequestBody RegisterRequestDTO registerRequest) {
        try {
            LoginResponseDTO loginResponse = service.register(registerRequest);

            ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.CREATED)
                    .message("User registration successful")
                    .data(loginResponse)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<LoginResponseDTO> errorResponse = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Registration failed: " + e.getMessage())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO loginResponse = service.login(loginRequest);
            ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.OK)
                    .message("Login successful")
                    .data(loginResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<LoginResponseDTO> errorResponse = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Login failed: " + e.getMessage())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshToken(
            @RequestBody RefreshTokenRequestDTO refreshRequest) {
        RefreshTokenResponseDTO tokenResponse = service.refreshToken(refreshRequest.getRefreshToken());

        ApiResponse<RefreshTokenResponseDTO> response = ApiResponse.<RefreshTokenResponseDTO>builder()
                .status(HttpStatus.OK)
                .message("Login successful")
                .data(tokenResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
