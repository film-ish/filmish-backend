pipeline {
    agent any

    environment {
        DB_URL = credentials('DB_URL')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
    }

    stages {
        stage('Build & Test') {
            steps {
                echo '빌드 및 테스트 실행 중...'
                sh 'chmod +x ./gradlew' // 실행 권한 추가
                sh './gradlew build'
            }
        }

        stage('Docker 이미지 생성') {
            steps {
                echo 'Docker 이미지 생성 중...'
                // Jenkins에서 빌드된 JAR 파일을 Dockerfile에 전달하기 위해 build-arg 사용
                sh 'docker build --build-arg JAR_FILE=build/libs/*.jar -t imoong/knockknock-backend:latest .'
            }
        }

        stage('Docker Hub 로그인 및 푸시') {
            steps {
                echo 'Docker Hub에 이미지 푸시 중...'
                withCredentials([usernamePassword(credentialsId: 'Docker-hub',
                                                  usernameVariable: 'DOCKER_HUB_USER',
                                                  passwordVariable: 'DOCKER_HUB_PASS')]) {
                    sh 'echo $DOCKER_HUB_PASS | docker login -u $DOCKER_HUB_USER --password-stdin'
                    sh 'docker push imoong/knockknock-backend:latest'
                }
            }
            post {
                always {
                    sh 'docker logout'
                }
            }
        }

        stage('배포') {
            steps {
                echo '배포 실행 중...'
                sh '''
                    # 백엔드 .env 파일 생성
                    cat > /home/ubuntu/knockknock/backend.env << EOL
        DB_URL=${DB_URL}
        DB_USERNAME=${DB_USERNAME}
        DB_PASSWORD=${DB_PASSWORD}
        EOL

                    # 배포 스크립트 실행
                    cd /home/ubuntu/knockknock && ./scripts/deploy.sh backend
                '''
            }
        }
    }

    post {
        success {
            mattermostSend(
                endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                channel: 'gang',
                message: "✅ 빌드 성공! 😀 \n프로젝트: *KNOCK-KNOCK BACK*\n브랜치: *develop -> main*\n[빌드 로그 확인](<${env.BUILD_URL}>)"
            )
        }
        failure {
            mattermostSend(
                endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                channel: 'gang',
                message: "❌ 빌드 실패... 🚨 \n프로젝트: *KNOCK-KNOCK BACK*\n브랜치: *develop -> main*\n[빌드 로그 확인](<${env.BUILD_URL}>)"
            )
        }
    }
}