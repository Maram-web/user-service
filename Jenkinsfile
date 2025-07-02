pipeline {
    agent any

    environment {
        IMAGE_NAME = "marammanai/user-service:latest"
        K8S_MASTER = "ceph1@192.168.13.11"
        DEPLOY_YAML = "k8s-user-deployment.yaml"
        NAMESPACE = "user"
        FORCE_BUILD = "true" // Forcer le build au premier run
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
            }
        }

        stage('Analyse des changements') {
            steps {
                script {
                    def changes = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                    echo "📂 Fichiers modifiés:\n${changes}"

                    env.NEED_BUILD_JAR = (
                        env.FORCE_BUILD == "true" ||
                        changes.contains("src/") ||
                        changes.contains("pom.xml")
                    ) ? "true" : "false"

                    env.NEED_BUILD_DOCKER = (
                        env.FORCE_BUILD == "true" ||
                        changes.contains("Dockerfile") ||
                        changes.contains("src/")
                    ) ? "true" : "false"
                }
            }
        }

        stage('Build JAR') {
            when {
                expression { env.NEED_BUILD_JAR == "true" }
            }
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            when {
                expression { env.NEED_BUILD_DOCKER == "true" }
            }
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Docker Push') {
            when {
                expression { env.NEED_BUILD_DOCKER == "true" }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME
                    '''
                }
            }
        }

        stage('Copy YAML') {
            steps {
                sh '''
                    ssh-keyscan -H 192.168.13.11 >> ~/.ssh/known_hosts
                    scp $DEPLOY_YAML $K8S_MASTER:/home/ceph1/$DEPLOY_YAML
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    ssh $K8S_MASTER kubectl apply -f /home/ceph1/$DEPLOY_YAML
                    ssh $K8S_MASTER kubectl rollout status deployment/user-service -n user
                '''
            }
        }
    }

    post {
        success {
            echo "✅ user-service deployed successfully in namespace 'user'!"
        }
        failure {
            echo "❌ user-service deployment failed!"
        }
    }
}
