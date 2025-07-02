pipeline {
    agent any

    environment {
        VERSION_FILE = ".build_version"
        K8S_MASTER = "ceph1@192.168.13.11"
        USER_DEPLOY = "k8s-user-deployment.yaml"
        MYSQL_DEPLOY = "k8s-mysql-deployment.yaml"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
            }
        }

        stage('Build & Versioning') {
            steps {
                script {
                    def version = readFile(env.VERSION_FILE).trim().toInteger() + 1
                    def imageName = "marammanai/user-service:v${version}"
                    writeFile file: env.VERSION_FILE, text: version.toString()
                    env.IMAGE_NAME = imageName

                    sh 'chmod +x mvnw'
                    sh './mvnw clean package -DskipTests'
                }
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

        stage('Prepare YAML') {
            steps {
                sh """
                    ssh-keyscan -H 192.168.13.11 >> ~/.ssh/known_hosts
                    sed -i 's|__IMAGE__|'${IMAGE_NAME}'|g' $USER_DEPLOY
                """
            }
        }

        stage('Copy YAML to K8s Master') {
            steps {
                sh """
                    scp $USER_DEPLOY $MYSQL_DEPLOY $K8S_MASTER:/home/ceph1/
                """
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                    ssh $K8S_MASTER "kubectl apply -f /home/ceph1/$MYSQL_DEPLOY"
                    ssh $K8S_MASTER "kubectl apply -f /home/ceph1/$USER_DEPLOY"
                """
            }
        }
    }

    post {
        success {
            echo "✅ Déploiement réussi (MySQL + User Service)"
        }
        failure {
            echo "❌ Échec du pipeline"
        }
    }
}
