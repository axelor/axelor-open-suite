package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.google.common.base.Strings;

public abstract class RegistrationNumberValidation {
  public abstract boolean computeRegistrationCodeValidity(String registrationCode);

  public void setRegistrationCodeValidationValues(Partner partner) {
    Address mainAddress = partner.getMainAddress();
    if (mainAddress == null || mainAddress.getAddressL7Country() == null) {
      return;
    }
    Country businessCountry = mainAddress.getAddressL7Country();
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

  protected String getTaxNbrFromRegistrationCode(Partner partner) {
    String taxNbr = "";

    if (partner.getMainAddress() != null
        && partner.getMainAddress().getAddressL7Country() != null) {
      String countryCode = partner.getMainAddress().getAddressL7Country().getAlpha2Code();
      String regCode = partner.getRegistrationCode();

      if (regCode != null) {
        regCode = regCode.replaceAll(" ", "");

        if (regCode.length() == 14) {
          String siren = regCode.substring(0, 9);
          String taxKey = getTaxKeyFromSIREN(siren);

          taxNbr = String.format("%s%s%s", countryCode, taxKey, siren);
        }
      }
    }

    return taxNbr;
  }

  protected String getNicFromRegistrationCode(Partner partner) {
    String regCode = partner.getRegistrationCode();
    String nic = "";

    Country businessCountry = partner.getMainAddress().getAddressL7Country();
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

    Country businessCountry = partner.getMainAddress().getAddressL7Country();
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
