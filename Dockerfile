# Use an official OpenJDK image as the base image
FROM openjdk:19-jdk-slim

# Set environment variables for Maven installation
ENV MAVEN_VERSION 3.9.2
ENV MAVEN_HOME /opt/maven
ENV PATH ${MAVEN_HOME}/bin:${PATH}

RUN apt-get update && apt-get install -y \
    curl \
    git \
    && curl -fsSL https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar xz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    apt-get clean

# Set the working directory
WORKDIR /app

# Copy the source code into the container
COPY . /app

# Run Maven to build the project
RUN mvn clean package -DskipTests

# Set up PostgreSQL environment variables for connecting to a database
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=userlist_db_cli

# Expose PostgreSQL port and application port
EXPOSE 5432
EXPOSE 8080

# Default command to run the application (can be adjusted if needed)
CMD ["java", "-jar", "target/password-manager-cli-1.0-SNAPSHOT.jar"]
