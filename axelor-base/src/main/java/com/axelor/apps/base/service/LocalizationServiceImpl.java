package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import java.util.Locale;
import org.apache.commons.lang3.LocaleUtils;

public class LocalizationServiceImpl implements LocalizationService {
  @Override
  public void validateLocale(Localization localization) throws AxelorException {
    String localeStr = localization.getCode();
    if (StringUtils.isEmpty(localeStr)) {
      return;
    }
    String languageTag = localeStr.replace("_", "-");

    if (LocaleUtils.availableLocaleList().stream()
        .map(Locale::toLanguageTag)
        .noneMatch(languageTag::equals)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.COMPANY_INVALID_LOCALE),
          localeStr);
    }
  }
}
