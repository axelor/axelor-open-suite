package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;

public class RegistrationNumberValidationDefault extends RegistrationNumberValidation {
  public boolean computeRegistrationCodeValidity(String registrationCode) {
    return true;
  }

  @Override
  protected String getTaxNbrFromRegistrationCode(Partner partner) {
    return null;
  }
}
