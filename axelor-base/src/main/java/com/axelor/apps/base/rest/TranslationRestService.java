package com.axelor.apps.base.rest;

import com.axelor.exception.AxelorException;
import java.util.Map;

public interface TranslationRestService {

  Integer createNewTranslation(Map<String, String> translationMap, String language)
      throws AxelorException;
}
