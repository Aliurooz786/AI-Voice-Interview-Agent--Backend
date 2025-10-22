package com.interview.agent.service;

import com.interview.agent.dto.InterviewDto;
import com.interview.agent.dto.InterviewResponseDto;
import com.interview.agent.dto.UserResponseDto;
import com.interview.agent.enums.InterviewType;
import com.interview.agent.model.Interview;
import com.interview.agent.model.User;
import com.interview.agent.repository.InterviewRepository;
import com.interview.agent.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class handling business logic for interviews.
 * Includes creation, question generation via Gemini, and retrieval.
 */
@Service
public class InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    @Autowired
    public InterviewService(InterviewRepository interviewRepository, UserRepository userRepository, GeminiService geminiService) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    /**
     * Creates a new interview, generates AI questions based on duration,
     * and saves the complete record in a single transaction.
     *
     * @param interviewDto DTO containing job position, description, and duration.
     * @param userEmail    Email of the authenticated user creating the interview.
     * @return An {@link InterviewResponseDto} representing the created interview with questions.
     * @throws UsernameNotFoundException if the user is not found.
     * @throws RuntimeException          if AI question generation fails.
     */
    @Transactional // Ensures atomicity: either everything saves, or nothing does.
    public InterviewResponseDto createInterviewAndGenerateQuestions(InterviewDto interviewDto, String userEmail) {
        log.info("Processing combined request: Create interview and generate questions for user: {}", userEmail);

        // 1. Find the associated user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        // 2. Determine Interview Type from duration
        InterviewType type = determineInterviewType(interviewDto.getDuration());

        // 3. Create the initial Interview entity
        Interview newInterview = new Interview();
        newInterview.setJobPosition(interviewDto.getJobPosition());
        newInterview.setJobDescription(interviewDto.getJobDescription());
        newInterview.setDuration(interviewDto.getDuration()); // Save duration
        newInterview.setInterviewType(type); // Save type
        newInterview.setUser(user);


        // 4. Generate questions using GeminiService
        try {
            log.info("Calling GeminiService to generate questions for job: {}, duration: {}",
                    newInterview.getJobPosition(), newInterview.getDuration());
            String generatedJson = geminiService.generateQuestions(
                    newInterview.getJobPosition(),
                    newInterview.getJobDescription(),
                    newInterview.getDuration() // Pass duration to GeminiService
            );
            newInterview.setGeneratedQuestions(generatedJson); // Set generated questions
            log.info("Successfully received questions JSON from Gemini.");

        } catch (RuntimeException e) {
            log.error("Failed critically during question generation for user {}. Rolling back transaction.", userEmail, e);
            // Re-throw the exception to ensure the transaction is rolled back
            throw new RuntimeException("Failed to generate interview questions via AI: " + e.getMessage(), e);
        }

        // 5. Save the complete interview entity (including questions)
        Interview finalInterview = interviewRepository.save(newInterview);
        log.info("Successfully saved complete interview with ID: {}", finalInterview.getId());

        // 6. Map to DTO and return
        return mapToDto(finalInterview);
    }

    /**
     * Retrieves all interviews created by a specific user.
     *
     * @param userEmail The email of the user whose interviews are to be fetched.
     * @return A List of {@link InterviewResponseDto} for the user's interviews.
     */
    @Transactional(readOnly = true) // Use readOnly for read operations for performance
    public List<InterviewResponseDto> getInterviewsByUser(String userEmail) {
        log.info("Fetching interviews for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        List<Interview> interviews = interviewRepository.findByUserId(user.getId());
        log.info("Found {} interviews for user: {}", interviews.size(), userEmail);

        // Convert List<Interview> to List<InterviewResponseDto> using the updated mapToDto
        return interviews.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single interview by its ID, including generated questions.
     *
     * @param interviewId The ID of the interview to fetch.
     * @return An {@link InterviewResponseDto} for the found interview.
     * @throws RuntimeException if the interview is not found.
     */
    @Transactional(readOnly = true)
    public InterviewResponseDto getInterviewById(Long interviewId) {
        log.info("Fetching interview details for ID: {}", interviewId);
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found with ID: " + interviewId));
        log.debug("Found interview: {}", interview); // Log fetched interview data
        return mapToDto(interview);
    }

    /**
     * Private helper to determine InterviewType based on duration string.
     *
     * @param duration The duration string (e.g., "5 min").
     * @return The corresponding {@link InterviewType}.
     */
    private InterviewType determineInterviewType(String duration) {
        if ("5 min".equalsIgnoreCase(duration)) {
            return InterviewType.MOCK;
        }
        // Consider other durations or default to FULL
        return InterviewType.FULL;
    }

    /**
     * Private helper method to map an Interview entity to a safe response DTO.
     * Includes all relevant fields for the frontend.
     *
     * @param interview The Interview entity to map.
     * @return The mapped {@link InterviewResponseDto}.
     */
    private InterviewResponseDto mapToDto(Interview interview) {
        InterviewResponseDto dto = new InterviewResponseDto();
        dto.setId(interview.getId());
        dto.setJobPosition(interview.getJobPosition());
        dto.setJobDescription(interview.getJobDescription());
        dto.setCreatedAt(interview.getCreatedAt());
        dto.setDuration(interview.getDuration());           // Map duration
        dto.setInterviewType(interview.getInterviewType()); // Map type
        dto.setGeneratedQuestions(interview.getGeneratedQuestions()); // Map questions

        // Map associated User to safe DTO
        UserResponseDto userDto = new UserResponseDto();
        if (interview.getUser() != null) { // Check if user is loaded (might be lazy)
            userDto.setId(interview.getUser().getId());
            userDto.setFullName(interview.getUser().getFullName());
            userDto.setEmail(interview.getUser().getEmail());
        }
        dto.setUser(userDto);

        return dto;
    }
}