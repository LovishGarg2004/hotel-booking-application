pipeline {
    agent any

    environment {
        IMAGE_NAME = 'hotel-booking-app'
        DOCKERHUB_USER = 'your-dockerhub-username'
        DOCKERHUB_REPO = "${DOCKERHUB_USER}/${IMAGE_NAME}"
        CONTAINER_NAME = 'hotel-booking-container'
    }

    stages {
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
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh 'echo "$PASSWORD" | docker login -u "$USERNAME" --password-stdin'
                    sh "docker push ${DOCKERHUB_REPO}:${BUILD_NUMBER}"
                    sh "docker push ${DOCKERHUB_REPO}:latest"
                }
            }
        }

        stage('Deploy to Development') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    sh "docker stop ${CONTAINER_NAME}-dev || true"
                    sh "docker rm ${CONTAINER_NAME}-dev || true"
                    sh "docker run -d --name ${CONTAINER_NAME}-dev \
                        -p 8080:8080 \
                        -e SPRING_PROFILES_ACTIVE=dev \
                        -e DB_URL=${DEV_DB_URL} \
                        -e DB_USERNAME=${DEV_DB_USERNAME} \
                        -e DB_PASSWORD=${DEV_DB_PASSWORD} \
                        ${DOCKERHUB_REPO}:${BUILD_NUMBER}"
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
