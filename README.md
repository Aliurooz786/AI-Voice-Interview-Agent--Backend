# Voxy: AI Voice Interview Agent (Backend)

[![Java 17](https://img.shields.io/badge/Java-17-blue.svg)](https://www.java.com)
[![Spring Boot 3.2.5](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Security-JWT%20%26%20OAuth2-purple.svg)](https://spring.io/projects/spring-security)
[![AI](https://img.shields.io/badge/AI-Gemini%20API-red.svg)](https://deepmind.google/technologies/gemini/)

This is the secure, stateless backend server for the **Voxy AI Voice Interview Agent**. It is built with **Spring Boot 3**, **Spring Security (JWT & OAuth2)**, and **PostgreSQL**.

Its primary role is to:
1.  Manage user authentication (Manual Register/Login & Google OAuth2).
2.  Provide secure, role-based RESTful APIs for the frontend.
3.  Orchestrate AI interactions by using the **Google Gemini API** to dynamically generate interview topics based on job descriptions.

**Frontend Repository:** [AI-Voice-Interview-Agent--Frontend](https://github.com/Aliurooz786/AI-Voice-Interview-Agent--Frontend)

---

##  Features

* **Secure Authentication:** Full stateless JWT-based authentication (`/register`, `/login`).
* **Google OAuth2 Integration:** Seamless Google Sign-In flow that validates the user and returns a JWT token for stateless session management.
* **Dynamic Topic Generation:** A single `POST /api/interviews` endpoint analyzes a Job Description and Duration using the **Gemini API** to create a custom, dynamic **Topic-Based Agenda** (e.g., `["System Design", "AWS", "Teamwork"]`).
* **Secure API:** All non-auth endpoints are protected by `JwtAuthFilter` and Spring Security.
* **Profile Management:** A `GET /api/auth/me` endpoint for the frontend to fetch the logged-in user's details.

---

##  Tech Stack

* **Core:** Java 17, Spring Boot 3.2.5
* **Security:** Spring Security, JWT (jjwt-api), Google OAuth2 Client
* **Database:** Spring Data JPA, PostgreSQL
* **AI:** Google Gemini API (via RestTemplate & Prompt Engineering)
* **Tools:** Maven, Lombok

---

##  Getting Started

### 1. Prerequisites

* **Java 17** (or newer)
* **Maven 3.8+**
* **PostgreSQL** server running locally.

### 2. Database Setup

1.  Open `psql` or a tool like `pgAdmin`.
2.  Create a new database named **`ai_voice_agent_db`**.

    ```sql
    CREATE DATABASE ai_voice_agent_db;
    ```
    (The `spring.jpa.hibernate.ddl-auto=update` property will automatically create the tables on first run.)

### 3. Configuration



    ```properties
    # src/main/resources/secrets.properties
    # YEH FILE .GITIGNORE ME HONI CHAHIYE

    # JWT Secret
    jwt.secret.key=null
    
    # Database Password
    spring.datasource.password=null
    
    # External API Keys
    gemini.api.key=null
    
    # Google OAuth2 Secrets
    spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
    spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
    ```

4.  Ab, apni **`application.properties`** file ko verify karein ki usme `spring.config.import` waali line ho (jaisa humne pehle kiya tha):

    ```properties
    # src/main/resources/application.properties
    
    # ... (server.port, spring.datasource.url, etc.)
    
    # Import the secret properties file
    spring.config.import=optional:classpath:secrets.properties
    ```

### 4. Run the Application

Apni main application class (jisme `@SpringBootApplication` hai) ko IntelliJ se Run karein, ya terminal se:

```bash
mvn spring-boot:run


#End
