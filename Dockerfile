FROM 1science/sbt:latest
MAINTAINER Kasper Saaby <kdsaaby@gmail.com>

ADD . /app

RUN sbt clean compile stage

CMD ["target/universal/stage/bin/overture_webide"]