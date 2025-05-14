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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;

public class BillOfMaterialCheckServiceImpl implements BillOfMaterialCheckService {

  protected final ManufOrderRepository manufOrderRepository;

  @Inject
  public BillOfMaterialCheckServiceImpl(ManufOrderRepository manufOrderRepository) {
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public void checkUsedBom(BillOfMaterial billOfMaterial) throws AxelorException {
    Objects.requireNonNull(billOfMaterial);

    // Check usage in ManufOrder
    var anyManufOrder =
        manufOrderRepository
            .all()
            .filter("self.billOfMaterial = :billOfMaterial")
            .bind("billOfMaterial", billOfMaterial)
            .fetchOne();

    if (anyManufOrder != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_REGENERATE_BOM_AS_ALREADY_IN_PRODUCTION));
    }
  }
}
