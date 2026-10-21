
# Stage 1: Build the Fashion Store application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package


# Stage 2: Run Fashion Store on Tomcat 10.1 with Java 21
FROM tomcat:10.1.61-jre21-temurin

# Use the port required by Render
RUN sed -i \
    -e 's/port="8080"/port="10000"/' \
    -e 's/port="8005"/port="-1"/' \
    /usr/local/tomcat/conf/server.xml

# Remove Tomcat's default web applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy Fashion Store under the /fashionstore context path
COPY --from=build /build/target/fashionstore.war \
    /usr/local/tomcat/webapps/fashionstore.war

# Redirect the domain root to the Fashion Store home page
RUN mkdir -p /usr/local/tomcat/webapps/ROOT \
    && printf '%s\n' \
    '<% response.sendRedirect("/fashionstore/home"); %>' \
    > /usr/local/tomcat/webapps/ROOT/index.jsp

EXPOSE 10000

CMD ["catalina.sh", "run"]
