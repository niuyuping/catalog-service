FROM bellsoft/liberica-openjre-alpine:21 AS builder
WORKDIR /workspace
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} catalog-service.jar
RUN java -Djarmode=layertools -jar catalog-service.jar extract

FROM bellsoft/liberica-openjre-alpine:21
RUN adduser -D appuser
USER appuser
WORKDIR /workspace
COPY --from=builder /workspace/dependencies/ ./
COPY --from=builder /workspace/spring-boot-loader/ ./
COPY --from=builder /workspace/snapshot-dependencies/ ./
COPY --from=builder /workspace/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]