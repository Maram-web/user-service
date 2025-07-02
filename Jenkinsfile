pipeline {
    agent any

    environment {
        DOCKER_REPO = "marammanai/user-service"
        BUILD_TAG = "v${BUILD_ID}"  // Utilise le numéro de build Jenkins pour un tag unique
        IMAGE_NAME = "${DOCKER_REPO}:${BUILD_TAG}"
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
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        docker build -t $IMAGE_NAME .
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME
                    """
                }
            }
        }

        stage('Update & Copy YAML') {
            steps {
                script {
                    sh """
                        sed 's|REPLACE_IMAGE|$IMAGE_NAME|g' $DEPLOY_YAML > temp.yaml
                        ssh-keyscan -H 192.168.13.11 >> ~/.ssh/known_hosts
                        scp temp.yaml $K8S_MASTER:/home/ceph1/$DEPLOY_YAML
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                sh "ssh $K8S_MASTER kubectl apply -f /home/ceph1/$DEPLOY_YAML"
            }
        }
    }

    post {
        success {
            echo "✅ user-service deployed! Image: $IMAGE_NAME"
        }
        failure {
            echo "❌ Deployment failed"
        }
    }
}
