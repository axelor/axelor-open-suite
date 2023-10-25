/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.RawMaterialRequirement;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class RawMaterialRequirementServiceImpl implements RawMaterialRequirementService {

  @Override
  public String getSequence(RawMaterialRequirement rawMaterialRequirement) throws AxelorException {

    String seq =
        Beans.get(SequenceService.class)
            .getSequenceNumber(
                SequenceRepository.RAW_MATERIAL_REQUIREMENT,
                rawMaterialRequirement.getCompany(),
                RawMaterialRequirement.class,
                "code");
    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.RAW_MATERIAL_REQUIREMENT_NO_SEQUENCE),
          rawMaterialRequirement.getCompany().getName());
    }

    return seq;
  }
}
