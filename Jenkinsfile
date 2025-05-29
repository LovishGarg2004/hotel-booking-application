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
        DEV_DB_PASSWORD = 'postgres'
        DEV_PORT = '8081'
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
                    docker.build("${DOCKERHUB_REPO}:${BUILD_NUMBER}")
                    docker.build("${DOCKERHUB_REPO}:latest")
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
                    
                    sh "docker stop ${CONTAINER_NAME}-dev || true"
                    sh "docker rm ${CONTAINER_NAME}-dev || true"
                    sh """
                        docker run -d --name ${CONTAINER_NAME}-dev \
                        -p ${DEV_PORT}:8080 \
                        -e SPRING_PROFILES_ACTIVE=dev \
                        -e SPRING_DATASOURCE_URL=${DEV_DB_URL} \
                        -e SPRING_DATASOURCE_USERNAME=${DEV_DB_USERNAME} \
                        -e SPRING_DATASOURCE_PASSWORD=${DEV_DB_PASSWORD} \
                        ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                    """
                    
                    echo "Container started. Checking logs..."
                    sh "docker logs ${CONTAINER_NAME}-dev"
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
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
