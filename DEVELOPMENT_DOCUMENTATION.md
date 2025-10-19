# PLM-Lite Development Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Microservices](#microservices)
5. [Frontend Application](#frontend-application)
6. [Infrastructure Services](#infrastructure-services)
7. [Database Design](#database-design)
8. [Development Setup](#development-setup)
9. [API Documentation](#api-documentation)
10. [Integration Points](#integration-points)
11. [Deployment](#deployment)
12. [Development Guidelines](#development-guidelines)

---

## Project Overview

**PLM-Lite** is a lightweight Product Lifecycle Management (PLM) system built using a microservices architecture. The system manages the complete lifecycle of products, documents, parts, changes, and tasks within an organization.

### Key Features
- **Document Management**: Version control, lifecycle management, and document approval workflows
- **Bill of Materials (BOM)**: Hierarchical part structure management with relationships
- **Change Management**: Structured change request and approval process
- **Task Management**: Workflow-driven task assignment and execution with Camunda integration
- **User Management**: User authentication, authorization, and profile management
- **Search & Discovery**: Full-text search capabilities across all entities
- **Graph Relationships**: Neo4j-based relationship mapping for complex data associations

### Project Goals
- Provide a lightweight alternative to enterprise PLM systems
- Enable rapid product development and change management
- Maintain traceability across documents, parts, and changes
- Support distributed team collaboration

---

## Architecture

### Microservices Architecture
PLM-Lite follows a microservices architecture pattern with the following characteristics:

```
┌─────────────┐
│   Frontend  │ (React on Port 3001)
│   (React)   │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ API Gateway │ (Port 8080)
│  (Spring)   │
└──────┬──────┘
       │
       ↓
┌──────────────────────────────────────┐
│        Service Discovery              │
│        (Eureka Server)                │
│           Port 8761                   │
└──────────────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────────────────────────┐
│              Backend Microservices                        │
├───────────┬───────────┬───────────┬──────────┬───────────┤
│ Document  │   Task    │   User    │  Change  │   BOM     │
│  Service  │  Service  │  Service  │ Service  │  Service  │
│  (8081)   │  (8082)   │  (8083)   │  (8084)  │  (8089)   │
└───────────┴───────────┴───────────┴──────────┴───────────┘
       │
       ↓
┌──────────────────────────────────────────────────────────┐
│              Infrastructure Services                      │
├────────────┬──────────────┬────────────┬─────────────────┤
│   Graph    │    Search    │ Workflow   │  File Storage   │
│  Service   │   Service    │Orchestrator│    Service      │
│  (Neo4j)   │(Elasticsearch)│ (Camunda) │    (MinIO)      │
└────────────┴──────────────┴────────────┴─────────────────┘
```

### Design Principles
1. **Service Independence**: Each service is independently deployable and scalable
2. **Database Per Service**: Each service manages its own database
3. **API-First Design**: RESTful APIs for all service interactions
4. **Event-Driven Communication**: RabbitMQ for asynchronous messaging
5. **Centralized Configuration**: Shared configuration management
6. **Service Discovery**: Dynamic service registration and discovery

---

## Technology Stack

### Backend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming language |
| Spring Boot | 3.4.0 | Application framework |
| Spring Cloud | 2024.0.0-RC1 | Microservices framework |
| Maven | 3.x | Build and dependency management |
| H2 Database | Latest | Development database (persistent file-based) |
| MySQL | 8.x | Production relational database |
| Neo4j | Latest | Graph database for relationships |
| Elasticsearch | 8.7 | Full-text search engine |
| MinIO | Latest | Object storage for files |
| RabbitMQ | Latest | Message broker |
| Redis | Latest | Caching layer |
| Camunda/Zeebe | 8.x | Workflow engine |

### Frontend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3.1 | UI framework |
| Material-UI (MUI) | 6.1.9 | Component library |
| Axios | 1.7.8 | HTTP client |
| React Scripts | 5.0.1 | Build tooling |

### DevOps & Tools

| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Multi-container orchestration |
| Git | Version control |
| NGINX | Reverse proxy and load balancing |
| PowerShell/Batch | Startup automation scripts |

---

## Microservices

### 1. Document Service (Port 8081)

**Purpose**: Manages document lifecycle from creation to obsolescence.

**Key Features**:
- Document Master and Version management
- Version control (major/minor versions with revisions)
- Document status workflow (IN_WORK → IN_REVIEW → RELEASED → OBSOLETE)
- File attachment handling via MinIO
- Document-Part linking
- Full-text search integration

**Database**: H2 (file-based persistent) / MySQL

**Key Entities**:
- `DocumentMaster`: Parent entity for all document versions
- `Document`: Individual document versions with metadata
- `DocumentPartLink`: Association between documents and parts

**API Endpoints**:
```
POST   /documents              - Create new document
GET    /documents/{id}         - Get document by ID
PUT    /documents/{id}         - Update document
DELETE /documents/{id}         - Delete document
GET    /documents/creator/{creator} - Get documents by creator
POST   /documents/{id}/upload  - Upload file attachment
GET    /documents/{id}/download - Download file
```

**Technologies**:
- Spring Boot, Spring Data JPA
- H2 Database (file-based)
- MinIO client for file storage
- OpenFeign for inter-service communication
- Elasticsearch integration

---

### 2. Task Service (Port 8082)

**Purpose**: Manages workflow-driven tasks and approvals.

**Key Features**:
- Task creation and assignment
- BPMN workflow integration with Camunda/Zeebe
- Task status tracking (PENDING → IN_PROGRESS → COMPLETED)
- Multi-user task signoffs
- Task analytics and reporting
- Review workflow for documents
- Redis-based caching

**Database**: H2 (file-based persistent) / MySQL

**Key Entities**:
- `Task`: Task metadata and workflow information
- `TaskSignoff`: User decisions and approvals

**Workflow Processes**:
- Document Review Process
- Document Approval Process
- Change Approval Process

**API Endpoints**:
```
POST   /tasks                  - Create task
GET    /tasks/{id}             - Get task by ID
PUT    /tasks/{id}/status      - Update task status
GET    /tasks/assigned/{user}  - Get tasks assigned to user
POST   /tasks/{id}/signoff     - Submit task signoff/approval
GET    /tasks/analytics        - Get task analytics
```

**Technologies**:
- Spring Boot, Spring Data JPA
- Camunda/Zeebe workflow engine
- Redis for caching
- RabbitMQ for messaging

---

### 3. User Service (Port 8083)

**Purpose**: Manages user accounts, authentication, and authorization.

**Key Features**:
- User profile management
- User directory and lookup
- Email-based user identification
- User synchronization tracking
- Group and role management (planned)

**Database**: H2 (file-based persistent) / MySQL

**Key Entities**:
- `User`: User profile with email, fullname, last sync timestamp

**API Endpoints**:
```
POST   /users                  - Create user
GET    /users/{id}             - Get user by ID
PUT    /users/{id}             - Update user
DELETE /users/{id}             - Delete user
GET    /users/email/{email}    - Get user by email
GET    /users                  - Get all users
```

**Technologies**:
- Spring Boot, Spring Data JPA
- H2 Database (file-based)

---

### 4. Change Service (Port 8084)

**Purpose**: Manages engineering change requests and change workflows.

**Key Features**:
- Change request creation and tracking
- Change impact analysis (documents and parts)
- Multi-stage approval workflow
- Change status management (IN_WORK → IN_REVIEW → RELEASED)
- Automatic document status updates on approval
- Relationship tracking in Neo4j

**Database**: H2/MySQL (structured data) + Neo4j (relationships) + Elasticsearch (search)

**Key Entities**:
- `Change`: Change request with metadata
- `ChangeDocument`: Documents affected by change
- `ChangePart`: Parts affected by change

**API Endpoints**:
```
POST   /api/changes                    - Create change
GET    /api/changes/{id}               - Get change by ID
PUT    /api/changes/{id}/submit-review - Submit for review
PUT    /api/changes/{id}/approve       - Approve change
GET    /api/changes/status/{status}    - Get changes by status
GET    /api/changes/creator/{creator}  - Get changes by creator
GET    /api/changes/search             - Search changes
```

**Technologies**:
- Spring Boot, Spring Data JPA
- Spring Data Neo4j
- Spring Data Elasticsearch
- OpenFeign for document service integration

---

### 5. BOM Service (Port 8089)

**Purpose**: Manages Bill of Materials and hierarchical part structures.

**Key Features**:
- Part creation and management
- Hierarchical BOM structure (parent-child relationships)
- Part stage management (CONCEPTUAL_DESIGN → DETAILED_DESIGN → PROTOTYPE → PRODUCTION → OBSOLETE)
- Circular dependency prevention
- Document-Part linking
- Soft delete with 30-day retention
- Quantity tracking for part usage

**Database**: MySQL

**Key Entities**:
- `Part`: Part metadata with title, stage, level, creator
- `PartUsage`: Parent-child relationships with quantity
- `DocumentPartLink`: Part-Document associations
- `ChangePart`: Part-Change associations

**API Endpoints**:
```
POST   /parts                      - Create part
GET    /parts/{id}                 - Get part by ID
PATCH  /parts/{id}/stage/{stage}   - Update part stage
DELETE /parts/{id}                 - Delete part (soft)
POST   /parts/usage                - Add part usage relationship
GET    /parts/{id}/bom-hierarchy   - Get complete BOM tree
GET    /parts/{id}/children        - Get direct children
GET    /parts/{id}/parents         - Get direct parents
POST   /parts/document-link        - Link part to document
GET    /parts/{id}/documents       - Get linked documents
```

**Technologies**:
- Spring Boot, Spring Data JPA
- MySQL database
- OpenFeign for service communication
- Eureka client for service discovery

---

### 6. Auth Service

**Purpose**: Authentication and authorization service.

**Key Features**:
- JWT-based authentication
- User login and token generation
- Token validation and refresh
- Role-based access control

**Database**: MySQL / H2

**Technologies**:
- Spring Boot, Spring Security
- JWT (JSON Web Tokens)

---

### 7. File Storage Service (Port 9900)

**Purpose**: Handles file upload, storage, and retrieval.

**Key Features**:
- File upload to MinIO object storage
- File download and streaming
- File metadata management
- Bucket management

**Storage**: MinIO (S3-compatible object storage)

**API Endpoints**:
```
POST   /files/upload    - Upload file
GET    /files/{id}      - Download file
DELETE /files/{id}      - Delete file
```

**Technologies**:
- Spring Boot
- MinIO Java client

---

## Frontend Application

### React Application (Port 3001)

**Purpose**: User interface for all PLM-Lite functionality.

**Architecture**:
```
src/
├── components/
│   ├── Auth/              - Authentication components
│   ├── BOM/               - BOM management UI
│   │   ├── BOMManager.js
│   │   └── TreeView.js
│   ├── Changes/           - Change management UI
│   │   └── ChangeManager.js
│   ├── Dashboard/         - Main dashboard
│   │   └── Dashboard.js
│   ├── Documents/         - Document management UI
│   │   ├── DocumentManager.js
│   │   └── ReviewerSelectionDialog.js
│   ├── Layout/            - Layout components
│   │   └── MainLayout.js
│   ├── Settings/          - Settings UI
│   ├── Tasks/             - Task management UI
│   │   ├── TaskManager.js
│   │   ├── ReviewTasks.js
│   │   ├── TaskDetails.js
│   │   ├── TaskAnalytics.js
│   │   └── TaskManagementContainer.js
│   └── GlobalSearch.js    - Global search component
├── services/
│   ├── bomService.js      - BOM API client
│   ├── changeService.js   - Change API client
│   ├── documentService.js - Document API client
│   ├── taskService.js     - Task API client
│   └── userService.js     - User API client
├── App.js                 - Main application component
└── index.js               - Application entry point
```

**Key Features**:
1. **Document Management**
   - Create, view, update documents
   - Upload and download attachments
   - Submit for review
   - Track document versions

2. **BOM Management**
   - Visual tree view of part hierarchies
   - Add/remove parts and relationships
   - Edit part metadata and quantities

3. **Change Management**
   - Create and track change requests
   - View impact analysis
   - Submit and approve changes

4. **Task Management**
   - View assigned tasks
   - Complete task signoffs
   - Task analytics dashboard
   - Review workflows

5. **Global Search**
   - Search across documents, parts, changes
   - Filter by various criteria

**UI Framework**: Material-UI (MUI)
- Modern, responsive design
- Component-based architecture
- Consistent styling and theming

---

## Infrastructure Services

### 1. Eureka Server (Port 8761)

**Purpose**: Service registry and discovery.

**Features**:
- Dynamic service registration
- Health monitoring
- Service lookup and load balancing

**Access**: http://localhost:8761

---

### 2. API Gateway (Port 8080)

**Purpose**: Single entry point for all client requests.

**Features**:
- Request routing to microservices
- Load balancing
- CORS handling
- Authentication/authorization filters

**Technologies**:
- Spring Cloud Gateway

---

### 3. Graph Service (Neo4j)

**Purpose**: Store and query complex relationships.

**Relationship Types**:
- `(Change)-[:AFFECTS_DOCUMENT]->(Document)`
- `(Change)-[:AFFECTS_PART]->(Part)`
- `(Change)-[:CREATED_BY]->(User)`
- `(Document)-[:LINKED_TO]->(Part)`

**Port**: 7687 (Bolt protocol)
**Web Console**: Port 7474

---

### 4. Search Service (Elasticsearch)

**Purpose**: Full-text search and indexing.

**Indexed Entities**:
- Documents (title, content, metadata)
- Parts (title, description)
- Changes (title, reason, description)

**Port**: 9200

---

### 5. Workflow Orchestrator (Camunda/Zeebe)

**Purpose**: Business process automation.

**BPMN Processes**:
1. Document Lifecycle Process
2. Document Approval Process
3. Change Approval Process
4. PBS Creation Process

**Port**: 26500 (Zeebe broker)

---

### 6. Message Broker (RabbitMQ)

**Purpose**: Asynchronous messaging between services.

**Use Cases**:
- Event notifications
- Async task processing
- Service decoupling

---

## Database Design

### Unified Schema

The database schema spans multiple services with the following key tables:

#### Document Service Tables
```sql
-- Document master (all versions)
CREATE TABLE DocumentMaster (
  id VARCHAR(255) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  creator VARCHAR(255) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  category VARCHAR(255) NOT NULL
);

-- Individual document versions
CREATE TABLE Document (
  id VARCHAR(255) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  creator VARCHAR(255) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  category VARCHAR(255) NOT NULL,
  masterid VARCHAR(255) NOT NULL,
  version VARCHAR(255) NOT NULL,
  revision VARCHAR(255) NOT NULL,
  stage VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  FOREIGN KEY (masterid) REFERENCES DocumentMaster(id)
);
```

#### BOM Service Tables
```sql
-- Parts
CREATE TABLE Part (
  bigintid VARCHAR(255) PRIMARY KEY,
  titlechar VARCHAR(255) NOT NULL,
  stage VARCHAR(255) NOT NULL,
  level VARCHAR(255) NOT NULL,
  creator VARCHAR(255) NOT NULL,
  create_time TIMESTAMP NOT NULL
);

-- Part relationships (parent-child)
CREATE TABLE PartUsage (
  id VARCHAR(255) PRIMARY KEY,
  child_id VARCHAR(255) NOT NULL,
  parent_id VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  FOREIGN KEY (child_id) REFERENCES Part(bigintid),
  FOREIGN KEY (parent_id) REFERENCES Part(bigintid)
);

-- Document-Part links
CREATE TABLE DocumentPartLink (
  link_id VARCHAR(255) PRIMARY KEY,
  part_id VARCHAR(255) NOT NULL,
  document_id VARCHAR(255) NOT NULL,
  FOREIGN KEY (part_id) REFERENCES Part(bigintid),
  FOREIGN KEY (document_id) REFERENCES Document(id)
);
```

#### Change Service Tables
```sql
-- Change requests
CREATE TABLE Change (
  id VARCHAR(255) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  stage VARCHAR(255) NOT NULL,
  class VARCHAR(255) NOT NULL,
  product VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  creator VARCHAR(255) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  change_reason VARCHAR(255) NOT NULL,
  document_beforechange VARCHAR(255) NOT NULL,
  document_afterchange VARCHAR(255) NOT NULL
);

-- Change-Document relationships
CREATE TABLE ChangeDocument (
  id VARCHAR(255) PRIMARY KEY,
  changetask_id VARCHAR(255) NOT NULL,
  updated_itemid VARCHAR(255) NOT NULL,
  previous_itemid VARCHAR(255) NOT NULL,
  FOREIGN KEY (changetask_id) REFERENCES Change(id)
);

-- Change-Part relationships
CREATE TABLE ChangePart (
  id VARCHAR(255) PRIMARY KEY,
  changetask_id VARCHAR(255) NOT NULL,
  part_id VARCHAR(255) NOT NULL,
  FOREIGN KEY (changetask_id) REFERENCES Change(id)
);
```

#### Task Service Tables
```sql
-- Tasks
CREATE TABLE Task (
  id VARCHAR(255) PRIMARY KEY,
  process VARCHAR(255) NOT NULL,
  task_type VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  assigned_to VARCHAR(255) NOT NULL,
  created_time TIMESTAMP NOT NULL
);

-- Task approvals/signoffs
CREATE TABLE TaskSignoff (
  id VARCHAR(255) PRIMARY KEY,
  task_id VARCHAR(255) NOT NULL,
  user_or_group VARCHAR(255) NOT NULL,
  decision VARCHAR(255) NOT NULL,
  decision_time TIMESTAMP NOT NULL,
  comment TEXT NOT NULL,
  FOREIGN KEY (task_id) REFERENCES Task(id)
);
```

#### User Service Tables
```sql
-- Users
CREATE TABLE User (
  id VARCHAR(255) PRIMARY KEY,
  fullname VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  lastsync TIMESTAMP NOT NULL
);
```

---

## Development Setup

### Prerequisites

1. **Java Development Kit (JDK) 17**
   ```bash
   java -version  # Verify Java 17 is installed
   ```

2. **Node.js and npm**
   ```bash
   node -v  # Version 16+ recommended
   npm -v
   ```

3. **Maven**
   ```bash
   mvn -v  # Version 3.6+ recommended
   ```

4. **Docker Desktop** (Optional, for infrastructure services)
   - Neo4j
   - Elasticsearch
   - RabbitMQ
   - MinIO
   - Redis

### Initial Setup

#### 1. Clone Repository
```bash
git clone <repository-url>
cd plm-lite
```

#### 2. Build All Services
```bash
# Build all backend services
mvn clean install

# Or build individual services
cd document-service
mvn clean install
```

#### 3. Install Frontend Dependencies
```bash
cd frontend
npm install
```

#### 4. Configure Environment

Each service uses either H2 (embedded file-based database) for development or MySQL for production.

**Current Configuration**: Services are configured with H2 file-based databases for easy development.

**Key Configuration Files**:
- `document-service/src/main/resources/application.properties`
- `task-service/src/main/resources/application.properties`
- `user-service/src/main/resources/application.properties`
- `change-service/src/main/resources/application.yml`
- `bom-service/src/main/resources/application.properties`

### Running the Application

#### Option 1: Using Startup Scripts (Recommended)

**Windows Batch Script**:
```bash
# Start all services
start-all-services.bat

# Stop all services
stop-all-services.bat
```

**PowerShell Script**:
```powershell
# Start all services
.\start-all-services.ps1

# Stop all services
.\stop-all-services.ps1
```

The startup scripts will:
1. Start backend services (BOM → Change → Document → Task → User)
2. Wait 60 seconds for services to initialize
3. Start the React frontend
4. Open separate terminal windows for each service

#### Option 2: Manual Start

**Start Backend Services** (in separate terminals):
```bash
# Service startup order
cd bom-service && mvn spring-boot:run
cd change-service && mvn spring-boot:run
cd document-service && mvn spring-boot:run
cd task-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
```

**Start Frontend**:
```bash
cd frontend
npm start
```

#### Option 3: Using Docker (Future)

```bash
docker-compose up
```

### Accessing the Application

Once all services are running (wait 1-2 minutes):

- **Frontend Application**: http://localhost:3001
- **Document Service**: http://localhost:8081
- **Task Service**: http://localhost:8082
- **User Service**: http://localhost:8083
- **Change Service**: http://localhost:8084
- **BOM Service**: http://localhost:8089
- **Eureka Dashboard**: http://localhost:8761

### Troubleshooting

#### Port Already in Use
```bash
# Stop all Java processes
Stop-Process -Name java -Force

# Stop all Node processes
Stop-Process -Name node -Force

# Or use stop scripts
stop-all-services.bat
```

#### Service Not Starting
1. Check Java version: `java -version` (must be 17)
2. Check Maven build: `mvn clean install`
3. Check port availability: `netstat -ano | findstr :<port>`
4. Review service logs in terminal windows

#### Frontend Not Loading
1. Verify Node is running: `Get-Process -Name node`
2. Check port 3001: `netstat -ano | findstr :3001`
3. Review browser console for errors
4. Ensure backend services are running

#### Database Issues
- H2 database files are stored in `<service>/data/` directories
- Console access: http://localhost:8081/h2-console (for document-service)
- JDBC URL: `jdbc:h2:file:./data/documentdb`
- Username: `sa`
- Password: `password`

---

## API Documentation

### REST API Standards

All APIs follow REST conventions:
- **HTTP Methods**: GET, POST, PUT, PATCH, DELETE
- **Response Format**: JSON
- **Status Codes**: 200 (OK), 201 (Created), 400 (Bad Request), 404 (Not Found), 500 (Server Error)
- **Error Format**:
  ```json
  {
    "timestamp": "2024-01-01T12:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/api/documents"
  }
  ```

### Service-Specific APIs

Refer to individual service sections above for detailed API endpoints.

### Inter-Service Communication

Services communicate via:
1. **OpenFeign** (synchronous REST calls)
2. **RabbitMQ** (asynchronous messaging)
3. **Service Discovery** (Eureka for service lookup)

Example OpenFeign client:
```java
@FeignClient(name = "document-service")
public interface DocumentClient {
    @GetMapping("/documents/{id}")
    DocumentDTO getDocument(@PathVariable String id);
}
```

---

## Integration Points

### Document-Task Integration
- Tasks created for document reviews
- Task completion triggers document status updates
- Task assignments based on document reviewers

### Document-Part Integration (BOM)
- Documents linked to parts via `DocumentPartLink`
- BOM service queries document metadata
- Document service provides part association APIs

### Change-Document-Part Integration
- Changes track affected documents and parts
- Change approval triggers document version updates
- Impact analysis spans documents and BOM

### Workflow Integration
- Camunda/Zeebe processes orchestrate approvals
- Process instances created for reviews and changes
- Task service integrates with workflow engine

### Search Integration
- Elasticsearch indexes all entities
- Full-text search across services
- Real-time index updates on entity changes

### Graph Integration
- Neo4j stores relationships between entities
- Complex relationship queries (impact analysis)
- Traceability and dependency mapping

---

## Deployment

### Development Deployment
Currently using individual Spring Boot applications running locally.

### Production Deployment Options

#### 1. Docker Containerization
```dockerfile
# Example Dockerfile for a service
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

#### 2. Kubernetes Orchestration
- Service deployments
- ConfigMaps for configuration
- Secrets for credentials
- Ingress for routing

#### 3. Cloud Deployment
- AWS ECS/EKS
- Azure AKS
- Google GKE

### CI/CD Pipeline (Planned)
1. **Build**: Maven build for backend, npm build for frontend
2. **Test**: Unit tests, integration tests
3. **Package**: Docker images
4. **Deploy**: Kubernetes/Cloud deployment
5. **Monitor**: Logging and metrics collection

---

## Development Guidelines

### Code Standards

#### Backend (Java/Spring Boot)
1. **Package Structure**:
   ```
   com.example.service/
   ├── controller/    - REST controllers
   ├── service/       - Business logic
   ├── repository/    - Data access
   ├── model/         - Domain entities
   ├── dto/           - Data transfer objects
   ├── config/        - Configuration classes
   └── exception/     - Custom exceptions
   ```

2. **Naming Conventions**:
   - Classes: PascalCase
   - Methods: camelCase
   - Constants: UPPER_SNAKE_CASE
   - Packages: lowercase

3. **Best Practices**:
   - Use DTOs for API responses
   - Implement proper exception handling
   - Add input validation
   - Use constructor injection for dependencies
   - Write unit tests for business logic

#### Frontend (React)
1. **Component Structure**:
   - Use functional components with hooks
   - Keep components focused and small
   - Extract reusable logic to custom hooks

2. **Naming Conventions**:
   - Components: PascalCase (e.g., `DocumentManager.js`)
   - Services: camelCase (e.g., `documentService.js`)
   - CSS: kebab-case

3. **Best Practices**:
   - Use Material-UI components for consistency
   - Implement error boundaries
   - Use proper state management
   - Optimize re-renders with React.memo
   - Use async/await for API calls

### Git Workflow

1. **Branch Strategy**:
   - `master`: Production-ready code
   - `develop`: Integration branch
   - `feature/*`: Feature branches
   - `bugfix/*`: Bug fix branches

2. **Commit Messages**:
   ```
   type(scope): subject
   
   body
   
   footer
   ```
   Types: feat, fix, docs, style, refactor, test, chore

3. **Pull Request Process**:
   - Create feature branch from `develop`
   - Implement changes with tests
   - Submit PR with clear description
   - Address review comments
   - Merge after approval

### Testing Strategy

1. **Unit Tests**:
   - JUnit 5 for backend
   - Jest for frontend
   - Target: 70%+ code coverage

2. **Integration Tests**:
   - Test inter-service communication
   - Test database interactions
   - Use test containers for databases

3. **End-to-End Tests**:
   - Test complete user workflows
   - Use Selenium or Cypress

### Logging and Monitoring

1. **Logging Levels**:
   - `ERROR`: System errors requiring attention
   - `WARN`: Unexpected situations
   - `INFO`: Important business events
   - `DEBUG`: Detailed diagnostic information

2. **Log Format**:
   ```
   [timestamp] [level] [service] [traceId] [thread] - message
   ```

3. **Monitoring Metrics**:
   - Service health and uptime
   - API response times
   - Error rates
   - Database connection pool status

### Security Considerations

1. **Authentication**: JWT-based authentication
2. **Authorization**: Role-based access control
3. **Data Protection**: Encrypt sensitive data
4. **API Security**: Rate limiting, input validation
5. **Secrets Management**: Use environment variables, never commit secrets

---

## Appendix

### A. Port Assignments

| Service | Port |
|---------|------|
| Frontend | 3001 |
| API Gateway | 8080 |
| Document Service | 8081 |
| Task Service | 8082 |
| User Service | 8083 |
| Change Service | 8084 |
| Graph Service | 8084 |
| Search Service | 8085 |
| Workflow Orchestrator | 8086 |
| BOM Service | 8089 |
| Eureka Server | 8761 |
| File Storage Service | 9900 |
| Neo4j | 7687 (Bolt), 7474 (HTTP) |
| Elasticsearch | 9200 |
| RabbitMQ | 5672 (AMQP), 15672 (Management) |
| Redis | 6379 |
| MinIO | 9000 (API), 9001 (Console) |
| Zeebe | 26500 |

### B. Environment Variables

Common environment variables across services:

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/dbname
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/

# Neo4j
SPRING_NEO4J_URI=bolt://localhost:7687
SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
SPRING_NEO4J_AUTHENTICATION_PASSWORD=password

# Elasticsearch
SPRING_ELASTICSEARCH_URIS=http://localhost:9200

# MinIO
MINIO_URL=http://localhost:9000
MINIO_ACCESS_KEY=minio
MINIO_SECRET_KEY=minio123

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Zeebe
ZEEBE_CLIENT_BROKER_GATEWAY_ADDRESS=127.0.0.1:26500
```

### C. Common Issues and Solutions

| Issue | Solution |
|-------|----------|
| Service won't start | Check if port is already in use, stop conflicting process |
| Database connection error | Verify database is running and credentials are correct |
| Service not registering with Eureka | Check Eureka server is running on port 8761 |
| Frontend can't connect to backend | Verify CORS configuration and backend service status |
| File upload fails | Check MinIO is running and bucket exists |
| Workflow not starting | Verify Zeebe broker is running on port 26500 |

### D. Useful Commands

```bash
# Build all services
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run specific service
cd <service-name> && mvn spring-boot:run

# Check port usage
netstat -ano | findstr :<port>

# Kill process by port (PowerShell)
$processId = (Get-NetTCPConnection -LocalPort <port>).OwningProcess
Stop-Process -Id $processId -Force

# View H2 database
# Access h2-console at http://localhost:<service-port>/h2-console
# JDBC URL: jdbc:h2:file:./data/<dbname>
```

### E. Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [React Documentation](https://react.dev/)
- [Material-UI Documentation](https://mui.com/)
- [Camunda Documentation](https://docs.camunda.io/)
- [Neo4j Documentation](https://neo4j.com/docs/)
- [Elasticsearch Documentation](https://www.elastic.co/guide/)

---

## Document Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2024-01-01 | PLM Team | Initial documentation |

---

**Last Updated**: 2024-10-18

**Maintainers**: PLM Development Team

**Contact**: For questions or contributions, please contact the development team.

