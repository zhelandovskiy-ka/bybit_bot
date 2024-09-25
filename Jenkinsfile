pipeline {
    agent any
    stages {
        stage('Clone repository') {
            steps {
                git branch: 'develop', url: 'https://github.com/zhelandovskiy-ka/bybit_bot.git'
            }
        }

        stage ('Build') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('Deploy') {
            steps {
                sh 'java --enable-preview -jar target/bybit_bot-1.0.0-SNAPSHOT.jar'
            }
        }
    }
}