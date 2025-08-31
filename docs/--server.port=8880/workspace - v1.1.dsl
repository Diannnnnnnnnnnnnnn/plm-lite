workspace {

    model {
        // Person
        user = person "User" {
            description "A user of the PLM Lite System (Engineer, Approver, Admin, etc.)"
        }

        // PLM Lite System
        plmLite = softwareSystem "PLM Lite System" {
            description "Product Lifecycle Management System for managing parts, documents, changes, workflows"

            // UserService
            userService = container "UserService" {
                description "Manages users, authentication, roles, permissions"
                technology "Spring Boot, MySQL, Neo4j, Redis"

                user -> this "Uses the user service"

                userController = component "UserController" {
                    technology "REST Controller"
                }
                userServiceLogic = component "UserServiceLogic" {
                    technology "Business Logic"
                }
                userRepository = component "UserRepository" {
                    technology "Spring Data JPA + Neo4j"
                }

                userController -> userServiceLogic "Calls business logic"
                userServiceLogic -> userRepository "Reads/Writes data"
            }

            // DocumentService
            documentService = container "DocumentService" {
                description "Manages documents, metadata, versions, links to parts and changes"
                technology "Spring Boot, MySQL, Neo4j, Elasticsearch, MinIO"

                user -> this "Uses the document service"

                documentController = component "DocumentController" {
                    technology "REST Controller"
                }
                documentServiceLogic = component "DocumentServiceLogic" {
                    technology "Business Logic"
                }
                documentRepository = component "DocumentRepository" {
                    technology "JPA, Neo4j, Elasticsearch"
                }
                minioAdapter = component "MinIOAdapter" {
                    technology "Handles file upload/download"
                }

                documentController -> documentServiceLogic "Calls business logic"
                documentServiceLogic -> documentRepository "Reads/Writes metadata"
                documentServiceLogic -> minioAdapter "Stores/Retrieves files"
            }

            // TaskService
            taskService = container "TaskService" {
                description "Handles tasks for document/change approvals"
                technology "Spring Boot, MySQL, Neo4j, Elasticsearch"

                user -> this "Uses the task service"

                taskController = component "TaskController" {
                    technology "REST Controller"
                }
                taskServiceLogic = component "TaskServiceLogic" {
                    technology "Business Logic"
                }
                taskRepository = component "TaskRepository" {
                    technology "JPA + Neo4j + Elasticsearch"
                }

                taskController -> taskServiceLogic "Calls business logic"
                taskServiceLogic -> taskRepository "Reads/Writes task data"
            }

            // BOMService
            bomService = container "BOMService" {
                description "Manages parts and BOM structures"
                technology "Spring Boot, MySQL, Neo4j, Elasticsearch"

                user -> this "Uses the BOM service"

                bomController = component "BOMController" {
                    technology "REST Controller"
                }
                bomServiceLogic = component "BOMServiceLogic" {
                    technology "Business Logic"
                }
                bomRepository = component "BOMRepository" {
                    technology "JPA + Neo4j + Elasticsearch"
                }

                bomController -> bomServiceLogic "Calls business logic"
                bomServiceLogic -> bomRepository "Reads/Writes part and BOM data"
            }

            // ChangeService
            changeService = container "ChangeService" {
                description "Handles Engineering Change Requests (ECRs) and Orders (ECOs)"
                technology "Spring Boot, MySQL, Neo4j, Elasticsearch"

                user -> this "Uses the change service"

                changeController = component "ChangeController" {
                    technology "REST Controller"
                }
                changeServiceLogic = component "ChangeServiceLogic" {
                    technology "Business Logic"
                }
                changeRepository = component "ChangeRepository" {
                    technology "JPA + Neo4j + Elasticsearch"
                }

                changeController -> changeServiceLogic "Calls business logic"
                changeServiceLogic -> changeRepository "Reads/Writes change data"
            }

            // FileStorageService
            fileStorageService = container "FileStorageService" {
                description "Stores and retrieves binary files"
                technology "Spring Boot, MinIO, MySQL"

                fileController = component "FileController" {
                    technology "REST Controller"
                }
                fileServiceLogic = component "FileServiceLogic" {
                    technology "Business Logic"
                }
                fileRepository = component "FileRepository" {
                    technology "JPA + MySQL"
                }
                fileStorageAdapter = component "FileStorageAdapter" {
                    technology "MinIO Client"
                }

                fileController -> fileServiceLogic "Calls business logic"
                fileServiceLogic -> fileRepository "Stores/Retrieves file metadata"
                fileServiceLogic -> fileStorageAdapter "Stores/Retrieves file contents"
            }

            // WorkflowOrchestrator
            workflowOrchestrator = container "WorkflowOrchestrator" {
                description "Handles workflow logic for approvals"
                technology "Camunda Zeebe or Activiti, MySQL"

                user -> this "Starts workflows"

                workflowController = component "WorkflowController" {
                    technology "REST Controller"
                }
                workflowServiceLogic = component "WorkflowServiceLogic" {
                    technology "Business Logic"
                }
                workflowRepository = component "WorkflowRepository" {
                    technology "JPA + MySQL"
                }
                workflowEngineAdapter = component "WorkflowEngineAdapter" {
                    technology "Camunda/Zeebe/Activiti Client"
                }

                workflowController -> workflowServiceLogic "Calls business logic"
                workflowServiceLogic -> workflowRepository "Stores workflow state"
                workflowServiceLogic -> workflowEngineAdapter "Executes workflows"
            }

            // API Gateway
            apiGateway = container "APIGateway" {
                description "Routes API requests to appropriate services"
                technology "Spring Cloud Gateway"

                user -> this "Accesses frontend and APIs"

                routeHandler = component "RouteHandler" {
                    technology "Spring Cloud Gateway Routes"
                }
                securityFilter = component "SecurityFilter" {
                    technology "JWT/Auth Filter"
                }

                routeHandler -> securityFilter "Applies authentication/authorization"
            }

            // React Frontend
            reactFrontend = container "ReactFrontend" {
                description "User Interface for interacting with the system"
                technology "React"

                user -> this "Uses the web app"

                uiComponent = component "UIComponent" {
                    technology "React Components"
                }
                apiClient = component "APIClient" {
                    technology "Axios/Fetch"
                }

                uiComponent -> apiClient "Calls backend APIs"
            }

            // Container-to-container relationships
            documentService -> fileStorageService "Stores and fetches files"
            workflowOrchestrator -> taskService "Creates approval tasks"
            workflowOrchestrator -> documentService "Reads document metadata"
            workflowOrchestrator -> changeService "Reads change request data"
            apiGateway -> userService "Proxies user requests"
            apiGateway -> documentService "Proxies document requests"
            apiGateway -> changeService "Proxies change requests"
            apiGateway -> bomService "Proxies part/BOM requests"
            apiGateway -> taskService "Proxies task requests"
            apiGateway -> workflowOrchestrator "Proxies workflow requests"
            reactFrontend -> apiGateway "Sends API requests"
        }

        // External Systems
        mysql = softwareSystem "MySQL" "Relational DB for structured service data" "Database"
        neo4j = softwareSystem "Neo4j" "Graph DB for entity relationships" "Database"
        redis = softwareSystem "Redis" "Cache for user session data" "Cache"
        elasticsearch = softwareSystem "Elasticsearch" "Search engine for documents, parts, tasks" "Search Engine"
        minio = softwareSystem "MinIO" "Object storage for files" "Storage"
        rabbitmq = softwareSystem "RabbitMQ" "Message broker for inter-service events" "Message Broker"

        // Container -> External System Relationships
        userService -> mysql "Reads/Writes user data"
        userService -> redis "Caches user sessions"
        userService -> neo4j "Manages user-task relationships"

        documentService -> mysql "Stores metadata"
        documentService -> elasticsearch "Indexes document data"
        documentService -> minio "Stores files"
        documentService -> neo4j "Links to parts and changes"

        taskService -> mysql "Stores task data"
        taskService -> elasticsearch "Indexes tasks"
        taskService -> neo4j "Links tasks with users and workflow instances"

        bomService -> mysql "Stores parts/BOMs"
        bomService -> elasticsearch "Indexes parts/BOMs"
        bomService -> neo4j "Links parts with documents/changes"

        changeService -> mysql "Stores change request data"
        changeService -> elasticsearch "Indexes changes"
        changeService -> neo4j "Links changes to parts/documents"

        fileStorageService -> mysql "Stores file metadata"
        fileStorageService -> minio "Stores files"

        workflowOrchestrator -> mysql "Stores workflow state (Zeebe tables)"
        workflowOrchestrator -> rabbitmq "Sends events"
    }

    views {
        systemContext plmLite {
            include *
            autolayout lr
            title "C1 - PLM Lite System Context"
        }

        container plmLite {
            include *
            autolayout lr
            title "C2 - PLM Lite Container View"
        }

        component userService {
            include *
            autolayout lr
            title "C3 - UserService Components"
        }

        component documentService {
            include *
            autolayout lr
            title "C3 - DocumentService Components"
        }

        component taskService {
            include *
            autolayout lr
            title "C3 - TaskService Components"
        }

        component bomService {
            include *
            autolayout lr
            title "C3 - BOMService Components"
        }

        component changeService {
            include *
            autolayout lr
            title "C3 - ChangeService Components"
        }

        component fileStorageService {
            include *
            autolayout lr
            title "C3 - FileStorageService Components"
        }

        component workflowOrchestrator {
            include *
            autolayout lr
            title "C3 - WorkflowOrchestrator Components"
        }

        component apiGateway {
            include *
            autolayout lr
            title "C3 - APIGateway Components"
        }

        component reactFrontend {
            include *
            autolayout lr
            title "C3 - ReactFrontend Components"
        }

        theme default
    }
}
