# Dockerfile (para cuando hagamos el despliegue)
FROM openjdk:17-jdk-slim

# Instalar dependencias del sistema
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Crear usuario no-root
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Configurar directorio de trabajo
WORKDIR /app

# Copiar el JAR de la aplicación
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

---

# .dockerignore
.git
.gitignore
README.md
target/
!target/*.jar
src/test/
docs/
*.md
.env