FROM openjdk:8-jre-alpine

ADD maven/*.jar /topical/

CMD ["java", "-Djava.awt.headless=true", "-Djava.security.egd=file:/dev/./urandom", "-classpath", "/topical/*", "nl.topicus.topical.TopicalBackend", "$(</run/secrets/domain)", "$(</run/secrets/username)", "$(</run/secrets/password)", "$(</run/secrets/url)", "$(</run/secrets/rooms)"] 
