cd /tmp
rm -rf topical-backend; true
git clone https://github.com/niesink/topical-backend.git
cd topical-backend
mvn compile
mvn exec:java -Dexec.mainClass="nl.topicus.topical.TopicalBackend" -Dexec.args="[domain] [username] [password] [url] [room1] [room2]"
