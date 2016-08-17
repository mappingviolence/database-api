package org.mappingviolence.database_api.server;

import org.mappingviolence.database_api.db.Database;
import org.mappingviolence.database_api.exception.NotFoundException;
import org.mappingviolence.database_api.search.Search;
import org.mongodb.morphia.query.ValidationException;

import spark.Spark;

public class Server {

  private static final String NOT_FOUND_ERROR = "{" + "\"success\" : false," + "\"error\" : {"
      + "\"code\" : 404,"
      + "\"message\" : \"There is no resource located here. Please change the requested URL.\""
      + "}" + "}";

  private static final String SERVER_ERROR = "{" + "\"success\" : false," + "\"error\" : {"
      + "\"code\" : 500,"
      + "\"message\" : \"There was an error on our side in processing your request. Please try again.\""
      + "}" + "}";

  public Server() {
  }

  public Server(int port) {
    Spark.port(port);
  }

  public void start() {
    Spark.exception(ValidationException.class, (exception, req, resp) -> {
      resp.status(400);
      // TODO: JSONify
      resp.body(
          "One of the fields you requested is not a valid field.\n"
              + "The invalid field is most likely: " + exception.getMessage().split(" ")[2]);
    });
    Spark.exception(NotFoundException.class, (exception, req, resp) -> {
      resp.status(404);
      resp.body(exception.getMessage());
    });
    Spark.exception(Exception.class, (exception, req, resp) -> {
      resp.status(500);
      // TODO: Change to SERVER_ERROR before production
      resp.body(exception.getClass().getName());
    });

    Spark.get("/pois", (req, resp) -> Database.pois(), new JsonTransformer());
    Spark.get(
        "/pois/:id",
        (req, resp) -> Database.pois(req.params(":id"), req.queryParams("f")),
        new JsonTransformer());
    Spark.post(
        "/searches",
        "application/json",
        (req, resp) -> Search.buildSearch(req.body()),
        new JsonTransformer());
    Spark.get(
        "/searches/:id",
        (req, resp) -> Search.runSearch(req.params(":id")),
        new JsonTransformer());

    // because Spark :(
    Spark.get("/pois/", (req, resp) -> Database.pois(), new JsonTransformer());
    Spark.get(
        "/pois/:id/",
        (req, resp) -> Database.pois(req.params(":id"), req.queryParams("f")),
        new JsonTransformer());
    Spark.post(
        "/searches/",
        "application/json",
        (req, resp) -> Search.buildSearch(req.body()),
        new JsonTransformer());
    Spark.get(
        "/searches/:id/",
        (req, resp) -> Search.runSearch(req.params(":id")),
        new JsonTransformer());

    Spark.get("/*", (req, resp) -> NOT_FOUND_ERROR);

    Spark.options("/*", (request, response) -> {

      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

    Spark.after((req, resp) -> {
      resp.header("Content-Type", "application/json; charset=utf-8");
    });
  }
}
