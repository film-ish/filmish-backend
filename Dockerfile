# 최종 실행 이미지: Eclipse Temurin 17 JRE만 포함하여 경량화 (Alpine 기반)
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 로그 디렉토리 생성
RUN mkdir -p /var/log/springboot && \
    chmod -R 755 /var/log/springboot

# 빌드 시 전달받은 JAR 파일을 이미지에 복사
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# 컨테이너 실행 명령 (Spring 프로필 설정 및 타임존 설정)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "app.jar"]