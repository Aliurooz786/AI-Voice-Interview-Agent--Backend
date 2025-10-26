package com.interview.agent.model;

// Import the new Enum
import com.interview.agent.enums.InterviewType;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents an Interview entity in the database.
 * Each instance corresponds to a single interview created by a user.
 * This class is mapped to the "interviews" table.
 */
@Entity
@Table(name = "interviews")
@Data
public class Interview {

    /**
     * The unique identifier for the interview (Primary Key). Auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The job title or position for the interview. Cannot be null.
     */
    @Column(nullable = false)
    private String jobPosition;

    /**
     * The detailed job description. Mapped to TEXT for long descriptions.
     */
    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    /**
     * The selected duration for the interview (e.g., "5 min", "15 min"). Cannot be null.
     */
    @Column(nullable = false)
    private String duration;

    /**
     * The type of interview (e.g., MOCK, FULL), determined based on duration.
     * Stored as a String ("MOCK", "FULL") in the database. Cannot be null.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType interviewType;

    /**
     * The AI-generated key topics for the interview, stored as a JSON string array.
     * (e.g., "[\"Java\", \"Spring Boot\", \"AWS\"]")
     * Mapped to TEXT for potentially large JSON.
     */
    @Column(columnDefinition = "TEXT")
    private String topicsJson;

    /**
     * Timestamp when the interview record was created. Auto-set, not updatable.
     */
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * The user who created this interview (Many-to-One relationship).
     * Creates a 'user_id' foreign key. Loaded lazily. Cannot be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}