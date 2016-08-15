package org.mappingviolence.database_api.poi;

import java.util.ArrayList;
import java.util.Collection;

import org.mappingviolence.poi.POI;
import org.mappingviolence.poi.date.Date;
import org.mappingviolence.poi.identity.Identity;
import org.mongodb.morphia.geo.Point;

public class PublicPOI {
  private String id;

  private String title;

  private String description;

  private Date date;

  private Point location;

  private String locationRationale;

  private Collection<PublicPerson> victims;

  private Collection<PublicPerson> aggressors;

  private Collection<String> tags;

  private Collection<String> primarySources;

  private Collection<String> secondarySources;

  public PublicPOI(POI poi) {
    this.title = poi.getTitle().getValue();
    this.description = poi.getDescription().getValue();
    this.date = poi.getDate().getValue();
    this.location = poi.getLocation().getValue();
    this.locationRationale = poi.getLocationRationale().getValue();

    this.victims = new ArrayList<>();
    poi.getVictims().forEach((victim) -> {
      PublicPerson publicPerson = new PublicPerson();
      for (Identity<?> i : victim.getIdentities()) {
        publicPerson.addIdentity(new PublicIdentity<Object>(i.getCategory(), i.getValue()));
      }
      this.victims.add(publicPerson);
    });

    this.aggressors = new ArrayList<>();
    poi.getAggressors().forEach((aggressor) -> {
      PublicPerson publicPerson = new PublicPerson();
      for (Identity<?> i : aggressor.getIdentities()) {
        publicPerson.addIdentity(new PublicIdentity<Object>(i.getCategory(), i.getValue()));
      }
      this.aggressors.add(publicPerson);
    });

    this.tags = new ArrayList<>();
    poi.getTags().forEach((tag) -> this.tags.add(tag.getValue()));
    this.primarySources = new ArrayList<>();
    poi.getPrimarySources().forEach(
        (primarySource) -> this.primarySources.add(primarySource.getValue()));
    this.secondarySources = new ArrayList<>();
    poi.getSecondarySources().forEach(
        (secondarySource) -> this.secondarySources.add(secondarySource.getValue()));
  }

  public String getTitle() {
    return title;
  }

  public void setId(String id) {
    this.id = id;
  }

}
