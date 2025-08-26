package com.bookhub.api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// @Entity marks this class as a JPA entity, meaning it will be mapped to a database table.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    // @Id marks this field as the primary key for the table.
    @Id
    private String id;

    // @Column specifies that this field maps to a column in the table.
    // 'nullable = false' means the column cannot be empty.
    // 'unique = true' ensures that every email in the table is unique.
    @Indexed(unique = true)
    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private Role role;


    public String getUsername() {
        return firstName + " " + lastName;
    }

    /**
     *
     * @return String, user info.
     * TODO: change later to not show sensitive stuff
     */
    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", username='" + getUsername() + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}