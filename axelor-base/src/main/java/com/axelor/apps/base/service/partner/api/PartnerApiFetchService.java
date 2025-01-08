package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PartnerApiConfiguration;

public interface PartnerApiFetchService {
  String fetch(PartnerApiConfiguration partnerApiConfiguration, String siretNumber)
      throws AxelorException;
}
