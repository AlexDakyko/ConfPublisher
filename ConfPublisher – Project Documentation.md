ConfPublisher – Full Project Documentation (English) 

This document contains the complete technical documentation set for the ConfPublisher project. It is formatted as a single unified file for easy download and storage, but you may split it into multiple files inside a /docs directory in your GitHub repository. 

The document includes: 

README.md 

ARCHITECTURE.md 

BACKEND_OVERVIEW.md 

FRONTEND_OVERVIEW.md 

API_REFERENCE.md 

CONFLUENCE_ADAPTER.md 

DEVELOPER_GUIDE.md 

DEPLOYMENT.md 

ENVIRONMENT.md (+ .env.example) 

TROUBLESHOOTING.md 

 

README.md 

ConfPublisher 

ConfPublisher is a system for creating, editing, publishing, and maintaining documentation pages in Confluence. It includes: 

Backend: Spring Boot 3, PostgreSQL, Flyway 

Frontend: React + Vite 

Docker Compose: full environment orchestration 

Features 

Create pages (HTML, Markdown, DOCX, JPEG, PNG, Excel) 

Edit/delete pages 

Update page body 

Manage attachments 

Manual publication scheduler ("Publish" button) 

Publication logs 

Search pages 

Stub Confluence provider for development 

Project Structure 

1     /backend        Spring Boot backend 

2     /frontend       React frontend 

3     /docker-compose.yml 

4      

 

ARCHITECTURE.md 

High-Level Architecture 

1     [React UI] → [Spring Boot REST API] → [Confluence Adapter] → [Confluence] 

2                                    ↓ 

3                                [PostgreSQL] 

4      

Components 

Frontend (React): UI for page management 

Backend (Spring Boot): API, logic, Confluence integration 

Database (PostgreSQL): storage of pages, attachments, schedule, logs 

Flyway: DB migrations 

Backend Package Layout 

1     com.confpub 

2      ├ api.error 

3      ├ config 

4      ├ confluence 

5      ├ domain 

6      ├ repository 

7      ├ service 

8      └ web 

9      

 

BACKEND_OVERVIEW.md 

Technologies 

Spring Boot 3 

Java 17 

PostgreSQL 

JPA/Hibernate 

Flyway 

Springdoc OpenAPI 

Testcontainers 

Key Modules 

domain/ – Entities for Page, Attachment, Schedule, PublishLog 

repository/ – Spring Data repositories 

service/ – business logic (PublishingService) 

web/ – controllers for Pages, Schedule, Attachments 

confluence/ – adapter layer, stub provider, real provider placeholder 

api.error/ – centralized exception handling 

Database Schema 

Based on migration files: 

page 

attachment 

page_attachment 

schedule 

publish_log 

 

FRONTEND_OVERVIEW.md 

Technologies 

React (TypeScript) 

Vite build system 

Axios/Tailored API wrapper 

Nginx (production) 

Structure 

1     src/ 

2      ├ pages/ 

3      │    ├ PageDetails.tsx 

4      │    ├ PageForm.tsx 

5      │    ├ PagesList.tsx 

6      ├ api.ts 

7      ├ App.tsx 

8      └ main.tsx 

9      

 

API_REFERENCE.md 

Page API 

POST /page — create new page 

GET /page/{id} — retrieve page 

PUT /page/{id} — update 

DELETE /page/{id} — delete 

POST /page/{id}/publish — trigger manual publication 

Attachment API 

POST /attachment — upload file 

DELETE /attachment/{id} — delete file 

Search API 

GET /search?q= — search pages 

Confluence Adapter API (Backend → Confluence) 

createPage 

editPage 

deletePage 

uploadAttachment 

deleteAttachment 

updatePageBody 

getPageInfo 

getStatus 

search 

 

CONFLUENCE_ADAPTER.md 

Purpose 

Abstracts Confluence operations. Allows switching between: 

Stub Provider (development) 

Real Confluence Provider (production) 

Interface Example 

1     interface BaseProvider { 

2        createPage(...) 

3        updatePage(...) 

4        deletePage(...) 

5        uploadAttachment(...) 

6        getPageInfo(...) 

7     } 

8      

Stub Provider 

Used in development without external dependencies. 

Real Provider 

Uses: 

CONFLUENCE_URL 

CONFLUENCE_USERNAME 

CONFLUENCE_API_TOKEN 

 

DEVELOPER_GUIDE.md 

Adding a New Entity 

Create entity in domain/ 

Create repository in repository/ 

Add service logic in service/ 

Expose endpoints in web/ 

Add DB migration using Flyway 

Adding New Confluence Operations 

Update BaseProvider interface 

Implement in Stub + Real providers 

Add controller endpoint 

Update frontend api.ts 

 

DEPLOYMENT.md 

Local Run 

Backend: 

1     cd backend 

2     ./mvnw spring-boot:run 

3      

Frontend: 

1     cd frontend 

2     npm install 

3     npm run dev 

4      

Docker Compose 

1     docker-compose up --build 

2      

Starts: 

backend on port 9090 

frontend on port 5173 

PostgreSQL on 5433 

 

ENVIRONMENT.md 

Environment Variables 

1     SERVER_PORT=9090 

2     SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/confpublisher 

3     SPRING_DATASOURCE_USERNAME=postgres 

4     SPRING_DATASOURCE_PASSWORD=postgres 

5     CORS_ALLOWED_ORIGINS=http://localhost:5173 

6     CONFLUENCE_URL= 

7     CONFLUENCE_USERNAME= 

8     CONFLUENCE_API_TOKEN= 

9      

.env.example 

1     SERVER_PORT=9090 

2     SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/confpublisher 

3     SPRING_DATASOURCE_USERNAME=postgres 

4     SPRING_DATASOURCE_PASSWORD=postgres 

5     CORS_ALLOWED_ORIGINS=http://localhost:5173 

6     CONFLUENCE_URL=https://your-domain.atlassian.net/wiki 

7     CONFLUENCE_USERNAME=email@example.com 

8     CONFLUENCE_API_TOKEN=your_api_token_here 

9      

 

TROUBLESHOOTING.md 

Backend does not start 

Check PostgreSQL port 5433 

Verify Flyway migrations 

Check datasource URL in .env 

Frontend cannot reach backend 

Ensure backend CORS allows http://localhost:5173 

Verify api.ts base URL 

Confluence Errors 

401 Unauthorized → check API token 

404 Not Found → incorrect page ID or space key 

429 Too Many Requests → Confluence Cloud rate limiting 

Docker Issues 

Remove containers: docker-compose down -v 

Rebuild: docker-compose up --build 

 

END OF DOCUMENT 