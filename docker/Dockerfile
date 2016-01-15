FROM anapsix/docker-oracle-java8 
MAINTAINER Jeroen Idserda <jeroen.idserda@topicus.nl>

RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get install -y git maven && \
    apt-get autoremove  && \
    apt-get clean  && \
    rm -rf /var/lib/apt/lists/*

ADD start.sh /tmp/

RUN chmod +x /tmp/start.sh

CMD ./tmp/start.sh
