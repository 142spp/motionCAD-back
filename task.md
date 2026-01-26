

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
- [ ] Secure existing endpoints <!-- id: 29 -->

## Asset Pipeline & S3 Integration
- [x] Asset Pipeline & S3 Integration <!-- id: 32 -->
    - [x] Add `software.amazon.awssdk:s3` dependency
    - [x] Configure S3 credentials in `application.yml`
    - [x] Create `S3Service` for file uploads
    - [x] Implement S3 Presigned URLs (5-minute expiration) for secure asset access
- [x] Create Asset Crawling API <!-- id: 33 -->
    - [x] Implement endpoint to receive asset metadata and files
    - [x] Integrate with `PartService` to save parts
- [x] Provide Python Crawler Example <!-- id: 34 -->
    - [x] Create a sample script to search and download from Sketchfab
    - [x] (Optional) Add Draco compression step using `gltf-pipeline`
- [x] Implement AI Asset Confirmation Flow <!-- id: 35 -->
    - [x] Add S3 transfer logic for external URLs
    - [x] Implement confirm endpoint for users to save AI assets
- [x] Cleanup Legacy AI Logic & Schema <!-- id: 36 -->
    - [x] Remove `prompt` field from `Part` entity
    - [x] Delete legacy `createPartByAI` service and controller methods
