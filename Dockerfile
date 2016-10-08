FROM java:8-alpine
ENV SBT_VERSION 0.13.5
RUN apk --no-cache --update add bash mariadb-dev tzdata && \
    wget http://dl.bintray.com/sbt/native-packages/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.zip && \
    unzip sbt-${SBT_VERSION}.zip && \
    rm -f sbt-${SBT_VERSION}.zip && \
    chmod a+x /sbt/bin/sbt&& \
    cp /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    apk del tzdata
ENV PATH $PATH:/sbt/bin
WORKDIR /app
COPY . /app
RUN sbt clean compile stage
CMD ["target/universal/stage/bin/caja", "-Dhttp.port=${CAJA_PORT}"]
ARG REVISION
LABEL revision=$REVISION maintainer="Nee-co"
