package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;

public interface PartnerGenerateService {
  void configurePartner(Partner partner, String siret) throws AxelorException;
}
