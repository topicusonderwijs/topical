FROM openjdk:8-jre-alpine

ADD maven/*.jar /topical/

CMD java -Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom -classpath '/topical/*' nl.topicus.topical.TopicalBackend $(cat /run/secrets/domain) $(cat /run/secrets/username) $(cat /run/secrets/password) $(cat /run/secrets/url) $(cat /run/secrets/rooms) 
