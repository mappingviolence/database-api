package org.mappingviolence.database_api.poi;

public class PublicIdentity<T> {
  private String category;
  private T value;

  @SuppressWarnings("unused")
  private PublicIdentity() {

  }

  public PublicIdentity(String category, T value) {
    this.category = category;
    this.value = value;
  }
}
