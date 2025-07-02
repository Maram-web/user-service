pipeline {
    agent any

    environment {
        IMAGE_NAME = "marammanai/user-service:latest"
        K8S_MASTER = "ceph1@192.168.13.11"
        DEPLOY_YAML = "k8s-user-deployment.yaml"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'user-service', url: 'https://github.com/Maram-web/ceph-back.git'
            }
        }

        stage('Analyse des changements') {
            steps {
                script {
                    def changes = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                    echo "📂 Fichiers modifiés:\n${changes}"

                    env.NEED_BUILD_JAR = (changes.contains("src/") || changes.contains("pom.xml")) ? "true" : "false"
                    env.NEED_BUILD_DOCKER = (changes.contains("Dockerfile") || changes.contains("src/")) ? "true" : "false"
                }
            }
        }

        stage('Build JAR') {
            when {
                expression { env.NEED_BUILD_JAR == "true" }
            }
            steps {
                sh '''
                    echo "🔧 Compilation du projet Spring Boot"
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        stage('Docker Build') {
            when {
                expression { env.NEED_BUILD_DOCKER == "true" }
            }
            steps {
                sh '''
                    echo "🐳 Construction de l'image Docker"
                    docker build -t $IMAGE_NAME .
                '''
            }
        }

        stage('Docker Push') {
            when {
                expression { env.NEED_BUILD_DOCKER == "true" }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "📤 Connexion Docker Hub & push"
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME
                    '''
                }
            }
        }

        stage('Copy YAML') {
            steps {
                sh '''
                    echo "📁 Copie du fichier YAML vers le master Kubernetes"
                    ssh-keyscan -H 192.168.13.11 >> ~/.ssh/known_hosts
                    scp $DEPLOY_YAML $K8S_MASTER:/home/ceph1/$DEPLOY_YAML
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    echo "🚀 Déploiement sur Kubernetes"
                    ssh $K8S_MASTER kubectl apply -f /home/ceph1/$DEPLOY_YAML
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Déploiement terminé avec succès"
        }
        failure {
            echo "❌ Le pipeline a échoué"
        }
    }
}
