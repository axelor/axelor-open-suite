package com.axelor.apps.base.service.partner;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;

public interface PartnerCreationService {

  Partner createPartner(
      Integer partnerTypeSelect,
      Integer titleSelect,
      String firstName,
      String name,
      Partner mainPartner,
      String description,
      boolean isContact,
      boolean isCustomer,
      boolean isSupplier,
      boolean isProspect)
      throws AxelorException;
}
