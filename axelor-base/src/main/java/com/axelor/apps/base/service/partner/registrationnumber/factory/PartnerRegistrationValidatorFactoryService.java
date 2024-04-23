package com.axelor.apps.base.service.partner.registrationnumber.factory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.partner.registrationnumber.RegistrationNumberValidator;

public interface PartnerRegistrationValidatorFactoryService {

  RegistrationNumberValidator getRegistrationNumberValidator(Partner partner)
      throws ClassNotFoundException;
}
