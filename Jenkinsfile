pipeline {
    agent any

    environment {
        VERSION_FILE = ".build_version"
        IMAGE_NAME = "marammanai/user-service"
        IMAGE_TAG = ""
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
                    if (!fileExists(env.VERSION_FILE)) {
                        writeFile file: env.VERSION_FILE, text: "0"
                    }
                    def version = readFile(env.VERSION_FILE).trim()
                    if (!version.isInteger()) {
                        error "❌ .build_version contains invalid value: ${version}"
                    }
                    version = version.toInteger() + 1
                    writeFile file: env.VERSION_FILE, text: version.toString()
                    env.IMAGE_TAG = "v${version}"

                    sh 'chmod +x mvnw'
                    sh './mvnw clean package -DskipTests'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        docker build -t $IMAGE_NAME:$IMAGE_TAG .
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME:$IMAGE_TAG
                    """
                }
            }
        }

        stage('Prepare YAML') {
            steps {
                script {
                    sh """
                        sed 's|REPLACE_IMAGE|$IMAGE_NAME:$IMAGE_TAG|g' k8s/user-deployment.yaml > k8s/deployment-generated.yaml
                    """
                }
            }
        }

        stage('Copy YAML to K8s Master') {
            steps {
                sh 'scp -o StrictHostKeyChecking=no -i /root/.ssh/id_rsa k8s/deployment-generated.yaml root@192.168.56.100:/root/'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh 'ssh -o StrictHostKeyChecking=no -i /root/.ssh/id_rsa root@192.168.56.100 "kubectl apply -f /root/deployment-generated.yaml"'
            }
        }
    }

    post {
        failure {
            echo '❌ Échec du pipeline'
        }
        success {
            echo "✅ Déploiement de $IMAGE_NAME:$IMAGE_TAG réussi"
        }
    }
}
