package com.axelor.apps.base.rest;

import com.axelor.exception.AxelorException;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public interface TranslationRestService {

  Integer createNewTranslation(JSONObject translationFile, String language)
      throws AxelorException, JSONException;
}
