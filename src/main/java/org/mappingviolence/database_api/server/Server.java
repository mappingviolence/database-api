package org.mappingviolence.database_api.server;

import org.mappingviolence.database_api.db.Database;
import org.mappingviolence.database_api.exception.NotFoundException;

import spark.Spark;

public class Server {

  public Server() {
  }

  public Server(int port) {
    Spark.port(port);
  }

  public void start() {
    Spark.exception(NotFoundException.class, (exception, req, resp) -> {
      resp.status(404);
      resp.body(exception.getMessage());
    });
    Spark.exception(Exception.class, (exception, req, resp) -> {
      resp.status(500);
      resp.body(
          "{" + "\"success\" : false," + "\"error\" : {" + "\"code\" : 500,"
              + "\"message\" : \"There was an error in retrieving the pois. Please try again.\""
              + "}" + "}");
    });

    Spark.get("/pois", (req, resp) -> Database.pois(), new JsonTransformer());
    Spark.get("/pois/:id", (req, resp) -> Database.pois(req.params(":id")), new JsonTransformer());

    Spark.get(
        "/*",
        (req, resp) -> "{" + "\"success\" : false," + "\"error\" : {" + "\"code\" : 404,"
            + "\"message\" : \"There is no resource located here. Please change the requested URL.\""
            + "}" + "}");
    Spark.after((req, resp) -> {
      resp.header("Content-Type", "application/json; charset=utf-8");
    });
  }
}
