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
        DEV_PORT = '8081'
        
        // Production environment variables (static for testing)
        PROD_DB_URL = 'jdbc:postgresql://localhost:5432/hotel_booking_prod'
        PROD_DB_USERNAME = 'postgres'
        PROD_DB_PASSWORD = 'admin'
        
        // Additional Spring Boot configurations
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
        EC2_HOST = '3.94.114.92'
        EC2_USER = 'ec2-user'
        EC2_REGION = 'us-east-1'
        EC2_KEY = credentials('EC2_SSH_KEY')
        
        // Static credentials for testing (replace with actual credentials later)
        SONAR_TOKEN = '778fe2c1f277f8e05f032b7c09a76d11a619daf4'
        DOCKERHUB_TOKEN = 'your-dockerhub-token-here'
        DOCKERHUB_PASSWORD = 'your-dockerhub-password-here'
    }

    stages {
        stage('Debug Info') {
            steps {
                sh '''
                    echo "=== Git Information ==="
                    git branch -a
                    echo "Current commit:"
                    git rev-parse HEAD
                    echo "Current branch:"
                    git rev-parse --abbrev-ref HEAD
                '''
            }
        }

        stage('Test Docker') {
            steps {
                script {
                    try {
                        sh 'docker --version'
                        sh '''
                            if ! docker info > /dev/null 2>&1; then
                                echo "Docker daemon is not running. Please start Docker."
                                exit 1
                            fi
                            echo "Docker daemon is running"
                        '''
                    } catch (Exception e) {
                        echo "Error: ${e.message}"
                        echo "Please ensure Docker is running and you have proper permissions."
                        error('Docker daemon check failed')
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                     junit testResults: '**/build/test-results/test/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                script {
                    try {
                        withSonarQubeEnv('SonarCloud') {
                         sh """
                            ./gradlew sonarqube \
                            -Dsonar.projectKey=hotel-booking-application \
                            -Dsonar.organization=lovishgarg2004 \
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.login=$SONAR_TOKEN
                            """
                        }
                        
                    } catch (Exception e) {
                        echo "Warning: SonarCloud analysis skipped - ${e.message}"
                        echo "Using static token for testing. Update with real credentials later."
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    try {
                        timeout(time: 10, unit: 'MINUTES') {
                            def qg = waitForQualityGate(abortPipeline: false)
                    if (qg.status != 'OK') {
                        echo "⚠️ SonarCloud Quality Gate failed: ${qg.status} — continuing build anyway."
                    } else {
                        echo "✅ SonarCloud Quality Gate passed: ${qg.status}"
                    }
                        }
                    } catch (Exception e) {
                        echo "Warning: Quality gate check skipped - ${e.message}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build --platform linux/amd64 -t ${DOCKERHUB_REPO}:${BUILD_NUMBER} .
                    docker tag ${DOCKERHUB_REPO}:${BUILD_NUMBER} ${DOCKERHUB_REPO}:latest
                """
            }
        }

        stage('Push to Docker Hub') {
            when {
                expression { 
                    def remoteBranches = sh(script: 'git branch -r', returnStdout: true).trim()
                    return remoteBranches.contains('origin/development') || remoteBranches.contains('origin/main')
                }
            }
             steps {
                withCredentials([string(credentialsId: 'dockerhub-token', variable: 'DOCKER_TOKEN')]) {
                    sh '''
                        echo $DOCKER_TOKEN | docker login -u $DOCKERHUB_USER --password-stdin
                        docker push ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                        docker push ${DOCKERHUB_REPO}:latest
                    '''
                }
            }
        }

        stage('Deploy to Development') {
            when {
                anyOf {
                    branch 'development'
                    branch 'dev'
                    branch 'feature/sonarcloud-integration'
                }
            }
            steps {
                sh '''
                    echo "Deploying to development environment..."
                    echo "Using database URL: ${DEV_DB_URL}"
                    echo "Using port: ${DEV_PORT}"
                    
                    # Stop and remove existing container if it exists
                    docker stop ${CONTAINER_NAME}-dev 2>/dev/null || true
                    docker rm ${CONTAINER_NAME}-dev 2>/dev/null || true
                    
                    # Run new container
                    docker run -d --name ${CONTAINER_NAME}-dev \
                    -p ${DEV_PORT}:8080 \
                    -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
                    -e SPRING_DATASOURCE_URL="${DEV_DB_URL}" \
                    -e SPRING_DATASOURCE_USERNAME="${DEV_DB_USERNAME}" \
                    -e SPRING_DATASOURCE_PASSWORD="${DEV_DB_PASSWORD}" \
                    -e SPRING_JPA_HIBERNATE_DDL_AUTO="${SPRING_JPA_HIBERNATE_DDL_AUTO}" \
                    -e SPRING_JPA_SHOW_SQL="${SPRING_JPA_SHOW_SQL}" \
                    -e SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT="${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT}" \
                    -e SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL="${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL}" \
                    -e SERVER_PORT="${SERVER_PORT}" \
                    -e LOGGING_LEVEL_ROOT="${LOGGING_LEVEL_ROOT}" \
                    -e LOGGING_LEVEL_ORG_HIBERNATE="${LOGGING_LEVEL_ORG_HIBERNATE}" \
                    -e LOGGING_LEVEL_ORG_SPRINGFRAMEWORK="${LOGGING_LEVEL_ORG_SPRINGFRAMEWORK}" \
                    --restart unless-stopped \
                    ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                    
                    # Wait for container to start
                    sleep 10
                    
                    # Check if container is running
                    if docker ps | grep -q ${CONTAINER_NAME}-dev; then
                        echo "Development deployment successful!"
                        echo "Application available at: http://localhost:${DEV_PORT}"
                    else
                        echo "Development deployment failed!"
                        docker logs ${CONTAINER_NAME}-dev || true
                        exit 1
                    fi
                '''
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'development'
            }
            steps {
                sh '''
                    echo "Deploying to production environment..."
                    
                    # Stop and remove existing container if it exists
                    docker stop ${CONTAINER_NAME}-prod 2>/dev/null || true
                    docker rm ${CONTAINER_NAME}-prod 2>/dev/null || true
                    
                    # Run new container
                    docker run -d --name ${CONTAINER_NAME}-prod \
                    -p 8080:8080 \
                    -e SPRING_PROFILES_ACTIVE=prod \
                    -e SPRING_DATASOURCE_URL="${PROD_DB_URL}" \
                    -e SPRING_DATASOURCE_USERNAME="${PROD_DB_USERNAME}" \
                    -e SPRING_DATASOURCE_PASSWORD="${PROD_DB_PASSWORD}" \
                    -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
                    -e SPRING_JPA_SHOW_SQL=false \
                    -e LOGGING_LEVEL_ROOT=INFO \
                    --restart unless-stopped \
                    ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                    
                    # Wait for container to start
                    sleep 15
                    
                    # Health check
                    timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health 2>/dev/null; do sleep 5; done' || echo "Health check endpoint not available"
                    
                    # Check if container is running
                    if docker ps | grep -q ${CONTAINER_NAME}-prod; then
                        echo "Production deployment successful!"
                    else
                        echo "Production deployment failed!"
                        docker logs ${CONTAINER_NAME}-prod || true
                        exit 1
                    fi
                '''
            }
        }

        stage('Deploy to EC2') {
            when {
               expression { 
                    def remoteBranches = sh(script: 'git branch -r', returnStdout: true).trim()
                    return remoteBranches.contains('origin/development') || remoteBranches.contains('origin/main')
                }
            }
            steps {
script {
    echo "Deploying to EC2 instance..."

    def ec2Host = EC2_HOST
    def ec2User = EC2_USER
    def ec2KeyPath = EC2_KEY

    writeFile file: 'deploy-ec2.sh', text: """
        #!/bin/bash
        echo "Stopping existing container on EC2..."
        ssh -i ${ec2KeyPath} ${ec2User}@${ec2Host} 'docker stop hotel-booking-container || true && docker rm hotel-booking-container || true'

        echo "Pulling latest image on EC2..."
        ssh -i ${ec2KeyPath} ${ec2User}@${ec2Host} 'docker pull ${DOCKERHUB_USER}/hotel-booking-app:${BUILD_NUMBER}'

        echo "Starting new container on EC2..."
        ssh -i ${ec2KeyPath} ${ec2User}@${ec2Host} '''
            docker run -d --name hotel-booking-container \\
            -p 8080:8080 \\
            -e SPRING_PROFILES_ACTIVE=prod \\
            -e SPRING_DATASOURCE_URL=${DEV_DB_URL} \\
            -e SPRING_DATASOURCE_USERNAME=${DEV_DB_USERNAME} \\
            -e SPRING_DATASOURCE_PASSWORD=${DEV_DB_PASSWORD} \\
            -e SPRING_JPA_HIBERNATE_DDL_AUTO=${SPRING_JPA_HIBERNATE_DDL_AUTO} \\
            -e SPRING_JPA_SHOW_SQL=${SPRING_JPA_SHOW_SQL} \\
            -e SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT} \\
            -e SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL} \\
            -e SERVER_PORT=${SERVER_PORT} \\
            ${DOCKERHUB_USER}/hotel-booking-app:${BUILD_NUMBER}
        '''
    """

    sh 'chmod +x deploy-ec2.sh'
    sh './deploy-ec2.sh'
}
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
            script {
                if (env.BRANCH_NAME == 'development' || env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'feature/sonarcloud-integration') {
                    echo "Development app: http://localhost:${env.DEV_PORT}"
                    echo "Container logs: docker logs ${env.CONTAINER_NAME}-dev"
                } else if (env.BRANCH_NAME == 'main') {
                    echo "Production app: http://localhost:8080"
                    echo "EC2 app: http://${env.EC2_HOST}:8080"
                    echo "Container logs: docker logs ${env.CONTAINER_NAME}-prod"
                }
            }
        }
        failure {
            echo 'Pipeline failed!'
            script {
                def containerSuffix = (env.BRANCH_NAME == 'main') ? 'prod' : 'dev'
                echo "Check container logs: docker logs ${env.CONTAINER_NAME}-${containerSuffix}"
                echo "Check container status: docker ps -a | grep ${env.CONTAINER_NAME}-${containerSuffix}"
                
                // Additional debugging for feature branch
                if (env.BRANCH_NAME == 'feature/sonarcloud-integration') {
                    echo "=== Feature Branch Debugging ==="
                    echo "SonarCloud integration testing failed. Check:"
                    echo "1. SonarCloud token is valid"
                    echo "2. Project key and organization are correct"
                    echo "3. Network connectivity to SonarCloud"
                }
            }
        }
    }
}