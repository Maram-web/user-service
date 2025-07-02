pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "marammanai/user-service"
        DOCKER_CREDENTIALS_ID = 'docker-hub-creds'
        DEPLOY_YAML = "k8s-user-deployment.yaml"
        K8S_MASTER = "ceph1@192.168.13.11"
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
                    def versionFile = '.build_version'
                    def version = 'v1'

                    if (fileExists(versionFile)) {
                        version = readFile(versionFile).trim()
                        def versionNumber = version.replace("v", "").toInteger() + 1
                        version = "v${versionNumber}"
                    }

                    writeFile(file: versionFile, text: version)
                    IMAGE_TAG = "${DOCKER_IMAGE}:${version}"
                    env.IMAGE_TAG = IMAGE_TAG

                    sh "chmod +x mvnw"
                    sh "./mvnw clean package -DskipTests"
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker build -t $IMAGE_TAG .
                        docker push $IMAGE_TAG
                    '''
                }
            }
        }

        stage('Prepare YAML') {
            steps {
                script {
                    def yamlContent = readFile("${DEPLOY_YAML}")
                    yamlContent = yamlContent.replace("__IMAGE__", IMAGE_TAG)
                    writeFile(file: 'generated.yaml', text: yamlContent)
                }
            }
        }

        stage('Copy YAML to K8s Master') {
            steps {
                sh '''
                    ssh-keyscan -H 192.168.13.11 >> ~/.ssh/known_hosts
                    scp generated.yaml $K8S_MASTER:/home/ceph1/generated-user.yaml
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    ssh $K8S_MASTER kubectl apply -f /home/ceph1/generated-user.yaml
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Déploiement réussi avec tag : ${env.IMAGE_TAG}"
        }
        failure {
            echo "❌ Échec du pipeline"
        }
    }
}
