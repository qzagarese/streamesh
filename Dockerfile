FROM maven:3.6.3-jdk-11 as maven
WORKDIR /usr
COPY streamesh-core ./streamesh-core
COPY streamesh-docker-driver ./streamesh-docker-driver
COPY streamesh-server ./streamesh-server

WORKDIR /usr/streamesh-core
RUN  mvn clean install

WORKDIR /usr/streamesh-docker-driver
RUN  mvn clean install

WORKDIR /usr/streamesh-server
RUN  mvn clean package


FROM adoptopenjdk/openjdk11:jre-11.0.7_10-alpine as streamesh-server
COPY --from=maven /usr/streamesh-server/target/streamesh-server-1.0-SNAPSHOT.jar /usr/bin
EXPOSE 8080
ENV JAVA_OPTS ""
CMD java $JAVA_OPTS -jar /usr/bin/streamesh-server-1.0-SNAPSHOT.jar

FROM node:12.16.2-stretch as npm
WORKDIR /usr
COPY streamesh-web-ui ./streamesh-web-ui
WORKDIR /usr/streamesh-web-ui
RUN npm install -g @vue/cli
RUN npm install
RUN npm run build

FROM nginx:stable as streamesh-web-ui
COPY --from=npm /usr/streamesh-web-ui/dist/ /usr/share/nginx/html/
COPY nginx/streamesh.conf /etc/nginx/conf.d/default.conf


