pipeline {
    agent any

    environment {
        IMAGE_NAME = 'hotel-booking-app'
        DOCKERHUB_USER = 'your-dockerhub-username'
        DOCKERHUB_REPO = "${DOCKERHUB_USER}/${IMAGE_NAME}"
        CONTAINER_NAME = 'hotel-booking-container'
    }

    stages {
        stage('Clone Repo') {
            steps {
                git 'https://github.com/LovishGarg2004/hotel-booking-application.git'
            }
        }

        stage('Build JAR') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh 'echo "$PASSWORD" | docker login -u "$USERNAME" --password-stdin'
                    sh "docker push ${DOCKERHUB_REPO}"
                }
            }
        }

        stage('Run Docker Container Locally') {
            steps {
                script {
                    sh "docker stop ${CONTAINER_NAME} || true"
                    sh "docker rm ${CONTAINER_NAME} || true"
                    sh "docker run -d --name ${CONTAINER_NAME} -p 8080:8080 ${DOCKERHUB_REPO}"
                }
            }
        }
    }
}
