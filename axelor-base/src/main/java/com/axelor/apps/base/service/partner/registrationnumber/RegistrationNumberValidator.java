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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RegistrationNumberValidator {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected abstract boolean computeRegistrationCodeValidity(String registrationCode);

  public boolean isRegistrationCodeValid(Partner partner) {
    if (partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_COMPANY
        || Strings.isNullOrEmpty(partner.getRegistrationCode())
        || partner.getMainAddress() == null) {
      return true;
    }

    try {
      checkRegistrationCode(partner);
    } catch (AxelorException e) {
      log.error(e.getMessage());
      return false;
    }
    return true;
  }

  protected void checkRegistrationCode(Partner partner) throws AxelorException {

    String registrationCode = partner.getRegistrationCode();

    if (registrationCode == null) {
      registrationCode = "";
    }

    registrationCode = registrationCode.replace(" ", "");
    RegistrationNumberTemplate registrationNumberTemplate =
        Optional.of(partner)
            .map(Partner::getMainAddress)
            .map(Address::getCountry)
            .map(Country::getRegistrationNumberTemplate)
            .orElse(null);
    if (registrationNumberTemplate == null) {
      return;
    }
    if (registrationNumberTemplate.getIsRequiredForCompanies()
        && StringUtils.isBlank(registrationCode)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BaseExceptionMessage.REGISTRATION_CODE_EMPTY_FOR_COMPANIES));
    }

    if (registrationCode.length() != registrationNumberTemplate.getRequiredSize()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BaseExceptionMessage.PARTNER_INVALID_REGISTRATION_CODE));
    }

    if (!computeRegistrationCodeValidity(registrationCode)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BaseExceptionMessage.PARTNER_INVALID_REGISTRATION_CODE));
    }
  }

  public void setRegistrationCodeValidationValues(Partner partner) throws AxelorException {
    Address mainAddress = partner.getMainAddress();
    if (mainAddress == null || mainAddress.getCountry() == null) {
      return;
    }

    checkRegistrationCode(partner);
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

  protected abstract String getTaxNbrFromRegistrationCode(Partner partner);

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

  protected String getSirenFromRegistrationCode(Partner partner) {
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
