package org.mappingviolence.database_api.db;

import java.util.ArrayList;
import java.util.Collection;

import org.mappingviolence.database.DatabaseConnection;
import org.mappingviolence.database_api.exception.NotFoundException;
import org.mappingviolence.database_api.poi.PublicPOI;
import org.mappingviolence.poi.POI;
import org.mappingviolence.poi.POIWikiPage;
import org.mappingviolence.wiki.Status;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

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

  public static PublicPOI pois(String id) {
    PublicPOI publicPOI;
    Datastore ds = DatabaseConnection.getDatabase("data-entry-wiki");
    POIWikiPage poiW = ds
        .find(POIWikiPage.class)
        .retrievedFields(true, "_id", "current")
        .filter("_id", id)
        .filter("status", Status.IN_POOL)
        .get();
    if (poiW == null) {
      throw new NotFoundException("");
    }
    POI poi = poiW.getCurrentData();
    publicPOI = new PublicPOI(poi);
    publicPOI.setId(poiW.getId());
    return publicPOI;
  }
}
