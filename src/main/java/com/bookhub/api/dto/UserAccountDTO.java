package com.bookhub.api.dto;

import com.bookhub.api.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role; //



    // Helper method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
