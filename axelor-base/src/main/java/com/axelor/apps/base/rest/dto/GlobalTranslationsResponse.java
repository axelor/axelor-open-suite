package com.axelor.apps.base.rest.dto;

import java.util.List;

public class GlobalTranslationsResponse {

  private final List<TranslationResponse> translations;

  public GlobalTranslationsResponse(List<TranslationResponse> translations) {
    this.translations = translations;
  }

  public List<TranslationResponse> getTranslation() {
    return translations;
  }
}
