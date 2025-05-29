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
        
        // SonarCloud Configuration
        SONAR_TOKEN = credentials('SONAR_TOKEN')
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

        stage('SonarCloud Analysis') {
            steps {
                script {
                    try {
                        withSonarQubeEnv('SonarCloud') {
                            withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                                sh '''
                                    ./gradlew sonarqube \
                                    -Dsonar.projectKey=hotel-booking-application \
                                    -Dsonar.organization=lovishgarg2004 \
                                    -Dsonar.host.url=https://sonarcloud.io \
                                    -Dsonar.login=${SONAR_TOKEN}
                                '''
                            }
                        }
                    } catch (Exception e) {
                        echo "Warning: SonarCloud analysis skipped - ${e.message}"
                    }
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
                branch 'main'  // Only deploy to EC2 from main branch
            }
            steps {
                script {
                    echo "Deploying to EC2 instance..."
                    
                    // Create deployment script
                    writeFile file: 'deploy-ec2.sh', text: '''
                        #!/bin/bash
                        
                        # Stop and remove existing container
                        docker stop hotel-booking-container || true
                        docker rm hotel-booking-container || true
                        
                        # Pull latest image
                        docker pull ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                        
                        # Run new container
                        docker run -d --name hotel-booking-container \
                            -p 8080:8080 \
                            -e SPRING_PROFILES_ACTIVE=prod \
                            -e SPRING_DATASOURCE_URL=${DEV_DB_URL} \
                            -e SPRING_DATASOURCE_USERNAME=${DEV_DB_USERNAME} \
                            -e SPRING_DATASOURCE_PASSWORD=${DEV_DB_PASSWORD} \
                            -e SPRING_JPA_HIBERNATE_DDL_AUTO=${SPRING_JPA_HIBERNATE_DDL_AUTO} \
                            -e SPRING_JPA_SHOW_SQL=${SPRING_JPA_SHOW_SQL} \
                            -e SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT} \
                            -e SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL} \
                            -e SERVER_PORT=${SERVER_PORT} \
                            ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                        
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
                    '''
                    
                    // Make script executable
                    sh 'chmod +x deploy-ec2.sh'
                    
                    // Copy script to EC2 and execute
                    withAWS(credentials: 'AWS_CREDENTIALS', region: env.EC2_REGION) {
                        sh '''
                            # First, ensure the SSH key has correct permissions
                            chmod 600 ${EC2_KEY}
                            
                            # Add host key to known hosts
                            ssh-keyscan -H 54.210.68.146 >> ~/.ssh/known_hosts
                            
                            # Create directory and copy script
                            ssh -i ${EC2_KEY} -o StrictHostKeyChecking=accept-new ec2-user@54.210.68.146 'mkdir -p ~/hotel-booking'
                            
                            # Copy script to EC2
                            scp -i ${EC2_KEY} -o StrictHostKeyChecking=accept-new deploy-ec2.sh ec2-user@54.210.68.146:~/hotel-booking/
                            
                            # Execute script on EC2
                            ssh -i ${EC2_KEY} -o StrictHostKeyChecking=accept-new ec2-user@54.210.68.146 'cd ~/hotel-booking && chmod +x deploy-ec2.sh && ./deploy-ec2.sh'
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            node {
                cleanWs()
                
                // SonarCloud Quality Gate
                script {
                    try {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    } catch (Exception e) {
                        echo "Warning: Quality gate check skipped - ${e.message}"
                    }
                }
            }
        }
        success {
            node {
                echo 'Pipeline completed successfully!'
                echo 'Container is still running. You can access the application at:'
                echo "http://localhost:${DEV_PORT}"
                echo "To check container logs: docker logs ${CONTAINER_NAME}-dev"
                echo "To stop container: docker stop ${CONTAINER_NAME}-dev"
            }
        }
        failure {
            node {
                echo 'Pipeline failed!'
                echo 'Container logs are preserved for debugging.'
                echo "To check container logs: docker logs ${CONTAINER_NAME}-dev"
                echo "To check container status: docker ps -a | grep ${CONTAINER_NAME}-dev"
                echo "To check container resource usage: docker stats ${CONTAINER_NAME}-dev"
            }
        }
    }
}
