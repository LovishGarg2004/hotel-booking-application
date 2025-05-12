pipeline {
    agent any

    environment {
        IMAGE_NAME = "hotel-booking-app"
        TAG = "latest"
        CONTAINER_NAME = "hotel-booking-app"
        DOCKER_PATH = "/usr/local/bin/docker" // Mac default Docker path
    }

    stages {
        stage('Check Docker') {
            steps {
                script {
                    try {
                        sh 'docker info'
                    } catch (Exception e) {
                        error "Docker is not running or not accessible. Please start Docker Desktop and try again."
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build JAR') {
            steps {
                script {
                    try {
                        sh 'chmod +x gradlew'
                        sh './gradlew clean build'
                    } catch (Exception e) {
                        error "Failed to build JAR: ${e.message}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    try {
                        // Remove existing container if it exists
                        sh 'docker ps -q -f name=$CONTAINER_NAME | grep -q . && docker stop $CONTAINER_NAME || true'
                        sh 'docker ps -aq -f name=$CONTAINER_NAME | grep -q . && docker rm $CONTAINER_NAME || true'
                        
                        // Remove existing image if it exists
                        sh 'docker images -q $IMAGE_NAME:$TAG | grep -q . && docker rmi $IMAGE_NAME:$TAG || true'
                        
                        // Build new image
                        sh 'docker build -t $IMAGE_NAME:$TAG .'
                    } catch (Exception e) {
                        error "Failed to build Docker image: ${e.message}"
                    }
                }
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
                    try {
                        // Check if port 8080 is already in use
                        sh '''
                            if lsof -i :8080 > /dev/null; then
                                echo "Port 8080 is already in use. Stopping existing process..."
                                lsof -ti :8080 | xargs kill -9 || true
                            fi
                        '''
                        
                        sh 'docker run -d -p 8080:8080 --name $CONTAINER_NAME $IMAGE_NAME:$TAG'
                        
                        // Wait for container to be healthy
                        sh '''
                            for i in {1..30}; do
                                if docker ps | grep -q $CONTAINER_NAME; then
                                    echo "Container is running"
                                    break
                                fi
                                if [ $i -eq 30 ]; then
                                    echo "Container failed to start"
                                    exit 1
                                fi
                                sleep 1
                            done
                        '''
                    } catch (Exception e) {
                        error "Failed to run Docker container: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Cleanup
                sh '''
                    docker ps -q -f name=$CONTAINER_NAME | grep -q . && docker stop $CONTAINER_NAME || true
                    docker ps -aq -f name=$CONTAINER_NAME | grep -q . && docker rm $CONTAINER_NAME || true
                '''
                cleanWs()
            }
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check the logs for details.'
        }
    }
}