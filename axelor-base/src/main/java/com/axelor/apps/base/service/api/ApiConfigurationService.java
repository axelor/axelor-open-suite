package com.axelor.apps.base.service.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ApiConfiguration;
import com.axelor.apps.base.db.Partner;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public interface ApiConfigurationService {
  void setData(Partner partner, JSONObject resutlJson) throws JSONException;

  String fetchData(ApiConfiguration apiConfiguration, String siretNumber) throws AxelorException;
}
