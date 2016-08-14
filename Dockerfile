FROM maven:3.3.9-jdk-8

RUN mkdir -p /usr/share/mappingviolence-database-api

WORKDIR /usr/share/mappingviolence-database-api

COPY . .

ENV DB_API_PORT 80

CMD ["sh", "-c", "./build.sh && ./run.sh -p $DB_API_PORT"]
EXPOSE DB_API_PORT
