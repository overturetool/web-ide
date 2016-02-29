FROM java:8-jre
MAINTAINER Kasper Saaby <kdsaaby@gmail.com>

WORKDIR /app
ADD . /app
COPY workspace/ target/universal/stage/workspace/

#RUN sbt clean compile stage
CMD ["target/universal/stage/bin/overture_webide"]