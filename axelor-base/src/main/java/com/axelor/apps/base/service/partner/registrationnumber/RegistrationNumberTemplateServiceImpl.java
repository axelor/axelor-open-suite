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

import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.axelor.i18n.I18n;

public class RegistrationNumberTemplateServiceImpl implements RegistrationNumberTemplateService {
  @Override
  public String checkParametersLegality(RegistrationNumberTemplate registrationNumberTemplate) {
    Boolean useSiren = registrationNumberTemplate.getUseSiren();
    String errorMessage = null;
    if (useSiren) {
      errorMessage = checkSirenLegality(registrationNumberTemplate);
      if (errorMessage != null) {
        return errorMessage;
      }
    }
    Boolean useNic = registrationNumberTemplate.getUseNic();
    if (useNic) {
      errorMessage = checkNicLegality(registrationNumberTemplate);
      return errorMessage;
    }
    return null;
  }

  protected String checkSirenLegality(RegistrationNumberTemplate registrationNumberTemplate) {
    Integer sirenPos = registrationNumberTemplate.getSirenPos();
    Integer sirenLength = registrationNumberTemplate.getSirenLength();
    Integer requiredSize = registrationNumberTemplate.getRequiredSize();
    if (sirenPos > requiredSize) {
      return I18n.get(
          "For the short registration number, the starting position in the registration number should be less than the required size.");
    }
    if (sirenPos + sirenLength > requiredSize + 1) {
      return I18n.get(
          "For the short registration number, the sum of the starting position in the registration number and length should not be greater than the required size.");
    }
    return null;
  }

  protected String checkNicLegality(RegistrationNumberTemplate registrationNumberTemplate) {
    Integer nicPos = registrationNumberTemplate.getNicPos();
    Integer nicLength = registrationNumberTemplate.getNicLength();
    Integer requiredSize = registrationNumberTemplate.getRequiredSize();
    if (nicPos > requiredSize) {
      return I18n.get(
          "For the internal classification number, the starting position in the registration number should be less than the required size.");
    }
    if (nicPos + nicLength > requiredSize + 1) {
      return I18n.get(
          "For the internal classification number, the sum of the starting position in the registration number and length should not be greater than the required size.");
    }
    return null;
  }
}
