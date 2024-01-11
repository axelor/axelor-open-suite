package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;
import java.util.Map;

public interface RegistrationNumberValidation {
  public boolean computeRegistrationCodeValidity(String registrationCode);
}
