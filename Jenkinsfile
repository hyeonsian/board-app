// Part 8 파이프라인: Checkout → Build & Test → Archive → Deploy → Smoke test
//
// 사전 준비 (가이드 Part 7~8 참고)
//   1) Jenkins 전역 환경변수:  WAS_HOSTS = WAS 사설 IP (여러 대면 공백으로 구분)
//                              APP_URL  = http://<WEB 사설 IP>
//   2) Jenkins Credentials:    종류 "SSH Username with private key", ID = was-ssh-key, 사용자 = rocky
//   3) Jenkins 플러그인:       SSH Agent
pipeline {
  agent any

  options {
    skipDefaultCheckout(true)          // 아래 Checkout 단계에서 직접 체크아웃
    disableConcurrentBuilds()          // 동시에 두 번 배포되는 사고 방지
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  // 2분마다 Git 에 새 커밋이 있는지 확인 (Webhook 을 쓰지 않는 가장 단순한 방법)
  // 주의: 이 설정은 파이프라인이 한 번 실행된 뒤부터 적용됩니다. 첫 빌드는 직접 눌러 주세요.
  triggers { pollSCM('H/2 * * * *') }

  environment {
    DEPLOY_USER = 'rocky'
    SSH_OPTS    = '-o StrictHostKeyChecking=accept-new -o ConnectTimeout=10'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        sh '''
          GIT_SHORT=$(git rev-parse --short HEAD)
          mvn -B -ntp clean package -Dbuild.number=${BUILD_NUMBER} -Dgit.commit=${GIT_SHORT}
        '''
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
      }
    }

    stage('Archive') {
      steps {
        archiveArtifacts artifacts: 'target/ROOT.war', fingerprint: true
      }
    }

    stage('Deploy') {
      steps {
        sshagent(credentials: ['was-ssh-key']) {
          sh '''
            : "${WAS_HOSTS:?Jenkins 전역 환경변수 WAS_HOSTS 를 설정하세요}"
            for H in ${WAS_HOSTS}; do
              echo "=== [$H] 배포 ==="
              ssh ${SSH_OPTS} ${DEPLOY_USER}@${H} 'mkdir -p /tmp/deploy'
              scp ${SSH_OPTS} target/ROOT.war deploy/deploy.sh ${DEPLOY_USER}@${H}:/tmp/deploy/
              ssh ${SSH_OPTS} ${DEPLOY_USER}@${H} "sudo install -D -m 750 /tmp/deploy/deploy.sh /opt/deploy/deploy.sh && sudo /opt/deploy/deploy.sh deploy /tmp/deploy/ROOT.war ${BUILD_NUMBER}"
            done
          '''
        }
      }
    }

    stage('Smoke test') {
      steps {
        sh '''
          : "${APP_URL:?Jenkins 전역 환경변수 APP_URL 을 설정하세요}"
          EXPECT='"build":"'"${BUILD_NUMBER}"'"'
          for i in 1 2 3 4 5 6; do
            RESP=$(curl -fsS -m 5 "${APP_URL}/health" || true)
            echo "$RESP"
            if echo "$RESP" | grep -qF "$EXPECT"; then
              echo "배포된 버전 확인 OK (build ${BUILD_NUMBER})"
              exit 0
            fi
            sleep 5
          done
          echo "응답한 앱의 build 번호가 ${BUILD_NUMBER} 가 아닙니다 (배포 안 됨 또는 Apache 경로 문제)"
          exit 1
        '''
      }
    }
  }

  post {
    success { echo "배포 성공: build #${env.BUILD_NUMBER}" }
    failure { echo '실패: 어느 stage 인지, deploy.sh 가 자동 롤백했는지 위 로그에서 확인하세요.' }
  }
}
