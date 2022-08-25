package com.axelor.apps.base.service;

import com.axelor.apps.tool.service.TranslationService;
import com.google.inject.Inject;

public class InternationalServiceImpl implements InternationalService {

  protected TranslationService translationService;

  @Inject
  public InternationalServiceImpl(TranslationService translationService) {
    this.translationService = translationService;
  }

  @Override
  public String translate(String source, String sourceLanguage, String targetLanguage) {
    String translationKey = translationService.getTranslationKey(source, sourceLanguage);
    return translationService.getValueTranslation(translationKey, targetLanguage);
  }
}
