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
                def branchName = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                mattermostSend(
                    endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                    channel: 'gang',
                    text: """
                    :white_check_mark: **빌드 성공!** :tada:
                    --------------------------------
                    **프로젝트**: knockknock-back
                    **브랜치**: ${branchName}
                    **빌드 번호**: #${env.BUILD_NUMBER}
                    **빌드 시간**: ${new Date().format("yyyy-MM-dd HH:mm:ss")}

                    [빌드 로그 확인하기](${env.BUILD_URL})
                    """
                )
            }
        }
        failure {
            script {
                def branchName = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                mattermostSend(
                    endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                    channel: 'gang',
                    text: """
                    :x: **빌드 실패** :rotating_light:
                    --------------------------------
                    **프로젝트**: knockknock-back
                    **브랜치**: ${branchName}
                    **빌드 번호**: #${env.BUILD_NUMBER}
                    **빌드 시간**: ${new Date().format("yyyy-MM-dd HH:mm:ss")}

                    [빌드 로그 확인하기](${env.BUILD_URL})
                    """
                )
            }
        }
    }
}