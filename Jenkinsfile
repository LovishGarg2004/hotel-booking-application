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
        
        // Additional Spring Boot configurations
        SPRING_JPA_HIBERNATE_DDL_AUTO = 'update'
        SPRING_JPA_SHOW_SQL = 'true'
        SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT = 'org.hibernate.dialect.PostgreSQLDialect'
        SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL = 'true'
        SERVER_PORT = '8080'
        
        // Debug configurations
        SPRING_PROFILES_ACTIVE = 'dev'
        LOGGING_LEVEL_ROOT = 'DEBUG'
        LOGGING_LEVEL_ORG_HIBERNATE = 'DEBUG'
        LOGGING_LEVEL_ORG_SPRINGFRAMEWORK = 'DEBUG'
        
        // EC2 Configuration
        EC2_HOST = '54.210.68.146'
        EC2_USER = 'ec2-user'
        EC2_KEY = credentials('EC2_SSH_KEY')
    }

    stages {
        stage('Debug Info') {
            steps {
                sh '''
                    echo "Git branch information:"
                    git branch -a
                    echo "Current commit:"
                    git rev-parse HEAD
                    echo "Remote branches:"
                    git remote show origin
                '''
            }
        }

        stage('Test Docker') {
            steps {
                script {
                    try {
                        // Check if Docker is installed
                        sh 'docker --version'
                        
                        // Check Docker daemon status
                        sh '''
                            if ! docker info > /dev/null 2>&1; then
                                echo "Docker daemon is not running. Please start Docker Desktop."
                                exit 1
                            fi
                            echo "Docker daemon is running"
                            docker info
                        '''
                    } catch (Exception e) {
                        echo "Error: ${e.message}"
                        echo "Please ensure Docker Desktop is running and you have proper permissions."
                        currentBuild.result = 'FAILURE'
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
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}:${BUILD_NUMBER}", "--platform linux/amd64 .")
                    docker.build("${DOCKERHUB_REPO}:latest", "--platform linux/amd64 .")
                }
            }
        }

        stage('Push to Docker Hub') {
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
                expression { 
                    def remoteBranches = sh(script: 'git branch -r', returnStdout: true).trim()
                    return remoteBranches.contains('origin/development')
                }
            }
            steps {
                script {
                    echo "Deploying to development environment..."
                    echo "Using database URL: ${DEV_DB_URL}"
                    echo "Using port: ${DEV_PORT}"
                    
                    // Only stop and remove if it exists
                    sh """
                        if docker ps -a | grep -q ${CONTAINER_NAME}-dev; then
                            docker stop ${CONTAINER_NAME}-dev || true
                            docker rm ${CONTAINER_NAME}-dev || true
                        fi
                    """
                    
                    sh """
                        docker run -d --name ${CONTAINER_NAME}-dev \
                        -p ${DEV_PORT}:8080 \
                        -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
                        -e SPRING_DATASOURCE_URL=${DEV_DB_URL} \
                        -e SPRING_DATASOURCE_USERNAME=${DEV_DB_USERNAME} \
                        -e SPRING_DATASOURCE_PASSWORD=${DEV_DB_PASSWORD} \
                        -e SPRING_JPA_HIBERNATE_DDL_AUTO=${SPRING_JPA_HIBERNATE_DDL_AUTO} \
                        -e SPRING_JPA_SHOW_SQL=${SPRING_JPA_SHOW_SQL} \
                        -e SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT} \
                        -e SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL} \
                        -e SERVER_PORT=${SERVER_PORT} \
                        -e LOGGING_LEVEL_ROOT=${LOGGING_LEVEL_ROOT} \
                        -e LOGGING_LEVEL_ORG_HIBERNATE=${LOGGING_LEVEL_ORG_HIBERNATE} \
                        -e LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=${LOGGING_LEVEL_ORG_SPRINGFRAMEWORK} \
                        ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                    """
                    
                    echo "Container started. Waiting for application to initialize..."
                    sh "sleep 45"  // Increased wait time to 45 seconds
                    
                    echo "Container status:"
                    sh "docker ps -a | grep ${CONTAINER_NAME}-dev || true"
                    
                    echo "Full container logs:"
                    sh "docker logs ${CONTAINER_NAME}-dev || true"
                    
                    echo "Checking if container is running..."
                    def containerStatus = sh(script: "docker inspect -f '{{.State.Status}}' ${CONTAINER_NAME}-dev", returnStdout: true).trim()
                    if (containerStatus != "running") {
                        echo "Container is not running. Status: ${containerStatus}"
                        echo "Last 100 lines of container logs:"
                        sh "docker logs --tail 100 ${CONTAINER_NAME}-dev || true"
                        echo "Container exit code:"
                        sh "docker inspect -f '{{.State.ExitCode}}' ${CONTAINER_NAME}-dev || true"
                        error "Container failed to start properly"
                    }
                    
                    echo "Container is running. Testing application health..."
                    def maxRetries = 5
                    def retryCount = 0
                    def healthCheckPassed = false
                    
                    while (retryCount < maxRetries && !healthCheckPassed) {
                        try {
                            sh "curl -v http://localhost:${DEV_PORT}/actuator/health"
                            healthCheckPassed = true
                            echo "Health check passed!"
                        } catch (Exception e) {
                            retryCount++
                            if (retryCount < maxRetries) {
                                echo "Health check failed. Retrying in 15 seconds... (Attempt ${retryCount}/${maxRetries})"
                                sh "sleep 15"  // Increased retry wait time
                                echo "Container logs since last attempt:"
                                sh "docker logs --since 15s ${CONTAINER_NAME}-dev || true"
                            } else {
                                echo "Health check failed after ${maxRetries} attempts. Full container logs:"
                                sh "docker logs ${CONTAINER_NAME}-dev || true"
                                echo "Container exit code:"
                                sh "docker inspect -f '{{.State.ExitCode}}' ${CONTAINER_NAME}-dev || true"
                                echo "Container resource usage:"
                                sh "docker stats --no-stream ${CONTAINER_NAME}-dev || true"
                                error "Application failed to start properly"
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    sh "docker stop ${CONTAINER_NAME}-prod || true"
                    sh "docker rm ${CONTAINER_NAME}-prod || true"
                    sh "docker run -d --name ${CONTAINER_NAME}-prod \
                        -p 8080:8080 \
                        -e SPRING_PROFILES_ACTIVE=prod \
                        -e DB_URL=${PROD_DB_URL} \
                        -e DB_USERNAME=${PROD_DB_USERNAME} \
                        -e DB_PASSWORD=${PROD_DB_PASSWORD} \
                        ${DOCKERHUB_REPO}:${BUILD_NUMBER}"
                }
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
                    
                    // Create deployment script with proper image reference
                    writeFile file: 'deploy-ec2.sh', text: """
                        #!/bin/bash
                        
                        # Stop and remove existing container
                        docker stop hotel-booking-container || true
                        docker rm hotel-booking-container || true
                        
                        # Pull latest image
                        docker pull ${DOCKERHUB_USER}/hotel-booking-app:${BUILD_NUMBER}
                        
                        # Run new container
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
                        
                        # Wait for application to start
                        echo "Waiting for application to start..."
                        sleep 30
                        
                        # Check if container is running
                        if docker ps | grep -q hotel-booking-container; then
                            echo "Application deployed successfully!"
                        else
                            echo "Application failed to start. Check logs with: docker logs hotel-booking-container"
                            exit 1
                        fi
                    """
                    
                    // Make script executable
                    sh 'chmod +x deploy-ec2.sh'
                    
                    // Create directory and copy script to EC2
                    sh """
                        # Create directory on EC2
                        ssh -i ${EC2_KEY} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'mkdir -p ~/hotel-booking'
                        
                        # Copy script to EC2
                        scp -i ${EC2_KEY} -o StrictHostKeyChecking=no deploy-ec2.sh ${EC2_USER}@${EC2_HOST}:~/hotel-booking/
                        
                        # Execute script on EC2
                        ssh -i ${EC2_KEY} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'cd ~/hotel-booking && chmod +x deploy-ec2.sh && ./deploy-ec2.sh'
                    """
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
            echo 'Application deployed to EC2 instance.'
            echo "EC2 Host: ${EC2_HOST}"
        }
        failure {
            echo 'Pipeline failed!'
            echo 'Check Jenkins logs for details.'
        }
    }
}
