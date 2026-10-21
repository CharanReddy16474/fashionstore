# Fashion Store — E-commerce Web Application

A fashion e-commerce web application built using Java, Jakarta Servlets, JSP, MySQL, and Apache Tomcat. The application provides a customer-facing shopping experience with product browsing, search, account management, shopping cart, and checkout functionality.

## Tech Stack

- **Backend:** Java 21, Jakarta Servlets
- **Frontend:** JSP, HTML, CSS, JavaScript
- **Database:** MySQL
- **Web Server:** Apache Tomcat 10.1
- **Build Tool:** Apache Maven
- **Deployment:** Docker and Render
- **Cloud Database:** Aiven MySQL

## Features

- Product catalogue and category-based browsing
- Product search and product details
- Product images, pricing, and available variants
- Customer registration and login
- Customer profile and order access
- Shopping cart management
- Checkout and order confirmation
- Responsive shopping interface
- Shared navigation header and Fashion Store branding

## Project Structure

```text
fashionstore/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/fashionstore/
│       │       ├── controller/
│       │       ├── dao/
│       │       ├── model/
│       │       └── util/
│       └── webapp/
│           ├── assets/
│           └── WEB-INF/
├── Dockerfile
├── .dockerignore
├── .gitignore
├── pom.xml
└── README.md
```

## Local Development

1. Install Java 21, Maven, MySQL, and Apache Tomcat 10.1.
2. Create the `fashion_store` database and import the required schema and project data.
3. Configure database connection settings without committing credentials to source control.
4. Build the project:

   ```bash
   mvn clean package
   ```

5. Deploy `target/fashionstore.war` to a compatible Tomcat installation.

## Deployment

The deployment configuration uses Docker and is intended for hosting on Render with a cloud-hosted MySQL database.

**Live Application:** Add the public deployment URL after deployment is complete.

## Security

Database passwords and other secrets must be supplied through environment variables or secure deployment settings. Database backups and customer information must not be committed to this repository.

## Project Purpose

This project demonstrates Java web development, server-side request handling, database integration, product catalogue management, authentication, and e-commerce workflows.
