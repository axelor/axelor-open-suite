package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerApiConfiguration;

public interface PartnerGenerateService {
  void configurePartner(
      Partner partner, PartnerApiConfiguration partnerApiConfiguration, String siret)
      throws AxelorException;
}
