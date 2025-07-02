pipeline {
    agent any

    environment {
        IMAGE_NAME = "marammanai/user-service"
        IMAGE_TAG = "latest"
        FULL_IMAGE = "${IMAGE_NAME}:${IMAGE_TAG}"
        K8S_MASTER = "ceph1@192.168.13.11"
        DEPLOY_YAML = "k8s-user-deployment.yaml"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
            }
        }

        stage('Build JAR') {
            steps {
                sh '''
                    echo "🔧 Build Maven..."
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    echo "🐳 Docker build"
                    docker build -t $FULL_IMAGE .
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "🔐 Docker login"
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $FULL_IMAGE
                    '''
                }
            }
        }

        stage('Replace Image in YAML') {
            steps {
                sh '''
                    echo "📝 Remplacement de l'image dans le fichier YAML"
                    sed -i "s|__IMAGE__|$FULL_IMAGE|g" $DEPLOY_YAML
                '''
            }
        }

        stage('Copy YAML') {
            steps {
                sh '''
                    echo "📁 Copie YAML vers master"
                    ssh-keyscan -H 192.168.13.11 >> ~/.ssh/known_hosts
                    scp $DEPLOY_YAML $K8S_MASTER:/home/ceph1/$DEPLOY_YAML
                '''
            }
        }

        stage('Deploy K8s') {
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
            echo "✅ Déploiement user-service terminé !"
        }
        failure {
            echo "❌ Échec du pipeline user-service !"
        }
    }
}
