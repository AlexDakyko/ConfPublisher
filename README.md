

Requirements and user stories
Core user stories
As a content manager, I can compose a Confluence page (title + content + attachments) and preview it
As a content manager, I can schedule a page for future publication or publish immediately
As a content manager, I can view publication status (queued, published, failed) and retry failures safely
As a content manager, I can request AI to improve content, generate summaries, or suggest titles
As a content manager, I can specify target Confluence space and parent page for organization
Non-goals (initial scope)
Multi-tenant authentication (single user/team for now)
Advanced Confluence features (macros, templates)
Page versioning and diff tracking
Real Confluence API integration (use stub provider for development)
Data model
Core entities
Attachment: id, filename, content_type, size, storage_path, description
Page: id, title, content, space_key, parent_page_id, created_at, updated_at
PageAttachment: page_id, attachment_id, position (junction table)
Schedule: id, page_id, scheduled_at, status, attempt_count, last_error
PublishLog: id, page_id, provider, page_id (Confluence), status, message, created_at
Relationships
Page → PageAttachment → Attachment (one-to-many)
Page → Schedule (one-to-many)
Page → PublishLog (one-to-many)
API endpoints
Attachments
POST /api/attachments - Upload attachment file
GET /api/attachments/{id} - Get attachment metadata
Pages
POST /api/pages - Create page
GET /api/pages/{id} - Get page with attachments
GET /api/pages - List pages
Schedules
POST /api/schedules - Schedule a page for publication
GET /api/schedules - List schedules
GET /api/schedules/{id} - Get schedule status
Publishing
POST /api/confluence/publish - Publish page immediately to Confluence
GET /api/confluence/status/{pageId} - Get publication status
AI
POST /api/ai/improve-content - Improve page content
POST /api/ai/generate-summary - Generate page summary
POST /api/ai/suggest-title - Suggest page title
Design patterns
Provider Adapter Pattern
Use an adapter pattern to abstract Confluence API:

BaseProvider interface:
- publishPage(spaceKey, title, content, parentPageId, attachmentPaths) -> (pageId, message)
- getStatus(pageId) -> status

ConfluenceStubProvider (for development):
- Simulates Confluence API responses
- Returns mock Confluence page IDs
- Can simulate failures for testing
This allows you to:

Test without real Confluence API calls
Switch to real Confluence integration easily
Develop without Confluence credentials
Security and compliance
Best practices
Never store secrets in code: Use environment variables
Validate all inputs: Sanitize user input
Enforce file size/type limits: Prevent abuse
Redact secrets from logs: Don’t log sensitive data
Respect rate limits: Implement backoff strategies
Privacy
Don’t store PII unnecessarily
Use secure storage for credentials
Implement proper access controls
Follow data protection regulations
Testing strategy
Test types
Unit tests: Individual functions/components
Integration tests: API endpoints with test database
E2E tests: Complete user flows
AI evaluation: Test AI output quality
Testing with stub provider
Use stub provider for:

Development (no API keys needed)
Testing (predictable behavior)
CI/CD (no external dependencies)