# 최종 실행 이미지: OpenJDK 17 JRE만 포함하여 경량화
FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 시 전달받은 JAR 파일을 이미지에 복사
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# 컨테이너 실행 명령 (필요 시 JVM 옵션 추가 가능)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "app.jar"]
