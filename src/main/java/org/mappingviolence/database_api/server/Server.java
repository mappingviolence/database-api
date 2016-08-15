package org.mappingviolence.database_api.server;

import org.mappingviolence.database_api.db.Database;

import spark.Spark;

public class Server {

  public Server() {
  }

  public Server(int port) {
    Spark.port(port);
  }

  public void start() {
    Spark.exception(Exception.class, (exception, req, resp) -> {
      return;
    });

    Spark.get("/pois", (req, resp) -> Database.allPOIs(), new JsonTransformer());

    Spark.after((req, resp) -> {
      resp.header("Content-Type", "application/json; charset=utf-8");
    });
  }
}
