#FROM centos
#FROM java:openjdk-8-jdk
#MAINTAINER Kasper Saaby <kdsaaby@gmail.com>

FROM quintenk/supervisor

MAINTAINER Quinten Krijger "https://github.com/Krijger"

RUN apt-get update
RUN apt-get -y install python-software-properties
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update && apt-get -y upgrade

# automatically accept oracle license
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
# and install java 7 oracle jdk
RUN apt-get -y install oracle-java8-installer && apt-get clean
RUN update-alternatives --display java
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

WORKDIR /app
ADD . /app
COPY workspace/ target/universal/stage/workspace/

#ENV TERM dumb

#ENV JAVA_HOME JAVA_ENV_ENV_JAVA_HOME \
#    JAVA_VERSION JAVA_ENV_ENV_JAVA_VERSION \
#    JAVA_DEBIAN_VERSION JAVA_ENV_ENV_JAVA_DEBIAN_VERSION \

#RUN sbt clean compile stage
CMD ["target/universal/stage/bin/overture_webide"]