package com.axelor.apps.base.service.partner.registrationnumber.factory;

import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.axelor.apps.base.service.partner.registrationnumber.RegistrationNumberValidator;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;

public class PartnerRegistrationValidatorFactoryServiceImpl
    implements PartnerRegistrationValidatorFactoryService {

  @Override
  public RegistrationNumberValidator getRegistrationNumberValidator(Partner partner)
      throws ClassNotFoundException {
    Class<? extends RegistrationNumberValidator> klass =
        getRegistrationNumberValidatorClass(partner);
    if (klass == null) {
      return null;
    } else {
      return Beans.get(klass);
    }
  }

  protected Class<? extends RegistrationNumberValidator> getRegistrationNumberValidatorClass(
      Partner partner) throws ClassNotFoundException {
    Localization localization = partner.getLocalization();
    if (localization == null) {
      return null;
    }
    Country businessCountry = localization.getCountry();
    RegistrationNumberTemplate registrationNumberTemplate =
        businessCountry.getRegistrationNumberTemplate();

    if (registrationNumberTemplate != null) {
      Class<? extends RegistrationNumberValidator> klass = null;
      String origin = registrationNumberTemplate.getValidationMethodSelect();
      if (!Strings.isNullOrEmpty(origin)) {
        klass = (Class<? extends RegistrationNumberValidator>) Class.forName(origin);
      }
      return klass;
    }
    return null;
  }
}
