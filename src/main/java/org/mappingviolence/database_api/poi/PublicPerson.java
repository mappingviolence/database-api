package org.mappingviolence.database_api.poi;

import java.util.ArrayList;
import java.util.Collection;

public class PublicPerson {
  private Collection<PublicIdentity<?>> identities;

  public PublicPerson() {
    identities = new ArrayList<>();
  }

  public <T> boolean addIdentity(PublicIdentity<T> newIdentity) {
    return identities.add(newIdentity);
  }
}
