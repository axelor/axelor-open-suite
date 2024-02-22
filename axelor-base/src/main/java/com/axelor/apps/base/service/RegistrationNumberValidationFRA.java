package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;

public class RegistrationNumberValidationFRA extends RegistrationNumberValidation {
  public boolean computeRegistrationCodeValidity(String registrationCode) {
    int sum = 0;
    boolean isOddNumber = true;
    registrationCode = registrationCode.replace(" ", "");
    if (registrationCode.length() != 14) {
      return false;
    }
    int i = registrationCode.length() - 1;
    while (i > -1) {
      int number = Character.getNumericValue(registrationCode.charAt(i));
      if (number < 0) {
        i--;
        continue;
      }
      if (!isOddNumber) {
        number *= 2;
      }
      if (number < 10) {
        sum += number;
      } else {
        number -= 10;
        sum += number + 1;
      }
      i--;
      isOddNumber = !isOddNumber;
    }
    return sum % 10 == 0;
  }

  @Override
  protected String getTaxNbrFromRegistrationCode(Partner partner) {
    if (partner.getMainAddress() != null
        && partner.getMainAddress().getCountry() != null
        && partner.getMainAddress().getCountry().getRegistrationNumberTemplate() != null
        && partner.getMainAddress().getCountry().getRegistrationNumberTemplate().getUseTaxNbr()) {
      String taxNbr = "";
      String countryCode = partner.getMainAddress().getCountry().getAlpha2Code();
      String regCode = partner.getRegistrationCode();

      if (regCode != null) {
        regCode = regCode.replaceAll(" ", "");

        if (regCode.length() == 14) {
          String siren = regCode.substring(0, 9);
          String taxKey = getTaxKeyFromSIREN(siren);

          taxNbr = String.format("%s%s%s", countryCode, taxKey, siren);
        }
      }

      return taxNbr;
    } else {
      return null;
    }
  }
}
