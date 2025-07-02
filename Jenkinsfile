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
                git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
            }
        }

        stage('Build App') {
            steps {
                sh '''
                    echo "🛠️ Build du projet Java"
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    echo "🐳 Construction de l'image Docker"
                    docker build -t $IMAGE_NAME .
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "📤 Connexion à Docker Hub & push"
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
            echo "✅ user-service déployé avec succès !"
        }
        failure {
            echo "❌ Échec du déploiement user-service."
        }
    }
}
