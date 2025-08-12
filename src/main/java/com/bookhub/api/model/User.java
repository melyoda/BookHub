package com.bookhub.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

// @Entity marks this class as a JPA entity, meaning it will be mapped to a database table.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// @Table specifies the name of the database table. If omitted, the class name is used.
@Table(name = "_user")
public class User implements UserDetails{

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

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}