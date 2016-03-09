FROM 1science/sbt

RUN apk-install python py-pip \
    && pip install --upgrade awscli \
    && apk del py-pip \
    && apk del py-setuptools \
    && rm -rf /var/cache/apk/* \
    && rm -rf /tmp/*

WORKDIR /app

ADD . /app

#RUN sbt clean compile stage
RUN mkdir -p /app/target/universal/stage/workspace/OvertureExamples
RUN cp -R /app/workspace/OvertureExamples/VDMSL /app/target/universal/stage/workspace/OvertureExamples

CMD ["target/universal/stage/bin/overture_webide"]