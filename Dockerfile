# docker run -d -t -p 9000:9000 --name overture_api overture/api
#CMD ["activator", "run", "-Xms128M", "-Xmx256M", "-XX:MaxPermSize=128M", "XX:PermSize=128M"]

FROM ingensi/play-framework:latest
MAINTAINER Kasper Saaby <kdsaaby@gmail.com>

ADD . /app

#RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo
#RUN yum install -y sbt

RUN yum install -y attr
RUN setfattr -n user.pax.flags -v "mr" /usr/bin/java
# Install sbt
#ENV SBT_VERSION 0.13.9

#RUN yum clean all -y
#RUN yum update -y
#RUN yum search alien
#RUN yum makecache fast
#RUN yum install -y yum-cron
#RUN yum install -y wget
#RUN yum install -y aptitude
#RUN aptitude install -y alien
#RUN yum install -y dpkg
#RUN yum install -y alien

#RUN wget http://ftp.de.debian.org/debian/pool/main/a/alien/alien_8.88.tar.gz


#RUN wget https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb
#RUN alien -r sbt-$SBT_VERSION.deb

#RUN rpm -ivh sbt-$SBT_VERSION.rpm

#RUN \
#    curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
#    dpkg -i sbt-$SBT_VERSION.deb && \
#    rm sbt-$SBT_VERSION.deb && \
#    apt-get update && \
#    apt-get install sbt