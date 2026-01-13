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
package com.axelor.apps.base.service.partner.registrationnumber.factory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
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
    Address mainAddress = partner.getMainAddress();
    if (mainAddress == null || mainAddress.getCountry() == null) {
      return null;
    }
    Country businessCountry = partner.getMainAddress().getCountry();
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
