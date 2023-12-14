package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.repo.LocalizationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LocalizationServiceImpl implements LocalizationService {
  protected LocalizationRepository localizationRepository;

  @Inject
  public LocalizationServiceImpl(LocalizationRepository localizationRepository) {
    this.localizationRepository = localizationRepository;
  }

  @Transactional
  @Override
  public void setName(String languageName, String countryName, Localization localization) {
    localization.setName(
        localization.getLanguage().getName() + " (" + localization.getCountry().getName() + ")");
    localizationRepository.save(localization);
  }
}
