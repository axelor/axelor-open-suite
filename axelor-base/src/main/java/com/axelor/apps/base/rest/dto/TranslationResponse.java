package com.axelor.apps.base.rest.dto;

import com.axelor.meta.db.MetaTranslation;

public class TranslationResponse {

  private String key;
  private String value;
  private Number version;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Number getVersion() {
    return version;
  }

  public void setVersion(Number version) {
    this.version = version;
  }

  public static TranslationResponse build(MetaTranslation metaTranslation) {
    TranslationResponse translation = new TranslationResponse();
    translation.setKey(metaTranslation.getKey().replace("mobile_app_", ""));
    translation.setValue(metaTranslation.getMessage());
    translation.setVersion(metaTranslation.getVersion());
    return translation;
  }
}
