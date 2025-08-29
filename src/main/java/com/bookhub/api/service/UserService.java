package com.bookhub.api.service;

import com.bookhub.api.dto.LoginRequestDTO;
import com.bookhub.api.dto.LoginResponseDTO;
import com.bookhub.api.dto.RegisterRequestDTO;
import com.bookhub.api.dto.UserAccountDTO;
import com.bookhub.api.exception.UserAlreadyExistsException;
import com.bookhub.api.model.Role;
import com.bookhub.api.model.User;
import com.bookhub.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

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
        String token = jwtService.generateToken(user.getEmail());
        UserAccountDTO userAccountDTO = convertToUserAccountDTO(user);

        return new LoginResponseDTO(token, userAccountDTO);

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

            // Generate token and convert user to DTO
            String token = jwtService.generateToken(loginRequest.getEmail());
            UserAccountDTO userAccountDTO = convertToUserAccountDTO(user);

            return new LoginResponseDTO(token, userAccountDTO);
        }
        // This part is technically unreachable if auth fails, as it throws an exception first
        throw new BadCredentialsException("Invalid username or password");
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
