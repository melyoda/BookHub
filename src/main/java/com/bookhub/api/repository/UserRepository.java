package com.bookhub.api.repository;


import com.bookhub.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository marks this interface as a Spring Data repository.
@Repository
// JpaRepository<User, Long> gives us all standard CRUD methods for the User entity,
// which has a primary key of type Long.
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Data JPA can automatically create queries from method names.
    // This method will generate a query to find a User by their email address.
    // It returns an Optional, which is a modern way to handle potential null values.
    Optional<User> findByEmail(String email);
}
