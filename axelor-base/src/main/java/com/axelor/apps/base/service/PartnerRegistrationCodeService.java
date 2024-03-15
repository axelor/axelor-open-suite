package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;

public interface PartnerRegistrationCodeService {
  String getRegistrationCodeTitleFromTemplate(Partner partner);

  Class<? extends RegistrationNumberValidator> getRegistrationNumberValidatorClass(Partner partner);

  boolean isSirenHidden(Partner partner);

  boolean isNicHidden(Partner partner);

  boolean isTaxNbrHidden(Partner partner);
}
