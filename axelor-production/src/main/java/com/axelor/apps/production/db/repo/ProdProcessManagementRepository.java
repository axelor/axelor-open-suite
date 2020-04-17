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

import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;

public class ProdProcessManagementRepository extends ProdProcessRepository {

  @Override
  public ProdProcess save(ProdProcess prodProcess) {

    if (prodProcess.getVersionNumber() != null && prodProcess.getVersionNumber() > 1)
      prodProcess.setFullName(
          prodProcess.getName() + " - v" + String.valueOf(prodProcess.getVersionNumber()));
    else prodProcess.setFullName(prodProcess.getName());

    if (prodProcess.getOutsourcing()) {
      for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
        prodProcessLine.setOutsourcing(true);
      }
    }
    return super.save(prodProcess);
  }

  @Override
  public ProdProcess copy(ProdProcess entity, boolean deep) {

    ProdProcess copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setVersionNumber(1);
    copy.setOriginalProdProcess(null);

    return copy;
  }
}
