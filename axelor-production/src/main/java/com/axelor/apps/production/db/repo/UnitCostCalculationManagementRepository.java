/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.db.repo;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class UnitCostCalculationManagementRepository extends UnitCostCalculationRepository {

  @Override
  public UnitCostCalculation save(UnitCostCalculation entity) {

    try {
      if (Strings.isNullOrEmpty(entity.getUnitCostCalcSeq())
          && entity.getStatusSelect() == UnitCostCalculationRepository.STATUS_DRAFT) {
        entity.setUnitCostCalcSeq(Beans.get(SequenceService.class).getDraftSequenceNumber(entity));
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e);
    }

    return super.save(entity);
  }

  @Override
  public UnitCostCalculation copy(UnitCostCalculation entity, boolean deep) {
    entity.setStatusSelect(UnitCostCalculationRepository.STATUS_DRAFT);
    entity.setUnitCostCalcSeq(null);
    entity.setCalculationDate(null);
    entity.setUpdateCostDate(null);
    entity.setUnitCostCalcLineList(null);

    return super.copy(entity, deep);
  }
}
