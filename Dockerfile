FROM maven:3.6.0-jdk-11-slim AS build
COPY src /build/sync-certificates-vault/src
COPY pom.xml /build/sync-certificates-vault
RUN mvn -f /build/sync-certificates-vault/pom.xml clean package
RUN ls /build/sync-certificates-vault/target/

FROM openjdk:11-jre-slim
COPY --from=build /build/sync-certificates-vault/target/dependency-jars /opt/sync-certificates-vault/dependency-jars
COPY --from=build /build/sync-certificates-vault/target/sync-certificates-vault.jar /opt/sync-certificates-vault/sync-certificates-vault.jar 
WORKDIR /opt/sync-certificates-vault
COPY scripts .
CMD ["/opt/sync-certificates-vault/execute.sh"]