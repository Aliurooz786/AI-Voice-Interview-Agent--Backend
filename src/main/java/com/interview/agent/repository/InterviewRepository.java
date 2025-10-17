package com.interview.agent.repository;

import com.interview.agent.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for the {@link Interview} entity.
 * This interface handles all database operations for Interview objects,
 * inheriting standard CRUD (Create, Read, Update, Delete) methods from JpaRepository.
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    /**
     * Finds all interviews created by a specific user.
     * Spring Data JPA automatically generates the query for this method based on its name.
     *
     * @param userId The ID of the user whose interviews are to be retrieved.
     * @return A {@link List} of interviews belonging to the specified user.
     */
    List<Interview> findByUserId(Long userId);

}