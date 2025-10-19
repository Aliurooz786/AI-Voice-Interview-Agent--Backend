package com.interview.agent.service;

import com.interview.agent.dto.InterviewDto;
import com.interview.agent.dto.InterviewResponseDto;
import com.interview.agent.dto.UserResponseDto;
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

/**
 * Service class for handling business logic related to interviews.
 * This includes creating new interviews and generating AI-powered questions for them.
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
     * Creates a new interview record for the currently authenticated user.
     *
     * @param interviewDto DTO containing the job position and description.
     * @param userEmail The email of the user creating the interview.
     * @return An {@link InterviewResponseDto} representing the newly created interview.
     */
    @Transactional
    public InterviewResponseDto createInterview(InterviewDto interviewDto, String userEmail) {
        log.info("Creating a new interview for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        Interview newInterview = new Interview();
        newInterview.setJobPosition(interviewDto.getJobPosition());
        newInterview.setJobDescription(interviewDto.getJobDescription());
        newInterview.setUser(user);

        Interview savedInterview = interviewRepository.save(newInterview);
        log.info("Successfully saved new interview with ID: {} for user: {}", savedInterview.getId(), userEmail);

        return mapToDto(savedInterview);
    }

    /**
     * Generates interview questions using the Gemini AI service for a specific interview
     * and saves them to the database.
     *
     * @param interviewId The ID of the interview to generate questions for.
     * @return A raw JSON string containing the generated questions.
     * @throws RuntimeException if the interview with the given ID is not found.
     */
    @Transactional
    public String generateAndSaveQuestions(Long interviewId) {
        log.info("Generating questions for interview ID: {}", interviewId);

        // Step 1: Find the interview from the database.
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found with ID: " + interviewId));

        // Step 2: Call the Gemini Service to generate questions based on job details.
        String generatedJson = geminiService.generateQuestions(
                interview.getJobPosition(),
                interview.getJobDescription()
        );

        // Step 3: Set the generated JSON string on the interview entity.
        interview.setGeneratedQuestions(generatedJson);

        // Step 4: Save the updated interview back to the database.
        interviewRepository.save(interview);
        log.info("Successfully generated and saved questions for interview ID: {}", interviewId);

        return generatedJson;
    }

    /**
     * Retrieves a single interview by its ID.
     *
     * @param interviewId The ID of the interview to fetch.
     * @return An {@link InterviewResponseDto} for the found interview.
     * @throws RuntimeException if the interview is not found.
     */
    public InterviewResponseDto getInterviewById(Long interviewId) {
        log.info("Fetching interview details for ID: {}", interviewId);
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found with ID: " + interviewId));
        return mapToDto(interview);
    }

    /**
     * Private helper method to map an Interview entity to a safe response DTO.
     * This prevents sensitive data (like the user's password) from being exposed in API responses.
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

        // Map the associated User entity to a safe UserResponseDto
        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(interview.getUser().getId());
        userDto.setFullName(interview.getUser().getFullName());
        userDto.setEmail(interview.getUser().getEmail());

        dto.setUser(userDto);
        return dto;
    }
}