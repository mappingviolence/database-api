FROM maven:3.3.9-jdk-8

RUN mkdir -p /usr/share/mappingviolence-database-api

WORKDIR /usr/share/mappingviolence-database-api

COPY . /usr/share/mappingviolence-database-api

RUN mvn install:install-file \
-Dfile=/usr/share/mappingviolence-database-api/lib/mappingviolence-core.jar \
-DgroupId=org.mappingviolence -DartifactId=core \
-Dversion=0.9.1 \
-Dpackaging=jar \
&& ./build.sh

ENV DB_API_PORT 4567

EXPOSE $DB_API_PORT

CMD ["sh", "-c", "./run.sh -p $DB_API_PORT"]
