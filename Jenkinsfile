pipeline {
  agent any

  environment {
    TIMESTAMP = "${new Date().format('yyyyMMdd-HHmmss')}"
    IMAGE_TAG = "v${TIMESTAMP}"
    IMAGE_NAME = "marammanai/user-service:${IMAGE_TAG}"
    K8S_MASTER = "ceph1@192.168.13.11"
    DEPLOY_YAML = "k8s-user-deployment.yaml" // fichier yaml multi-doc que tu as montré
  } // <-- FERMETURE environment

  stages {

    stage('Checkout') {
      steps {
        git branch: 'user-service', url: 'https://github.com/Maram-web/user-service.git'
        sh 'git rev-parse --short HEAD || true'
      }
    }

    stage('Build Docker Image') {
      steps {
        sh '''
          echo "🐳 Build image tag: $IMAGE_TAG"
          docker build -t $IMAGE_NAME .
        '''
      }
    }

    stage('Push Docker Image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "📤 Push vers Docker Hub"
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push $IMAGE_NAME
          '''
        }
      }
    }

    stage('Update YAML with new image tag') {
      steps {
        sh '''
          echo "🛠 Mise à jour du YAML -> image: $IMAGE_NAME"
          sed "s|image: marammanai/user-service:.*|image: $IMAGE_NAME|" "$DEPLOY_YAML" > "updated-$DEPLOY_YAML"
          echo "Diff:"
          diff -u "$DEPLOY_YAML" "updated-$DEPLOY_YAML" || true
        '''
      }
    }

    stage('Copy YAML to Kubernetes Master') {
      steps {
        sh '''
          echo "📦 Copie du YAML mis à jour sur le master K8s"
          ssh-keyscan -H 192.168.13.11 >> ~/.ssh/known_hosts
          scp "updated-$DEPLOY_YAML" "$K8S_MASTER:/home/ceph1/$DEPLOY_YAML"
        '''
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        sh '''
          echo "🚀 Déploiement sur Kubernetes"
          ssh "$K8S_MASTER" kubectl apply -f "/home/ceph1/$DEPLOY_YAML"
          ssh "$K8S_MASTER" kubectl -n user rollout status deploy/user-service --timeout=120s || true
          ssh "$K8S_MASTER" kubectl -n user get pods -l app=user-service -o wide
        '''
      }
    }
  } // <-- FERMETURE stages

  post {
    success { echo "✅ user-service déployé avec succès : $IMAGE_NAME" }
    failure { echo "❌ Échec du déploiement de user-service." }
    always  { cleanWs() }
  }
}
