package org.mappingviolence.database_api.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mappingviolence.database.DatabaseConnection;
import org.mappingviolence.database_api.exception.NotFoundException;
import org.mappingviolence.database_api.poi.PublicPOI;
import org.mappingviolence.poi.POI;
import org.mappingviolence.poi.POIVersion;
import org.mappingviolence.poi.POIWikiPage;
import org.mappingviolence.wiki.Status;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// TODO: Change IN_POOL to PUBLISHED
public class Database {
  public static Collection<PublicPOI> pois() {
    Collection<PublicPOI> publicPOIs = new ArrayList<>();
    Datastore ds = DatabaseConnection.getDatabase("data-entry-wiki");
    Query<POIWikiPage> query = ds
        .find(POIWikiPage.class)
        .retrievedFields(true, "_id", "current")
        .filter("status =", Status.IN_POOL);
    query.forEach((POIWikiPage poiW) -> {
      POI poi = poiW.getCurrentData();
      PublicPOI publicPOI = new PublicPOI(poi);
      publicPOI.setId(poiW.getId());
      publicPOIs.add(publicPOI);
    });
    return publicPOIs;
  }

  public static PublicPOI pois(String id, String fields) {
    // cleanly handle if spark.queryParam(string) returns null
    if (fields == null) {
      fields = "";
    }
    fields = fields.concat(",");

    // split the requested field csv into an array
    String[] userRequestedFields = fields.split(",");
    // convert array to (immutable) list
    List<String> userRequestedFieldsList = Arrays.asList(userRequestedFields);

    // create a mutable list for the projection for mongo
    List<String> mongoRequestedFieldNamesList = new ArrayList<>();
    // create a mutable list for the gson serialization
    List<String> requestedFieldNamesList = new ArrayList<>();
    // if the user requested fields
    if (userRequestedFieldsList.size() > 0) {
      userRequestedFieldsList.forEach((requestedField) -> {
        // add them to the mongo list, appending data. b/c they are nested
        mongoRequestedFieldNamesList.add("data.".concat(requestedField));
        // add them to the gson list
        requestedFieldNamesList.add(requestedField);
      });
      // user didn't request any fields
    } else {
      // add data (i.e. all fields)
      mongoRequestedFieldNamesList.add("data");
      // add all fields to gson
      for (Field poiField : POI.class.getDeclaredFields()) {
        requestedFieldNamesList.add(poiField.getName());
      }
    }

    // convert list to array
    String[] mongoRequestedFields = new String[mongoRequestedFieldNamesList.size()];
    mongoRequestedFields = mongoRequestedFieldNamesList.toArray(mongoRequestedFields);

    // get poi wiki based on id
    PublicPOI publicPOI;
    Datastore ds = DatabaseConnection.getDatabase("data-entry-wiki");
    POIWikiPage poiW = ds
        .find(POIWikiPage.class)
        .retrievedFields(true, "current")
        .filter("_id", id)
        // make sure that the requested POI is published
        .filter("status", Status.IN_POOL)
        .get();
    if (poiW == null) {
      // TODO: Provide json error
      throw new NotFoundException("not found");
    }

    // get current version id
    String poiId = poiW.getCurrentVersion().getId();
    // get current version based on id
    POIVersion poiV = ds
        .createQuery(POIVersion.class)
        .retrievedFields(true, mongoRequestedFields)
        .filter("_id", poiId)
        .get();
    // get current poi data
    POI poi = poiV.getData();
    // convert to public poi
    publicPOI = new PublicPOI(poi);
    // set the id from the wiki page
    publicPOI.setId(poiW.getId());

    // all fields in PublicPOI
    Field[] publicPOIfields = PublicPOI.class.getDeclaredFields();
    // convert array to list
    List<Field> publicPOIfieldsList = Arrays.asList(publicPOIfields);
    // create mutable list specifying which fields to exclude from gson
    // this is needed because POI class instantiates list data as empty lists in
    // constructor
    // that means that they show up, which is not what is wanted
    List<String> excludedFieldNamesList = new ArrayList<>();

    // add all PublicPOI fields to excluded field names list
    publicPOIfieldsList.forEach((field) -> excludedFieldNamesList.add(field.getName()));
    // remove the requested fields
    requestedFieldNamesList.forEach((fieldName) -> excludedFieldNamesList.remove(fieldName));
    // need to send id on all requests
    excludedFieldNamesList.remove("id");

    // create exclusion strategy
    ExclusionStrategy exclude = new ExclusionStrategy() {

      @Override
      public boolean shouldSkipField(FieldAttributes f) {
        return excludedFieldNamesList.contains(f.getName());
      }

      @Override
      public boolean shouldSkipClass(Class<?> clazz) {
        return false;
      }
    };

    // build gson with exclusion strategy
    Gson gson = new GsonBuilder().setExclusionStrategies(exclude).create();
    // convert to json
    // can't send this because it needs to be a PublicPOI not string
    String jsonPublicPOI = gson.toJson(publicPOI);
    // convert back to PublicPOI without the extraneous data
    PublicPOI filteredPublicPOI = gson.fromJson(jsonPublicPOI, PublicPOI.class);
    return filteredPublicPOI;
  }
}
