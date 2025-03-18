pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo '빌드 실행 중...'
                // 여기에 빌드 명령어 추가
            }
        }
        stage('Test') {
            steps {
                echo '테스트 실행 중...'
                // 여기에 테스트 실행 명령어 추가
            }
        }
    }

    post {
        success {
            script {
                def now = new Date()
                def cal = Calendar.instance
                cal.time = now
                cal.add(Calendar.HOUR_OF_DAY, 9)
                def kstTime = cal.time.format("yyyy-MM-dd HH:mm:ss")

                mattermostSend(
                    endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                    channel: 'gang',
                    message: """
                    ✅ **빌드 성공!** 🎉
                    프로젝트: *knockknock-back*
                    브랜치: develop -> main
                    빌드 번호: #${env.BUILD_NUMBER}
                    빌드 시간: ${kstTime} (KST)
                    [빌드 로그 확인](${env.BUILD_URL})
                    """
                )
            }
        }
        failure {
            script {
                def now = new Date()
                def cal = Calendar.instance
                cal.time = now
                cal.add(Calendar.HOUR_OF_DAY, 9)
                def kstTime = cal.time.format("yyyy-MM-dd HH:mm:ss")

                mattermostSend(
                    endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                    channel: 'gang',
                    message: """
                    ❌ **빌드 실패...** 🚨
                    프로젝트: *knockknock-back*
                    브랜치: develop -> main
                    빌드 번호: #${env.BUILD_NUMBER}
                    빌드 시간: ${kstTime} (KST)
                    [빌드 로그 확인](${env.BUILD_URL})
                    """
                )
            }
        }
    }
}