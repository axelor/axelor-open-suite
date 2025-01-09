/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.partner.registrationnumber;

import com.axelor.apps.base.db.Partner;

public class RegistrationNumberValidatorFRA extends RegistrationNumberValidator {
  @Override
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
