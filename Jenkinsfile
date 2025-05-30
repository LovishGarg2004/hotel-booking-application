pipeline {
    agent any

    environment {
        IMAGE_NAME = 'hotel-booking-app'
        DOCKERHUB_USER = 'jxshit'
        DOCKERHUB_REPO = "${DOCKERHUB_USER}/${IMAGE_NAME}"
        CONTAINER_NAME = 'hotel-booking-container'
        
        // Development environment variables
        DEV_DB_URL = 'jdbc:postgresql://0.tcp.in.ngrok.io:19008/postgres'
        DEV_DB_USERNAME = 'postgres'
        DEV_DB_PASSWORD = 'admin'
        DEV_PORT = '8080'
        
        // Production environment variables
        PROD_DB_URL = 'jdbc:postgresql://localhost:5432/hotel_booking_prod'
        PROD_DB_USERNAME = 'postgres'
        PROD_DB_PASSWORD = 'admin'
        
        // Spring Boot configurations
        SPRING_JPA_HIBERNATE_DDL_AUTO = 'update'
        SPRING_JPA_SHOW_SQL = 'true'
        SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT = 'org.hibernate.dialect.PostgreSQLDialect'
        SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL = 'true'
        SERVER_PORT = '8080'
        
        // Debug configurations
        SPRING_PROFILES_ACTIVE = 'dev'
        LOGGING_LEVEL_ROOT = 'INFO'
        LOGGING_LEVEL_ORG_HIBERNATE = 'INFO'
        LOGGING_LEVEL_ORG_SPRINGFRAMEWORK = 'INFO'
        
        // EC2 Configuration
        EC2_HOST = '3.93.4.65'
        EC2_USER = 'ec2-user'
        EC2_REGION = 'us-east-1'
        EC2_KEY = credentials('EC2_SSH_KEY')
        
        // Credentials - Use Jenkins credentials store
        SONAR_TOKEN = credentials('sonarcloud-token')
        DOCKERHUB_TOKEN = credentials('dockerhub-token')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        retry(1)
    }

    stages {
        stage('Pipeline Setup') {
            parallel {
                stage('Debug Info') {
                    steps {
                        sh '''
                            echo "=== Build Information ==="
                            echo "Build Number: ${BUILD_NUMBER}"
                            echo "Branch: ${BRANCH_NAME}"
                            echo "Workspace: ${WORKSPACE}"
                            
                            echo "=== Git Information ==="
                            git branch -a
                            echo "Current commit: $(git rev-parse HEAD)"
                            echo "Current branch: $(git rev-parse --abbrev-ref HEAD)"
                            
                            echo "=== Environment Check ==="
                            java -version
                            ./gradlew --version
                        '''
                    }
                }
                
                stage('Docker Health Check') {
                    steps {
                        script {
                            try {
                                sh '''
                                    docker --version
                                    if ! docker info > /dev/null 2>&1; then
                                        echo "ERROR: Docker daemon is not running"
                                        exit 1
                                    fi
                                    echo "✅ Docker daemon is running"
                                    
                                    # Clean up old images to free space
                                    docker image prune -f --filter "until=24h" || true
                                '''
                            } catch (Exception e) {
                                error("Docker daemon check failed: ${e.message}")
                            }
                        }
                    }
                }
            }
        }

        stage('Checkout & Prepare') {
            steps {
                checkout scm
                sh '''
                    # Ensure gradlew is executable
                    chmod +x gradlew
                    
                    # Clean previous build artifacts
                    ./gradlew clean
                '''
            }
        }

        stage('Build & Test') {
            parallel {
                stage('Build Application') {
                    steps {
                        sh './gradlew build -x test --parallel'
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: '**/build/libs/*.jar', allowEmptyArchive: true
                        }
                    }
                }
                
                stage('Unit Tests') {
                    steps {
                        sh './gradlew test --parallel'
                    }
                    post {
                        always {
                            junit testResults: '**/build/test-results/test/*.xml', allowEmptyResults: true
                            publishTestResults testResultsPattern: '**/build/test-results/test/*.xml'
                        }
                    }
                }
            }
        }

        stage('Code Quality Analysis') {
            when {
                anyOf {
                    branch 'main'
                    branch 'development'
                    branch 'dev'
                    changeRequest()
                }
            }
            steps {
                script {
                    try {
                        withSonarQubeEnv('SonarCloud') {
                            sh """
                                ./gradlew sonarqube \
                                -Dsonar.projectKey=hotel-booking-application \
                                -Dsonar.organization=lovishgarg2004 \
                                -Dsonar.host.url=https://sonarcloud.io \
                                -Dsonar.login=${SONAR_TOKEN} \
                                -Dsonar.branch.name=${BRANCH_NAME} \
                                --info
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠️ SonarCloud analysis failed: ${e.message}"
                        echo "Continuing pipeline execution..."
                    }
                }
            }
        }

        stage('Quality Gate') {
            when {
                anyOf {
                    branch 'main'
                    branch 'development'
                }
            }
            steps {
                script {
                    try {
                        timeout(time: 10, unit: 'MINUTES') {
                            def qg = waitForQualityGate(abortPipeline: false)
                            if (qg.status != 'OK') {
                                echo "⚠️ SonarCloud Quality Gate failed: ${qg.status}"
                                if (env.BRANCH_NAME == 'main') {
                                    error("Quality gate failure on main branch!")
                                }
                            } else {
                                echo "✅ SonarCloud Quality Gate passed: ${qg.status}"
                            }
                        }
                    } catch (Exception e) {
                        echo "⚠️ Quality gate check failed: ${e.message}"
                        if (env.BRANCH_NAME == 'main') {
                            error("Quality gate check failed on main branch!")
                        }
                    }
                }
            }
        }

        stage('Build & Push Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'development'
                    branch 'dev'
                }
            }
            steps {
                script {
                    def imageTag = "${BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
                    
                    sh """
                        echo "Building Docker image with tag: ${imageTag}"
                        docker build --platform linux/amd64,linux/arm64 \
                            -t ${DOCKERHUB_REPO}:${imageTag} \
                            -t ${DOCKERHUB_REPO}:latest \
                            --build-arg BUILD_NUMBER=${BUILD_NUMBER} \
                            --build-arg GIT_COMMIT=${env.GIT_COMMIT} \
                            .
                    """
                    
                    // Push to Docker Hub
                    withCredentials([string(credentialsId: 'dockerhub-token', variable: 'DOCKER_TOKEN')]) {
                        sh """
                            echo \$DOCKER_TOKEN | docker login -u ${DOCKERHUB_USER} --password-stdin
                            docker push ${DOCKERHUB_REPO}:${imageTag}
                            docker push ${DOCKERHUB_REPO}:latest
                            echo "✅ Images pushed successfully"
                        """
                    }
                    
                    // Store image tag for later stages
                    env.IMAGE_TAG = imageTag
                }
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }

        stage('Deploy Applications') {
            parallel {
                stage('Deploy to Development') {
                    when {
                        anyOf {
                            branch 'development'
                            branch 'dev'
                            branch 'feature/sonarcloud-integration'
                        }
                    }
                    steps {
                        deployToEnvironment('dev', env.DEV_PORT, 'dev')
                    }
                }
                
                stage('Deploy to Production') {
                    when {
                        branch 'main'
                    }
                    steps {
                        script {
                            // Add manual approval for production
                            try {
                                timeout(time: 5, unit: 'MINUTES') {
                                    input message: 'Deploy to Production?', 
                                          ok: 'Deploy',
                                          submitterParameter: 'APPROVED_BY'
                                }
                                deployToEnvironment('prod', '8080', 'prod')
                            } catch (Exception e) {
                                echo "Production deployment cancelled or timed out"
                                currentBuild.result = 'ABORTED'
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to EC2') {
            when {
                anyOf {
                    branch 'main'
                    branch 'development'
                }
            }
            steps {
                deployToEC2()
            }
        }

        stage('Post-Deployment Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'development'
                }
            }
            parallel {
                stage('Health Check - Local') {
                    steps {
                        script {
                            def port = (env.BRANCH_NAME == 'main') ? '8080' : env.DEV_PORT
                            healthCheck("localhost:${port}")
                        }
                    }
                }
                
                stage('Health Check - EC2') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'development'
                        }
                    }
                    steps {
                        script {
                            healthCheck("${EC2_HOST}:8080")
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Cleanup Docker images
                sh '''
                    docker system prune -f --volumes || true
                    docker image prune -a -f --filter "until=24h" || true
                '''
            }
            cleanWs()
        }
        
        success {
            script {
                def message = "✅ Pipeline completed successfully!"
                if (env.BRANCH_NAME == 'development' || env.BRANCH_NAME == 'dev') {
                    message += "\n🔗 Development: http://localhost:${env.DEV_PORT}"
                } else if (env.BRANCH_NAME == 'main') {
                    message += "\n🔗 Production: http://localhost:8080"
                    message += "\n🔗 EC2: http://${env.EC2_HOST}:8080"
                }
                echo message
                
                // Send success notification (configure as needed)
                // slackSend(message: message, color: 'good')
            }
        }
        
        failure {
            script {
                def message = "❌ Pipeline failed for branch: ${env.BRANCH_NAME}"
                echo message
                
                // Debug information
                echo "=== Debugging Information ==="
                echo "Build Number: ${BUILD_NUMBER}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Stage: ${env.STAGE_NAME}"
                
                // Send failure notification (configure as needed)
                // slackSend(message: message, color: 'danger')
            }
        }
        
        unstable {
            echo "⚠️ Pipeline completed with warnings"
        }
    }
}

// Helper Methods
def deployToEnvironment(String environment, String port, String profile) {
    def containerName = "${CONTAINER_NAME}-${environment}"
    def dbUrl = (environment == 'prod') ? env.PROD_DB_URL : env.DEV_DB_URL
    def dbUser = (environment == 'prod') ? env.PROD_DB_USERNAME : env.DEV_DB_USERNAME
    def dbPass = (environment == 'prod') ? env.PROD_DB_PASSWORD : env.DEV_DB_PASSWORD
    def hibernateDdl = (environment == 'prod') ? 'validate' : env.SPRING_JPA_HIBERNATE_DDL_AUTO
    def showSql = (environment == 'prod') ? 'false' : env.SPRING_JPA_SHOW_SQL
    
    sh """
        echo "Deploying to ${environment} environment..."
        
        # Stop and remove existing container
        docker stop ${containerName} 2>/dev/null || true
        docker rm ${containerName} 2>/dev/null || true
        
        # Run new container
        docker run -d --name ${containerName} \\
            -p ${port}:8080 \\
            -e SPRING_PROFILES_ACTIVE=${profile} \\
            -e SPRING_DATASOURCE_URL="${dbUrl}" \\
            -e SPRING_DATASOURCE_USERNAME="${dbUser}" \\
            -e SPRING_DATASOURCE_PASSWORD="${dbPass}" \\
            -e SPRING_JPA_HIBERNATE_DDL_AUTO="${hibernateDdl}" \\
            -e SPRING_JPA_SHOW_SQL="${showSql}" \\
            -e SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT="${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT}" \\
            -e SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL="${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL}" \\
            -e SERVER_PORT="${SERVER_PORT}" \\
            -e LOGGING_LEVEL_ROOT="${LOGGING_LEVEL_ROOT}" \\
            --restart unless-stopped \\
            --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" \\
            --health-interval=30s \\
            --health-timeout=10s \\
            --health-retries=3 \\
            ${DOCKERHUB_REPO}:${env.IMAGE_TAG ?: 'latest'}
        
        # Wait for container to start
        echo "Waiting for container to start..."
        sleep 15
        
        # Verify deployment
        if docker ps | grep -q ${containerName}; then
            echo "✅ ${environment} deployment successful!"
            echo "🔗 Application: http://localhost:${port}"
        else
            echo "❌ ${environment} deployment failed!"
            docker logs ${containerName} || true
            exit 1
        fi
    """
}

def deployToEC2() {
    script {
        echo "Deploying to EC2 instance: ${EC2_HOST}"
        
        sshagent([EC2_KEY]) {
            sh """
                # Test SSH connection
                ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'echo "SSH connection successful"'
                
                # Stop existing container
                ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                    docker stop hotel-booking-container 2>/dev/null || true
                    docker rm hotel-booking-container 2>/dev/null || true
                '
                
                # Pull latest image
                ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                    docker pull ${DOCKERHUB_REPO}:${env.IMAGE_TAG ?: 'latest'}
                '
                
                # Start new container
                ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                    docker run -d --name hotel-booking-container \\
                        -p 8080:8080 \\
                        -e SPRING_PROFILES_ACTIVE=prod \\
                        -e SPRING_DATASOURCE_URL="${DEV_DB_URL}" \\
                        -e SPRING_DATASOURCE_USERNAME="${DEV_DB_USERNAME}" \\
                        -e SPRING_DATASOURCE_PASSWORD="${DEV_DB_PASSWORD}" \\
                        -e SPRING_JPA_HIBERNATE_DDL_AUTO="${SPRING_JPA_HIBERNATE_DDL_AUTO}" \\
                        -e SPRING_JPA_SHOW_SQL="${SPRING_JPA_SHOW_SQL}" \\
                        --restart unless-stopped \\
                        --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" \\
                        --health-interval=30s \\
                        ${DOCKERHUB_REPO}:${env.IMAGE_TAG ?: 'latest'}
                '
                
                echo "✅ EC2 deployment completed"
            """
        }
    }
}

def healthCheck(String endpoint) {
    sh """
        echo "Performing health check on ${endpoint}..."
        
        # Wait for application to be ready
        for i in {1..12}; do
            if curl -f http://${endpoint}/actuator/health >/dev/null 2>&1; then
                echo "✅ Health check passed for ${endpoint}"
                curl -s http://${endpoint}/actuator/health | jq '.' || true
                exit 0
            fi
            echo "Attempt \$i/12 failed, waiting 10 seconds..."
            sleep 10
        done
        
        echo "❌ Health check failed for ${endpoint}"
        exit 1
    """
}