package com.axelor.apps.base.service;

import com.google.common.base.Strings;
import java.util.Locale;

public class LocaleService {
  public static Locale computeLocaleByLocaleCode(String localeCode) {
    if (Strings.isNullOrEmpty(localeCode)) {
      return null;
    }
    String[] parts = localeCode.split("_");
    String languageCode = parts.length > 0 ? parts[0] : "";
    String country = parts.length > 1 ? parts[1] : "";
    return new Locale(languageCode, country);
  }
}
