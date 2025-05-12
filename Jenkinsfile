pipeline {
    agent any

    environment {
        IMAGE_NAME = "hotel-booking-app"
        TAG = "latest"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build JAR') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$TAG .'
            }
        }
        stage('Run Docker Container') {
            steps {
                sh 'docker run -d -p 8080:8080 --name $IMAGE_NAME $IMAGE_NAME:$TAG'
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}