pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'Java17'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Kyoo2321/Jenkins.git'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    // Publicar resultados JUnit (estadísticas en Jenkins)
                    junit '**/target/surefire-reports/*.xml'

                    // Publicar HTML de ExtentReports visualmente en Jenkins
                    publishHTML(target: [
                        reportDir: 'target',   // Carpeta donde están los HTML generados
                        reportFiles: 'ValidatePromartLoginPass.html,ValidatePromartLoginFail.html',
                        reportName: 'ExtentReports',
                        keepAll: true,
                        allowMissing: false,
                        alwaysLinkToLastBuild: true
                    ])
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
    }

    post {
        success {
            echo "✅ Build y pruebas completadas"
        }
        failure {
            echo "❌ Hubo errores en el pipeline"
        }
    }
}
