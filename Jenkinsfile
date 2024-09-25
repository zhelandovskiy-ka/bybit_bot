pipeline {
    agent any

    enviroment {
        SPRING_DATASOURCE_URL = credentials('SPRING_DATASOURCE_URL')
        SPRING_DATASOURCE_CREDS = credentials('SPRING_DATASOURCE_CREDS')
    }

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
            echo "${SPRING_DATASOURCE_CREDS_USR}"
            echo "${SPRING_DATASOURCE_CREDS_PSW}"
            echo "${SPRING_DATASOURCE_CREDS_URL}"

            steps {
                sh 'java --enable-preview -jar target/bybit_bot-1.0.0-SNAPSHOT.jar'
            }
        }
    }
}