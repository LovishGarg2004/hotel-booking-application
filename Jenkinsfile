pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'hotel-booking-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        CONTAINER_PORT = 8080
        HOST_PORT = 8080
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
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
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sh """
                        # Stop and remove existing container if it exists
                        if docker ps -a | grep -q ${DOCKER_IMAGE}; then
                            docker stop ${DOCKER_IMAGE} || true
                            docker rm ${DOCKER_IMAGE} || true
                        fi
                        
                        # Run the new container
                        docker run -d \\
                            --name ${DOCKER_IMAGE} \\
                            -p ${HOST_PORT}:${CONTAINER_PORT} \\
                            -e SPRING_PROFILES_ACTIVE=prod \\
                            --restart unless-stopped \\
                            ${DOCKER_IMAGE}:${DOCKER_TAG}
                            
                        # Clean up old images
                        docker image prune -f
                    """
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed! Check the logs for details.'
        }
    }
}