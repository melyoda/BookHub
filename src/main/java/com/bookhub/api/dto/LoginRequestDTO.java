package com.bookhub.api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String email;
    private String password;
}
