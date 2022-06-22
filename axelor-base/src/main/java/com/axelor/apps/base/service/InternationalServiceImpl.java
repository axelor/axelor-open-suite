package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.tool.service.TranslationService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class InternationalServiceImpl implements InternationalService {

  protected TranslationService translationService;

  @Inject
  public InternationalServiceImpl(TranslationService translationService) {
    this.translationService = translationService;
  }

  @Override
  public boolean compareCurrentLanguageWithPartner(Partner partner) throws AxelorException {
    if (partner.getLanguage() == null) {
      throw new AxelorException(
          partner,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PARTNER_LANGUAGE_MISSING),
          partner.getSimpleFullName());
    }

    if (AuthUtils.getUser().getLanguage() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CURRENT_USER_LANGUAGE_MISSING));
    }
    return partner.getLanguage().getCode().equals(AuthUtils.getUser().getLanguage());
  }

  @Override
  public String translate(String source, String sourceLanguage, String targetLanguage) {
    String translationKey = translationService.getTranslationKey(source, sourceLanguage);
    return translationService.getValueTranslation(translationKey, targetLanguage);
  }
}
