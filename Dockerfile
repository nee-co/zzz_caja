FROM java:8-alpine
MAINTAINER Nee-co
ENV SBT_VERSION 0.13.5
RUN apk --update add bash mariadb-dev tzdata && \
    wget http://dl.bintray.com/sbt/native-packages/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.zip && \
    unzip sbt-${SBT_VERSION}.zip && \
    rm -f sbt-${SBT_VERSION}.zip && \
    chmod a+x /sbt/bin/sbt&& \
    cp /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*
ENV PATH $PATH:/sbt/bin
WORKDIR /app
COPY . /app
RUN sbt compile
EXPOSE 9000
CMD ["sbt", "run"]