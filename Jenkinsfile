pipeline {
    agent any

    environment {
        IMAGE_NAME = "marammanai/user-service:latest"
        K8S_MASTER = "root@192.168.56.100"
        DEPLOY_YAML = "k8s-user-deployment.yaml"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "🐳 Building Docker image"
                        docker build -t $IMAGE_NAME .

                        echo "📤 Pushing to Docker Hub"
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME
                    """
                }
            }
        }

        stage('Copy YAML to K8s Master') {
            steps {
                sh '''
                    echo "📁 Copying deployment YAML to Kubernetes master"
                    ssh-keyscan -H 192.168.56.100 >> ~/.ssh/known_hosts
                    scp k8s-user-deployment.yaml $K8S_MASTER:/root/
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    echo "🚀 Applying deployment on Kubernetes"
                    ssh $K8S_MASTER "kubectl apply -f /root/k8s-user-deployment.yaml"
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Déploiement réussi de user-service (latest)'
        }
        failure {
            echo '❌ Le pipeline a échoué'
        }
    }
}
