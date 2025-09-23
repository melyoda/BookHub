package com.bookhub.api.service;

import com.bookhub.api.dto.*;
import com.bookhub.api.exception.InvalidTokenException;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.exception.UserAlreadyExistsException;
import com.bookhub.api.model.Role;
import com.bookhub.api.model.User;
import com.bookhub.api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepo;

    private final PasswordEncoder encoder;

//    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public LoginResponseDTO register(RegisterRequestDTO registerRequest) {

        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(encoder.encode(registerRequest.getPassword())) // Hash the password
                .role(Role.USER) //
                .build();

        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        try {
            userRepo.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Internal server error: "+ e.getMessage());
        }
//        return jwtService.generateToken(user.getEmail());
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        UserAccountDTO userAccountDTO = convertToUserAccountDTO(user);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userAccountDTO)
                .build();

    }

    // This method returns the token or throws an exception if authentication fails
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // The authenticate method will throw an exception if credentials are bad
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            // Get user from database
            User user = userRepo.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid credentials");
            }

            // Generate token and convert user to DTO
            String accessToken = jwtService.generateAccessToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            UserAccountDTO userAccountDTO = convertToUserAccountDTO(user);

            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userAccountDTO)
                    .build();
        }
        // This part is technically unreachable if auth fails, as it throws an exception first
        throw new BadCredentialsException("Invalid username or password");
    }

    public RefreshTokenResponseDTO refreshToken(String refreshToken) {
        // Validate the refresh token
        if (!jwtService.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // Extract username and generate new access token
        String userEmail = jwtService.extractUsername(refreshToken);
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(userEmail);
        UserAccountDTO userDTO = convertToUserAccountDTO(user);

        return RefreshTokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .user(userDTO)
                .build();
    }

    private UserAccountDTO convertToUserAccountDTO(User user) {
        UserAccountDTO userAccountDTO = new UserAccountDTO();
        userAccountDTO.setId(user.getId());
        userAccountDTO.setEmail(user.getEmail());
        userAccountDTO.setFirstName(user.getFirstName());
        userAccountDTO.setLastName(user.getLastName());
        userAccountDTO.setRole(user.getRole());

        return userAccountDTO;
    }

}
