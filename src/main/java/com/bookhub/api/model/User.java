package com.bookhub.api.model;

import jakarta.persistence.*;
import lombok.*;

// @Entity marks this class as a JPA entity, meaning it will be mapped to a database table.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// @Table specifies the name of the database table. If omitted, the class name is used.
@Table(name = "_user")
public class User {

    // @Id marks this field as the primary key for the table.
    @Id
    // @GeneratedValue specifies how the primary key is generated.
    // GenerationType.IDENTITY means the database will automatically increment the value.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column specifies that this field maps to a column in the table.
    // 'nullable = false' means the column cannot be empty.
    // 'unique = true' ensures that every email in the table is unique.
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String username = firstName + " " + lastName ;

    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     *
     * @return String, user info.
     * TODO: change later to not show sensitive stuff
     */
    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}