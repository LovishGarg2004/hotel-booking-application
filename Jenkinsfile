pipeline {
    agent any
    
    environment {
        // Application configuration
        APP_NAME = 'hotel-booking-app'
        DOCKER_IMAGE = "${APP_NAME}"
        DOCKER_TAG = "${env.BUILD_NUMBER ?: 'latest'}"
        CONTAINER_PORT = '8080'
        HOST_PORT = '8081'
    }
    

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Set Up Environment') {
            steps {
                script {
                    // Verify Java and Docker
                    sh 'java -version'
                    sh 'docker --version'
                    
                    // Make gradlew executable
                    sh 'chmod +x gradlew'
                    
                    // Clean up old containers and images
                    sh '''
                        # Stop and remove any existing containers
                        if docker ps -a | grep -q ${DOCKER_IMAGE}; then
                            docker stop ${DOCKER_IMAGE} || true
                            docker rm ${DOCKER_IMAGE} || true
                        fi
                        
                        # Remove old images to save space
                        docker system prune -f
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    // Build the application
                    sh './gradlew clean build -x test --no-daemon'
                    
                    // Check if build was successful
                    def jarFile = findFiles(glob: 'build/libs/*.jar')[0]?.name
                    if (!jarFile) {
                        error 'No JAR file found after build!'
                    }
                    echo "Built JAR: ${jarFile}"
                }
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test --no-daemon'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                    // Archive test results
                    archiveArtifacts 'build/test-results/**/*'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    
                    // Tag as latest
                    sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    // Run the new container
                    sh """
                        # Stop and remove existing container if it exists
                        if docker ps -a | grep -q ${DOCKER_IMAGE}; then
                            docker stop ${DOCKER_IMAGE} || true
                            docker rm ${DOCKER_IMAGE} || true
                        fi
                        
                        # Check if port is in use
                        if lsof -i:${HOST_PORT}; then
                            echo "Port ${HOST_PORT} is in use, attempting to free it..."
                            CONTAINER_ID=\$(docker ps -q --filter "publish=${HOST_PORT}")
                            if [ ! -z "\$CONTAINER_ID" ]; then
                                echo "Stopping container using port ${HOST_PORT}..."
                                docker stop \$CONTAINER_ID || true
                                docker rm \$CONTAINER_ID || true
                            fi
                        fi
                        
                        # Run the new container
                        docker run -d \\
                            --name ${DOCKER_IMAGE} \\
                            -p ${HOST_PORT}:${CONTAINER_PORT} \\
                            -e SPRING_PROFILES_ACTIVE=prod \\
                            -e SPRING_DATASOURCE_URL=\\\${SPRING_DATASOURCE_URL} \\
                            -e SPRING_DATASOURCE_USERNAME=\\\${SPRING_DATASOURCE_USERNAME} \\
                            -e SPRING_DATASOURCE_PASSWORD=\\\${SPRING_DATASOURCE_PASSWORD} \\
                            --restart unless-stopped \\
                            ${DOCKER_IMAGE}:${DOCKER_TAG}
                        
                        # Verify container is running
                        sleep 10
                        docker ps | grep ${DOCKER_IMAGE}
                        
                        echo "Application should be available at: http://\$(hostname -I | awk '{print \$1}'):${HOST_PORT}"
                    """
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace
            cleanWs()
        }
        success {
            echo '🎉 Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed! Check the logs for details.'
        }
    }
}
pipeline {
    agent any
    
    environment {
        // Application configuration
        DOCKER_IMAGE = 'hotel-booking-app'
        DOCKER_TAG = "${env.BUILD_NUMBER ?: 'latest'}"
        CONTAINER_PORT = '8080'
        HOST_PORT = '8081'
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
                sh './gradlew clean build -x test --no-daemon'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test --no-daemon'
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
                        # Stop and remove existing container
                        if docker ps -a | grep -q ${DOCKER_IMAGE}; then
                            docker stop ${DOCKER_IMAGE} || true
                            docker rm ${DOCKER_IMAGE} || true
                        fi

                        # Run new container
                        docker run -d \\
                            --name ${DOCKER_IMAGE} \\
                            -p ${HOST_PORT}:${CONTAINER_PORT} \\
                            -e SPRING_PROFILES_ACTIVE=prod \\
                            --restart unless-stopped \\
                            ${DOCKER_IMAGE}:${DOCKER_TAG}

                        # Verify container is running
                        sleep 5
                        docker ps | grep ${DOCKER_IMAGE}
                        
                        echo "Application should be available at: http://\$(hostname -I | awk '{print \$1}'):${HOST_PORT}"
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