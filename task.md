

## Hybrid Development Workflow
- [x] Optimize for local development with Docker DB <!-- id: 26 -->
    - [x] Update `application.yml` with local-friendly defaults
    - [x] Verify Docker DB port exposure
- [x] Document local run instructions <!-- id: 27 -->
- [x] Create unified dev script (`dev.ps1`) <!-- id: 30 -->

## Schema Refinement
- [x] Refine Background & Object handling <!-- id: 31 -->
    - [x] Update `Part` entity with `PartType` enum
    - [x] Update `Project` entity to include `backgroundPart`
    - [x] Update DTOs and Services to handle background selection

## Authentication & Security
- [x] Implement Spring Security & JWT <!-- id: 28 -->
    - [x] Add Security & JWT dependencies
    - [x] Configure `SecurityConfig` & CORS
    - [x] Implement `JwtTokenProvider` & `JwtAuthenticationFilter`
    - [x] Implement `AuthService` (Signup with BCrypt, Login)
    - [x] Implement `AuthController`
- [x] Secure existing endpoints <!-- id: 29 -->
