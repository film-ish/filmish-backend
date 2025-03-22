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
                echo '백엔드 빌드 및 테스트 실행 중...'
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
                echo '백엔드 배포 실행 중...'
                sh '''
                    # 배포 대상 디렉토리 존재 여부 확인 후 생성
                    mkdir -p /home/ubuntu/knockknock/backend

                    # 백엔드 .env 파일 생성
                    cat > /home/ubuntu/knockknock/backend/backend.env << EOL
DB_URL=${DB_URL}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
EOL

                    # 배포 스크립트 실행
                    cd /home/ubuntu/knockknock/backend && ./scripts/deploy.sh
                '''
            }
        }
    }

    post {
        success {
            script {
                // 간결한 브랜치 이름 얻기
                def fullBranch = sh(script: 'git name-rev --name-only HEAD', returnStdout: true).trim()
                def gitBranch = fullBranch.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '')

                def gitCommit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                def commitMsg = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                def commitAuthor = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()

                // 기본 트리거 유형 및 브랜치 정보
                def triggerType = "Push"
                def sourceBranch = ""
                def targetBranch = gitBranch
                def branchInfo = "**브랜치:** ${gitBranch}"

                // Pull Request/Merge Request 감지 및 정보 추출
                if (env.CHANGE_ID) {
                    triggerType = "Merge Request"
                    sourceBranch = env.CHANGE_BRANCH?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                    targetBranch = env.CHANGE_TARGET?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                    branchInfo = "**브랜치:** ${sourceBranch} → ${targetBranch}"
                } else if (env.BRANCH_NAME && env.CHANGE_URL) {
                    // GitHub Pull Request Builder 플러그인
                    triggerType = "Pull Request"
                    sourceBranch = env.BRANCH_NAME.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '')
                    targetBranch = env.ghprbTargetBranch?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                    branchInfo = "**브랜치:** ${sourceBranch} → ${targetBranch}"
                } else if (currentBuild.getBuildCauses().toString().contains('gitlab')) {
                    // GitLab 플러그인
                    if (currentBuild.getBuildCauses().toString().contains('MergeRequest')) {
                        triggerType = "Merge Request"
                        sourceBranch = env.gitlabSourceBranch?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                        targetBranch = env.gitlabTargetBranch?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                        branchInfo = "**브랜치:** ${sourceBranch} → ${targetBranch}"
                    }
                }

                mattermostSend(
                    endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                    channel: 'gang',
                    icon: ':jenkins:',
                    color: '#36a64f',
                    text: ":white_check_mark: **백엔드 빌드 성공!** :tada:\n" +
                          "--------------------------------------------------\n" +
                          ":mag: [빌드 로그 확인](${env.BUILD_URL})\n" +
                          ":chart_with_upwards_trend: [트렌드 보기](${env.BUILD_URL}trend)\n" +
                          ":page_with_curl: [소스 코드 변경사항](${env.BUILD_URL}changes)",
                    message: "**프로젝트:** KNOCK-KNOCK BACKEND\n" +
                             "**트리거 유형:** ${triggerType}\n" +
                             "${branchInfo}\n" +
                             "**커밋:** ${gitCommit} - ${commitMsg}\n" +
                             "**작성자:** ${commitAuthor}\n" +
                             "**빌드 번호:** #${env.BUILD_NUMBER}\n" +
                             "**빌드 시간:** ${new Date().format('yyyy-MM-dd HH:mm:ss')}"
                )
            }
        }
        failure {
            script {
                // 간결한 브랜치 이름 얻기
                def fullBranch = sh(script: 'git name-rev --name-only HEAD', returnStdout: true).trim()
                def gitBranch = fullBranch.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '')

                def gitCommit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                def commitMsg = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                def commitAuthor = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()

                // 기본 트리거 유형 및 브랜치 정보
                def triggerType = "Push"
                def sourceBranch = ""
                def targetBranch = gitBranch
                def branchInfo = "**브랜치:** ${gitBranch}"

                // Pull Request/Merge Request 감지 및 정보 추출
                if (env.CHANGE_ID) {
                    triggerType = "Merge Request"
                    sourceBranch = env.CHANGE_BRANCH?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                    targetBranch = env.CHANGE_TARGET?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                    branchInfo = "**브랜치:** ${sourceBranch} → ${targetBranch}"
                } else if (env.BRANCH_NAME && env.CHANGE_URL) {
                    // GitHub Pull Request Builder 플러그인
                    triggerType = "Pull Request"
                    sourceBranch = env.BRANCH_NAME.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '')
                    targetBranch = env.ghprbTargetBranch?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                    branchInfo = "**브랜치:** ${sourceBranch} → ${targetBranch}"
                } else if (currentBuild.getBuildCauses().toString().contains('gitlab')) {
                    // GitLab 플러그인
                    if (currentBuild.getBuildCauses().toString().contains('MergeRequest')) {
                        triggerType = "Merge Request"
                        sourceBranch = env.gitlabSourceBranch?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                        targetBranch = env.gitlabTargetBranch?.replaceAll('remotes/origin/', '').replaceAll('refs/heads/', '') ?: "unknown"
                        branchInfo = "**브랜치:** ${sourceBranch} → ${targetBranch}"
                    }
                }

                mattermostSend(
                    endpoint: 'https://meeting.ssafy.com/hooks/wuqodhw37jnejccnc1bsjso7pc',
                    channel: 'gang',
                    icon: ':jenkins:',
                    color: '#e01e5a',
                    text: ":x: **백엔드 빌드 실패!** :warning:\n" +
                          "--------------------------------------------------\n" +
                          ":mag: [빌드 로그 확인](${env.BUILD_URL})\n" +
                          ":bug: [콘솔 출력 확인](${env.BUILD_URL}console)\n" +
                          ":wrench: [테스트 결과](${env.BUILD_URL}testReport)",
                    message: "**프로젝트:** KNOCK-KNOCK BACKEND\n" +
                             "**트리거 유형:** ${triggerType}\n" +
                             "${branchInfo}\n" +
                             "**커밋:** ${gitCommit} - ${commitMsg}\n" +
                             "**작성자:** ${commitAuthor}\n" +
                             "**빌드 번호:** #${env.BUILD_NUMBER}\n" +
                             "**빌드 시간:** ${new Date().format('yyyy-MM-dd HH:mm:ss')}"
                )
            }
        }
    }
}