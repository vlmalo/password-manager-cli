# Password Manager CLI

This project is a simple command-line password manager written in Java. It securely stores and retrieves passwords, and uses a PostgreSQL database for storage. The project includes a database setup with Docker, Maven for building the project, and a shaded JAR for execution.

---

## Key Features

- **Secure Password Storage**: Passwords are stored securely in a PostgreSQL database.
- **Command-Line Interface (CLI)**: Lightweight and user-friendly terminal-based interaction.
- **Shaded JAR**: Self-contained executable JAR for portability.
- **Cross-Platform**: Runs on any system with Java 19 or later.
- **Maven Build**: Automated dependency management and build process.
- **Logging Frameworks**: 
  - **Logback** and **Log4j** are integrated for robust logging to track system activity and errors.
- **Prepared Statements**: Ensures secure and efficient database queries, preventing SQL injection attacks.
- **Brute Force Defense**: Implements measures to detect and mitigate brute force attacks.

---

## Application Features

- **Add Passwords**: Store website credentials (website, username, password) securely.
- **Retrieve Passwords**: Quickly fetch stored passwords using the website name.
- **Delete Passwords**: Remove credentials that are no longer needed.
- **Database Management**:
  - Automatically connects to a PostgreSQL database using Docker.
  - Configurable database credentials.
- **Logging**:
  - Tracks application events using Logback and Log4j, ensuring maintainability and monitoring. Log rotation is also implemented.
- **Security Enhancements**:
  - Prepared statements protect against SQL injection.
  - Brute force defense mechanisms safeguard against repeated unauthorized access attempts.
- **Error Handling**: Graceful error messages for invalid commands or database issues.

---

## Prerequisites

Before setting up this project, ensure that you have the following installed:

- **Java 19** or higher: The project uses Java 19 for compilation.
- **Maven**: This is used for building the project.
- **Docker**: Used to run the PostgreSQL database in a container.
- **Git**: To clone the project repository.

### Installation Steps

1. **Clone the repository**:

   First, clone the repository to your local machine:

    ```bash
    git clone https://github.com/vlmalo/password-manager-cli.git
    cd password-manager-cli
    ```

2. **Install Java 19**:

   If you don't already have Java 19, install it by following the instructions for your operating system:

    - For **Ubuntu**:

      ```bash
      sudo apt update
      sudo apt install openjdk-19-jdk
      ```

    - For **macOS** (using Homebrew):

      ```bash
      brew install openjdk@19
      ```

   After installation, verify Java is installed:

    ```bash
    java -version
    ```

3. **Install Maven**:

   If Maven is not installed, you can install it using the following commands:

    - For **Ubuntu**:

      ```bash
      sudo apt update
      sudo apt install maven
      ```

    - For **macOS** (using Homebrew):

      ```bash
      brew install maven
      ```

   Verify Maven is installed:

    ```bash
    mvn -version
    ```

4. **Install Docker**:

    - For **Ubuntu**:

      ```bash
      sudo apt update
      sudo apt install docker.io
      ```

    - For **macOS** and **Windows**, download and install Docker from [Docker's official site](https://www.docker.com/products/docker-desktop).

   After installation, verify Docker is running:

    ```bash
    docker --version
    ```

### Setting up the Database with Docker

The project uses a PostgreSQL database to store user credentials. You can run the database using Docker.

1. **Create a Docker network** (optional but recommended to manage container connections):

    ```bash
    docker network create password-manager-network
    ```

2. **Run the PostgreSQL Docker container**:

   Use the following command to create a PostgreSQL container with a custom password and database name:

    ```bash
    sudo docker run --name postgres-cli-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=userlist_db_cli -p 5432:5432 -d postgres:latest
    ```


-   You can check if the container is running by using:

    ```bash
    sudo docker ps
    ```


### Building the Project

1. **Build the project using Maven**:

   Once your environment is set up, run the following Maven command to clean and package the application:

    ```bash
    mvn clean package
    ```

   This will create a JAR file under the `target/` directory, including all necessary dependencies in a "fat JAR".

### Running the Application

1. **Run the application**:

   You can run the application using the generated JAR file. The `maven-shade-plugin` was used to package the application into a single executable JAR.

   Run the application with the following command:

    ```bash
    java -jar target/password-manager-cli-1.0-SNAPSHOT.jar
    ```

   This will execute the password manager CLI. You will be able to interact with the application through the terminal.

### Usage

The application allows you to store, retrieve, and delete passwords in a PostgreSQL database. The following commands are available:

- `add <website> <username> <password>`: Add a new password entry.
- `get <website>`: Retrieve the password for a specific website.
- `delete <website>`: Delete a password entry.

### Stopping the Docker Container

When you're done, you can stop the Docker container with:

```bash
docker stop postgres-cli-db
