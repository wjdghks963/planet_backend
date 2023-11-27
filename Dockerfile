# 기본 이미지 설정
FROM openjdk:17-oracle

# Gradle 빌드 결과 복사
COPY build/libs/*.jar app.jar

# 포트 설정
EXPOSE 8080

# 앱 실행
ENTRYPOINT ["java","-jar","/app.jar"]
