package com.interview.agent.repository;

import com.interview.agent.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for the {@link User} entity.
 * This interface handles all database operations for User objects,
 * inheriting standard CRUD (Create, Read, Update, Delete) methods from JpaRepository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     * Spring Data JPA automatically generates the query for this method based on its name.
     *
     * @param email The email address of the user to find.
     * @return An {@link Optional} containing the found user, or an empty Optional if no user was found.
     */
    Optional<User> findByEmail(String email);

}