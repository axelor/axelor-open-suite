package com.axelor.apps.base.service.partner.registrationnumber;

import com.axelor.apps.base.db.Partner;

public class RegistrationNumberValidatorDefault extends RegistrationNumberValidator {
  @Override
  public boolean computeRegistrationCodeValidity(String registrationCode) {
    return true;
  }

  @Override
  protected String getTaxNbrFromRegistrationCode(Partner partner) {
    return null;
  }
}
