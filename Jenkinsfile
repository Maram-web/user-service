pipeline {
    agent any

    environment {
        VERSION_FILE = ".build_version"
        IMAGE_NAME = "marammanai/user-service"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
            }
        }

        stage('Build & Versioning & Docker') {
            steps {
                script {
                    // Read & increment version
                    if (!fileExists(env.VERSION_FILE)) {
                        writeFile file: env.VERSION_FILE, text: "0"
                    }
                    def version = readFile(env.VERSION_FILE).trim()
                    if (!version.isInteger()) {
                        error "❌ .build_version contains invalid value: ${version}"
                    }
                    version = version.toInteger() + 1
                    writeFile file: env.VERSION_FILE, text: version.toString()
                    def imageTag = "v${version}" // local only

                    // Build the JAR
                    sh 'chmod +x mvnw'
                    sh './mvnw clean package -DskipTests'

                    // Docker build & push
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            docker build -t ${env.IMAGE_NAME}:${imageTag} .
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker push ${env.IMAGE_NAME}:${imageTag}
                        """
                    }

                    // Prepare YAML
                    sh """
                        sed 's|REPLACE_IMAGE|${env.IMAGE_NAME}:${imageTag}|g' k8s/user-deployment.yaml > k8s/deployment-generated.yaml
                    """

                    // Copy YAML
                    sh 'scp -o StrictHostKeyChecking=no -i /root/.ssh/id_rsa k8s/deployment-generated.yaml root@192.168.56.100:/root/'

                    // Deploy to K8s
                    sh 'ssh -o StrictHostKeyChecking=no -i /root/.ssh/id_rsa root@192.168.56.100 "kubectl apply -f /root/deployment-generated.yaml"'
                }
            }
        }
    }

    post {
        failure {
            echo '❌ Échec du pipeline'
        }
        success {
            echo '✅ Pipeline exécuté avec succès'
        }
    }
}
