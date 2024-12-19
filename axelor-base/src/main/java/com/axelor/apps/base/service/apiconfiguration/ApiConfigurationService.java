package com.axelor.apps.base.service.apiconfiguration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ApiConfiguration;

public interface ApiConfigurationService {
  String fetchData(ApiConfiguration apiConfiguration, String siretNumber) throws AxelorException;
}
