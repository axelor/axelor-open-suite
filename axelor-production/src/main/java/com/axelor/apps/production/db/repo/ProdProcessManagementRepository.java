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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.ProdProcess;

public class ProdProcessManagementRepository extends ProdProcessRepository {

  @Override
  public ProdProcess save(ProdProcess prodProcess) {

    if (prodProcess.getVersionNumber() != null && prodProcess.getVersionNumber() > 1)
      prodProcess.setFullName(
          prodProcess.getName() + " - v" + String.valueOf(prodProcess.getVersionNumber()));
    else prodProcess.setFullName(prodProcess.getName());

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
