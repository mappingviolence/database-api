FROM maven:3.3.9-jdk-8

RUN mkdir -p /usr/share/mappingviolence-database-api

WORKDIR /usr/share/mappingviolence-database-api

ENV DB_API_PORT 4567

EXPOSE $DB_API_PORT

CMD ["sh", "-c", "./build.sh && ./run.sh -p $DB_API_PORT"]
