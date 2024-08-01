/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.common.StringUtils;

public class PartnerRegistrationCodeViewServiceImpl implements PartnerRegistrationCodeViewService {
  @Override
  public String getRegistrationCodeTitleFromTemplate(Partner partner) {
    if (partner.getMainAddress() != null && partner.getMainAddress().getCountry() != null) {
      Country businessCountry = partner.getMainAddress().getCountry();
      RegistrationNumberTemplate registrationNumberTemplate =
          businessCountry.getRegistrationNumberTemplate();
      if (registrationNumberTemplate != null
          && !StringUtils.isBlank(registrationNumberTemplate.getTitleToDisplay())) {
        return registrationNumberTemplate.getTitleToDisplay();
      }
    }
    return null;
  }

  @Override
  public boolean isSirenHidden(Partner partner) {
    boolean isIndividual =
        partner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL;
    Address address = partner.getMainAddress();
    if (address != null && address.getCountry() != null) {
      RegistrationNumberTemplate registrationNumberTemplate =
          address.getCountry().getRegistrationNumberTemplate();
      if (registrationNumberTemplate == null) {
        return isIndividual;
      }
      return isIndividual || !registrationNumberTemplate.getUseSiren();
    }
    return false;
  }

  @Override
  public boolean isNicHidden(Partner partner) {
    boolean isIndividual =
        partner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL;
    Address address = partner.getMainAddress();
    if (address != null && address.getCountry() != null) {
      RegistrationNumberTemplate registrationNumberTemplate =
          address.getCountry().getRegistrationNumberTemplate();
      if (registrationNumberTemplate == null) {
        return isIndividual;
      }
      return isIndividual || !registrationNumberTemplate.getUseNic();
    }
    return false;
  }

  @Override
  public boolean isTaxNbrHidden(Partner partner) {
    boolean isCustomer = partner.getIsCustomer();
    boolean hideTaxNbr = !isCustomer;
    Address address = partner.getMainAddress();
    if (address != null && address.getCountry() != null) {
      RegistrationNumberTemplate registrationNumberTemplate =
          address.getCountry().getRegistrationNumberTemplate();
      if (registrationNumberTemplate == null) {
        return hideTaxNbr;
      }
      boolean useTaxNbr = registrationNumberTemplate.getUseTaxNbr();
      hideTaxNbr = !useTaxNbr || !isCustomer;
    }
    return hideTaxNbr;
  }
}
