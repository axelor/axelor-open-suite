package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;

public interface InternationalService {
  boolean compareCurrentLanguageWithPartner(Partner partner) throws AxelorException;

  String translate(String source, String sourceLanguage, String targetLanguage);
}
