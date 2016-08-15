package org.mappingviolence.database_api.server;

import com.google.gson.Gson;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

  private static final Gson GSON = new Gson();

  @Override
  public String render(Object model) {
    return GSON.toJson(model);
  }

}