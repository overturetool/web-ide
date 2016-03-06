FROM java:openjdk-8-jdk
MAINTAINER Kasper Saaby <kdsaaby@gmail.com>

ENV SBT_VERSION 0.13.11
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin
ENV TERM cygwin

# Install sbt
RUN curl -sL "http://dl.bintray.com/sbt/native-packages/sbt/$SBT_VERSION/sbt-$SBT_VERSION.tgz" | gunzip | tar -x -C /usr/local && \
    echo -ne "- with sbt $SBT_VERSION\n" >> /root/.built

RUN apt-get update
RUN apt-get install -y awscli

WORKDIR /app

ADD . /app

#RUN sbt clean compile stage
#RUN cp -R workspace/OvertureExamples target/universal/stage/workspace/

CMD ["target/universal/stage/bin/overture_webide"]