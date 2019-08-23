FROM openjdk:8-alpine

COPY target/uberjar/size-measure.jar /size-measure/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/size-measure/app.jar"]
