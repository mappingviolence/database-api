package org.mappingviolence.database_api.server;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

  private Gson gson;

  public JsonTransformer() {
    gson = new Gson();
  }

  public JsonTransformer(ExclusionStrategy... exclusionStragies) {
    gson = new GsonBuilder().setExclusionStrategies(exclusionStragies).create();
  }

  @Override
  public String render(Object model) {
    return gson.toJson(model);
  }

}