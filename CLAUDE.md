# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application that integrates with Spring AI to provide a chat service using OpenAI's GPT models. The application is configured to respond in Korean, with the AI assistant named "테오" (Theo).

### Architecture

**Package Structure**: `hello.spring_ai`
- **Main Application**: `SpringAiApplication.java` - Entry point with property source configuration
- **Chat Package**: Core functionality for AI chat interactions
  - `ChatController.java` - REST endpoint for chat requests
  - `ChatService.java` - Business logic using Spring AI ChatClient
  - `ChatRequest.java` / `ChatResponse.java` - DTOs using Java records

**Key Dependencies**:
- Spring Boot 3.4.4 with Java 17
- Spring AI 1.0.0-M6 with OpenAI integration
- Spring Web for REST API

**Configuration**:
- API keys loaded from `api-keys.properties`
- OpenAI API key referenced via `${openai.api-key}` property
- Default system prompt configures Korean responses and "테오" persona

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test
./gradlew test --tests SpringAiApplicationTests
```

### API Testing
The application exposes a single endpoint:
- `POST /chat` - Send chat messages to the AI service

### Configuration Requirements

**Required Setup**:
1. Create or update `src/main/resources/api-keys.properties` with:
   ```
   openai.api-key=your-api-key-here
   ```
2. Ensure the properties file is not committed to version control

**System Message Configuration**:
The AI is configured with a Korean language system prompt in `ChatService.java`. The default system message sets the AI's name as "테오" and instructs it to respond in Korean.

## Important Notes

- The package name uses underscores (`spring_ai`) instead of hyphens due to Java naming conventions
- Error handling is implemented in `ChatService` with Korean error messages
- The application uses Spring AI's ChatClient builder pattern for configuration
- All chat interactions are stateless through the REST API