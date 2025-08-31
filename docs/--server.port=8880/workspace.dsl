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
                tags "Service"

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
                tags "Service"

                documentController = component "DocumentController" {
                    technology "REST Controller"
                }
                documentServiceLogic = component "DocumentServiceLogic" {
                    technology "Business Logic"
                }
                documentRepository = component "DocumentRepository" {
                    technology "JPA, Neo4j, Elasticsearch"
                }

                documentController -> documentServiceLogic "Calls business logic"
                documentServiceLogic -> documentRepository "Reads/Writes metadata"
            }

            // TaskService
            taskService = container "TaskService" {
                description "Handles tasks for document/change approvals"
                technology "Spring Boot, MySQL, Neo4j, Elasticsearch"
                tags "Service"

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
                tags "Service"

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
                tags "Service"

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
                tags "Service"

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
                tags "Service"

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
                tags "Service"

                securityFilter = component "SecurityFilter" {
                    technology "JWT/Auth Filter"
                }
                routeHandler = component "RouteHandler" {
                    technology "Spring Cloud Gateway Routes"
                }

                securityFilter -> routeHandler "Filters requests before routing"
            }

            // React Frontend
            reactFrontend = container "ReactFrontend" {
                description "User Interface for interacting with the system"
                technology "React"
                tags "Frontend"

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
            documentService -> workflowOrchestrator "Starts approval workflow"
            workflowOrchestrator -> taskService "Creates approval tasks"
            workflowOrchestrator -> documentService "Reads document metadata"
            workflowOrchestrator -> changeService "Reads change request data"

            apiGateway -> userService "Proxies user requests"
            apiGateway -> documentService "Proxies document requests"
            apiGateway -> changeService "Proxies change requests"
            apiGateway -> bomService "Proxies part/BOM requests"
            apiGateway -> taskService "Proxies task requests"
            apiGateway -> workflowOrchestrator "Proxies workflow requests"

            taskService -> userService "Notifies approvers"
            changeService -> documentService "References affected documents"
            changeService -> bomService "References affected parts/BOMs"
            bomService -> documentService "References linked documents"
        }

        // External Systems
        mysql = softwareSystem "MySQL" {
            description "Relational DB for structured service data"
            tags "Database"
        }

        neo4j = softwareSystem "Neo4j" {
            description "Graph DB for entity relationships"
            tags "Database"
        }

        redis = softwareSystem "Redis" {
            description "Cache for user session data"
            tags "Cache"
        }

        elasticsearch = softwareSystem "Elasticsearch" {
            description "Search engine for documents, parts, tasks"
            tags "Search Engine"
        }

        minio = softwareSystem "MinIO" {
            description "Object storage for files"
            tags "Storage"
        }

        rabbitmq = softwareSystem "RabbitMQ" {
            description "Message broker for inter-service events"
            tags "Message Broker"
        }

        // Nginx Reverse Proxy (Independent System)
        nginx = softwareSystem "Nginx" {
            description "Reverse proxy serving React frontend and proxying API traffic"
            tags "Proxy"
        }

        // EurekaServer
        eurekaServer = softwareSystem "EurekaServer" {
            description "Service registry for PLM Lite microservices (discovery and registration)."
            tags "Spring Cloud Netflix Eureka"
        }

        // Relationships with External Systems
        userService -> mysql "Reads/Writes user data"
        userService -> redis "Caches user sessions"
        userService -> neo4j "Manages user-task relationships"

        documentService -> mysql "Stores metadata"
        documentService -> elasticsearch "Indexes document data"
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

        // Relationships with Nginx (Independent)
        user -> nginx "Uses web app"
        nginx -> apiGateway "Proxies requests to PLM Lite System"
        nginx -> reactFrontend "Serves frontend"
        
        userService -> eurekaServer "Registers with"
        documentService -> eurekaServer "Registers with"
        bomService -> eurekaServer "Registers with"
        changeService -> eurekaServer "Registers with"
        taskService -> eurekaServer "Registers with"
        fileStorageService -> eurekaServer "Registers with"
    }

    views {
        systemContext plmLite {
            include *
            include user
            autolayout lr
            title "C1 - PLM Lite System Context"
        }

        container plmLite {
            include *
            include user
            autolayout lr
            title "C2 - PLM Lite Container View"
        }

        component userService {
            include *
            autolayout tb
            title "C3 - UserService Components"
        }

        component documentService {
            include *
            autolayout tb
            title "C3 - DocumentService Components"
        }

        component taskService {
            include *
            autolayout tb
            title "C3 - TaskService Components"
        }

        component bomService {
            include *
            autolayout tb
            title "C3 - BOMService Components"
        }

        component changeService {
            include *
            autolayout tb
            title "C3 - ChangeService Components"
        }

        component fileStorageService {
            include *
            autolayout tb
            title "C3 - FileStorageService Components"
        }

        component workflowOrchestrator {
            include *
            autolayout tb
            title "C3 - WorkflowOrchestrator Components"
        }

        component apiGateway {
            include *
            autolayout tb
            title "C3 - APIGateway Components"
        }

        component reactFrontend {
            include *
            autolayout tb
            title "C3 - ReactFrontend Components"
        }

        // Dynamic Views
        dynamic plmLite {
            description "Shows the end-to-end document approval workflow"
            user -> nginx "Uploads a document"
            nginx -> apiGateway "Proxies request"
            apiGateway -> documentService "Stores document metadata"
            documentService -> fileStorageService "Stores file in MinIO"
            documentService -> workflowOrchestrator "Starts approval workflow"
            workflowOrchestrator -> taskService "Creates approval task"
            taskService -> userService "Notifies approvers"
        }

        dynamic plmLite {
            description "Shows the end-to-end change approval workflow"
            user -> nginx "Submits change request"
            nginx -> apiGateway "Proxies request"
            apiGateway -> changeService "Processes change"
            changeService -> workflowOrchestrator "Starts approval workflow"
            workflowOrchestrator -> taskService "Creates approval tasks"
            taskService -> userService "Notifies approvers"
            changeService -> documentService "References affected documents"
            changeService -> bomService "References affected parts/BOMs"
        }

        dynamic plmLite {
            description "Shows the end-to-end Part/BOM (PBS) creation workflow"
            user -> nginx "Creates PBS"
            nginx -> apiGateway "Proxies request"
            apiGateway -> bomService "Creates new part/BOM"
            bomService -> documentService "References linked documents"
        }

        // Styles
        styles {
            element "Service" {
                background #1168bd
                color #ffffff
            }
            element "Frontend" {
                background #08427b
                color #ffffff
            }
            element "Proxy" {
                background #009688
                color #ffffff
            }
            element "Database" {
                shape Cylinder
                background #ffcc00
            }
            element "Cache" {
                shape Cylinder
                background #00cc99
            }
            element "Search Engine" {
                shape Cylinder
                background #ff9900
            }
            element "Storage" {
                shape Cylinder
                background #ff6666
            }
            element "Message Broker" {
                shape Cylinder
                background #cc00cc
            }
        }

        theme default
    }
}
