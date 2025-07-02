pipeline {
    agent any

    environment {
        VERSION_FILE = ".build_version"
        IMAGE_NAME = "marammanai/user-service"
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
            }
        }

        stage('Build, Version & Docker Push') {
            steps {
                script {
                    // Étape 1 : Incrémentation version
                    if (!fileExists(env.VERSION_FILE)) {
                        writeFile file: env.VERSION_FILE, text: "0"
                    }
                    def version = readFile(env.VERSION_FILE).trim()
                    if (!version.isInteger()) {
                        error "❌ .build_version contient une valeur invalide : ${version}"
                    }
                    version = version.toInteger() + 1
                    writeFile file: env.VERSION_FILE, text: version.toString()
                    def imageTag = "v${version}"

                    // Étape 2 : Build le projet
                    sh 'chmod +x mvnw'
                    sh './mvnw clean package -DskipTests'

                    // Étape 3 : Build + push Docker
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            docker build -t ${env.IMAGE_NAME}:${imageTag} .
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker push ${env.IMAGE_NAME}:${imageTag}
                        """
                    }

                    // Étape 4 : Générer le YAML avec la bonne image tag
                    sh """
                        sed 's|REPLACE_IMAGE|${env.IMAGE_NAME}:${imageTag}|g' k8s/user-deployment.yaml > k8s/deployment-generated.yaml
                    """

                    // Étape 5 : Copier le fichier sur le master K8s
                    sh 'scp -o StrictHostKeyChecking=no -i /root/.ssh/id_rsa k8s/deployment-generated.yaml root@192.168.56.100:/root/'

                    // Étape 6 : Appliquer le déploiement sur Kubernetes
                    sh 'ssh -o StrictHostKeyChecking=no -i /root/.ssh/id_rsa root@192.168.56.100 "kubectl apply -f /root/deployment-generated.yaml"'

                    // Message de succès
                    echo "✅ Déploiement terminé : ${env.IMAGE_NAME}:${imageTag}"
                }
            }
        }
    }

    post {
        failure {
            echo '❌ Pipeline échoué'
        }
        success {
            echo '✅ Pipeline réussi'
        }
    }
}
