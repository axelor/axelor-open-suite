package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PartnerApiConfiguration;

public interface PartnerApiConfigurationService {
  String fetchData(PartnerApiConfiguration partnerApiConfiguration, String siretNumber)
      throws AxelorException;
}
