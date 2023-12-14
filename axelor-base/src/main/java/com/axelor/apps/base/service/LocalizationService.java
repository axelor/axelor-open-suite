package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Localization;

public interface LocalizationService {
  public void setName(String languageName, String countryName, Localization localization);
}
