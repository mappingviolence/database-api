package org.mappingviolence.database_api.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.mappingviolence.database.DatabaseConnection;
import org.mappingviolence.database_api.db.Database;
import org.mappingviolence.database_api.exception.NotFoundException;
import org.mappingviolence.database_api.poi.PublicPOI;
import org.mappingviolence.poi.POI;
import org.mappingviolence.poi.POIVersion;
import org.mappingviolence.poi.POIWikiPage;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Search {
  private static Map<String, Query<POIVersion>> cache = new HashMap<>();

  private static AtomicInteger idCounter = new AtomicInteger(1);

  private static final JsonParser JSON_PARSER = new JsonParser();

  public static Object buildSearch(String body) {
    Map<String, List<String>> searchMap = Search.parseSearchRequestBody(body);
    if (searchMap == null) {
      return new ConstructionError(400, "Poorly formed request. No search query was created.");
    }
    System.out.println(searchMap);

    boolean doTextSearch = false;
    String textSearchString = null;

    Map<String, Pattern> searchPatternMaps = new HashMap<>();
    for (Entry<String, List<String>> e : searchMap.entrySet()) {
      if ("all".equals(e.getKey())) {
        doTextSearch = true;
        StringBuilder textSearch = new StringBuilder();
        for (String s : e.getValue()) {
          textSearch.append(s);
          textSearch.append(" ");
        }
        textSearch.deleteCharAt(textSearch.length() - 1);
        textSearchString = textSearch.toString();
      } else {
        StringBuilder patternStrBuilder = new StringBuilder();
        for (String keyword : e.getValue()) {
          patternStrBuilder.append(keyword);
          patternStrBuilder.append("|");
        }
        patternStrBuilder.deleteCharAt(patternStrBuilder.length() - 1);
        String patternStr = patternStrBuilder.toString();
        System.out.println(patternStr);
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        searchPatternMaps.put(e.getKey(), pattern);
      }
    }
    System.out.println(searchPatternMaps);
    Query<POIVersion> query = DatabaseConnection
        .getDatabase("data-entry-wiki")
        .createQuery(POIVersion.class);

    List<String> publishedPOIIds = Database.publishedPOIIds();
    query = query.filter("_id in", publishedPOIIds);

    for (Entry<String, Pattern> e : searchPatternMaps.entrySet()) {
      switch (e.getKey()) {
        case "date":

          break;
        default:
          query = query.filter("data." + e.getKey() + ".value", e.getValue());
          break;
      }
    }
    if (doTextSearch) {
      System.out.println("String query = " + textSearchString);
      query = query.search(textSearchString);
    }

    String id = Integer.toString(idCounter.getAndIncrement());

    cache.put(id, query);
    System.out.println("done");
    return new ConstructionSuccess(id);
  }

  public static List<PublicPOI> runSearch(String id) {
    Query<POIVersion> query = cache.get(id);
    if (query == null) {
      throw new NotFoundException(
          "There is no resource located here. Please change the requested URL.");
    }

    List<PublicPOI> publicPOIs = new ArrayList<>();

    Datastore ds = DatabaseConnection.getDatabase("data-entry-wiki");

    query.forEach((POIVersion poiV) -> {
      POI poi = poiV.getData();
      PublicPOI publicPOI = new PublicPOI(poi);
      POIWikiPage poiW = ds.find(POIWikiPage.class, "current", poiV).get();
      publicPOI.setId(poiW.getId());
      publicPOIs.add(publicPOI);
    });
    return publicPOIs;
  }

  /**
   * Quick failing -- returns null on any form of invalid data.
   * 
   * @param body
   * @return
   */
  public static Map<String, List<String>> parseSearchRequestBody(String body) {
    try {
      JsonElement bodyJson = JSON_PARSER.parse(body);
      JsonArray searchArr = bodyJson.getAsJsonArray();
      Map<String, List<String>> searchMap = new HashMap<>();
      for (JsonElement searchElem : searchArr) {
        try {
          JsonObject searchObj = searchElem.getAsJsonObject();
          String searchKey = searchObj.get("field").getAsString();
          JsonArray searchValues = searchObj.get("values").getAsJsonArray();
          System.out.println("check " + searchValues.toString());
          List<String> searchValuesList = new ArrayList<>();
          searchValues.forEach((searchValue) -> searchValuesList.add(searchValue.getAsString()));
          searchMap.put(searchKey, searchValuesList);
        } catch (NullPointerException e) {
          e.printStackTrace();
          return null;
        }
      }
      return searchMap;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static class ConstructionSuccess {
    @SuppressWarnings("unused")
    private final boolean success = true;
    private String id;

    public ConstructionSuccess() {

    }

    public ConstructionSuccess(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  public static class ConstructionError {
    @SuppressWarnings("unused")
    private final boolean success = false;
    private ConstructionError.Error error;

    public ConstructionError() {

    }

    public ConstructionError(int code, String message) {
      error = new Error(code, message);
    }

    public String getMessage() {
      return error.message;
    }

    public int getCode() {
      return error.code;
    }

    private static class Error {
      private int code;
      private String message;

      @SuppressWarnings("unused")
      public Error() {
      }

      public Error(int code, String message) {
        this.code = code;
        this.message = message;
      }
    }
  }
}
