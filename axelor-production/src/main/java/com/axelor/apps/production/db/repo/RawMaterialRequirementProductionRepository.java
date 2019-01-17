/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.production.db.RawMaterialRequirement;
import com.axelor.apps.production.service.RawMaterialRequirementService;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class RawMaterialRequirementProductionRepository extends RawMaterialRequirementRepository {

  @Override
  public RawMaterialRequirement save(RawMaterialRequirement rawMaterialRequirement) {
    try {
      if (rawMaterialRequirement.getCode() == null) {
        RawMaterialRequirementService rawMaterialRequirementService =
            Beans.get(RawMaterialRequirementService.class);
        String seq = rawMaterialRequirementService.getSequence(rawMaterialRequirement);
        rawMaterialRequirement.setCode(seq);
      }

      return super.save(rawMaterialRequirement);
    } catch (Exception e) {
      JPA.em().getTransaction().rollback();
      JPA.runInTransaction(() -> TraceBackService.trace(e));
      JPA.em().getTransaction().begin();
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
