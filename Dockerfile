FROM adoptopenjdk/openjdk11:alpine-jre
ENV USER_UID=1001 \
    USER_NAME=user-task-management
RUN addgroup --gid ${USER_UID} ${USER_NAME} \
    && adduser --disabled-password --uid ${USER_UID} --ingroup ${USER_NAME} ${USER_NAME}

WORKDIR /app
COPY target/*.jar temp/
RUN rm -f temp/*-stubs.jar \
    && mv temp/*.jar ./app.jar \
    && rm -rf temp/
USER user-task-management
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
