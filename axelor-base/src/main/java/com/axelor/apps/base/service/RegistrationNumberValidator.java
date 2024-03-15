package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.google.common.base.Strings;

public abstract class RegistrationNumberValidator {
  public abstract boolean computeRegistrationCodeValidity(String registrationCode);

  protected abstract String getTaxNbrFromRegistrationCode(Partner partner);

  public void setRegistrationCodeValidationValues(Partner partner) {
    Address mainAddress = partner.getMainAddress();
    if (mainAddress == null || mainAddress.getCountry() == null) {
      return;
    }
    Country businessCountry = mainAddress.getCountry();
    RegistrationNumberTemplate registrationNumberTemplate =
        businessCountry.getRegistrationNumberTemplate();

    if (registrationNumberTemplate != null
        && !Strings.isNullOrEmpty(partner.getRegistrationCode())) {
      partner.setTaxNbr(getTaxNbrFromRegistrationCode(partner));
      partner.setNic(getNicFromRegistrationCode(partner));
      partner.setSiren(getSirenFromRegistrationCode(partner));
    }
  }

  protected String getTaxKeyFromSIREN(String sirenStr) {
    int siren = Integer.parseInt(sirenStr);
    int taxKey = Math.floorMod(siren, 97);
    taxKey = Math.floorMod(12 + 3 * taxKey, 97);
    return String.format("%02d", taxKey);
  }

  protected String getNicFromRegistrationCode(Partner partner) {
    String regCode = partner.getRegistrationCode();
    String nic = "";

    Country businessCountry = partner.getMainAddress().getCountry();
    RegistrationNumberTemplate registrationNumberTemplate =
        businessCountry.getRegistrationNumberTemplate();
    if (registrationNumberTemplate.getUseNic() && registrationNumberTemplate.getNicLength() != 0) {
      regCode = regCode.replaceAll(" ", "");
      nic =
          regCode.substring(
              registrationNumberTemplate.getNicPos() - 1,
              registrationNumberTemplate.getNicPos()
                  + registrationNumberTemplate.getNicLength()
                  - 1);
    }

    return nic;
  }

  public String getSirenFromRegistrationCode(Partner partner) {
    String regCode = partner.getRegistrationCode();
    String siren = "";

    Country businessCountry = partner.getMainAddress().getCountry();
    RegistrationNumberTemplate registrationNumberTemplate =
        businessCountry.getRegistrationNumberTemplate();
    if (registrationNumberTemplate.getUseSiren()
        && registrationNumberTemplate.getSirenLength() != 0) {
      regCode = regCode.replaceAll(" ", "");
      siren =
          regCode.substring(
              registrationNumberTemplate.getSirenPos() - 1,
              registrationNumberTemplate.getSirenPos()
                  + registrationNumberTemplate.getSirenLength()
                  - 1);
    }
    return siren;
  }
}
