### 📖 Blog Platform on Spring MVC

A simple web application for blogging with posts, comments, likes, and image uploads.

## 🚀 Full Workflow

### 1. Running the Application

- **Java 21** - Java version
- **Spring Boot 3.5.3** - For rapid application development
- **Spring MVC** - Web framework for handling HTTP requests
- **Spring Data JDBC** - For database operations
- **Spring Boot Thymeleaf** - Template engine for server-side rendering
- **H2 Database** - In-memory database for development and testing
 
**Build & Run:**
```
./gradlew build
./gradlew bootRun
```
App will be available at:
🔗 http://localhost:8080/
### 2. Features

#### ✅ Post CRUD

Create, edit, delete
Pagination & search
#### ✅ Comments

Add, edit, delete
#### ✅ Likes

Increment/decrement counter
#### ✅ Images

Upload & display
#### ✅ Tags

Assign tags to posts
## 🛠 Testing Approach

### Unit Tests
- Controller tests using standalone setup with Mockito mocks
- Service layer tests with mocked repositories
- Fast execution without Spring context initialization

### Integration Tests
- Full Spring context tests for verifying component integration
- Database integration tests with H2 in-memory database
- End-to-end flow verification

### Test Coverage
Use IntelliJ IDEA's built-in code coverage tool:
1. Right-click on test class or package
2. Select "Run with Coverage"
3. View coverage results in the Coverage tool window

To go beyond coverage targets:
- Controllers: 80%+
- Services: 90%+
- Repositories: 85%+

## Configuration Profiles

- **dev** - Development configuration with H2 in-memory database
- **test** - Test configuration with separate H2 database
- **prod** - Production configuration with external database

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/posts` | GET | List all posts with pagination |
| `/posts/{id}` | GET | View a specific post |
| `/posts/add` | GET | Show form to create new post |
| `/posts` | POST | Create a new post |
| `/posts/{id}/edit` | GET | Show form to edit existing post |
| `/posts/{id}` | POST | Update an existing post |
| `/posts/{id}/delete` | POST | Delete a post |
| `/posts/{id}/like` | POST | Like/unlike a post |
| `/posts/{id}/comments` | POST | Add a comment to a post |
| `/posts/{postId}/comments/{commentId}/delete` | POST | Delete a comment |
| `/posts/{postId}/comments/{commentId}` | POST | Edit a comment |

## 🔮 Future Improvements

### Short-term

🔹 Add authentication (Spring Security)  
🔹 Implement REST API  
🔹 Improve UI (Bootstrap, JS)

### Long-term

🔸 Migrate to PostgreSQL  
🔸 Caching (Redis)  
🔸 Docker deployment
