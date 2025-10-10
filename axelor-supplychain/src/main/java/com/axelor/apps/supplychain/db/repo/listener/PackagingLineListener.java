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
package com.axelor.apps.supplychain.db.repo.listener;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.service.packaging.PackagingLineService;
import com.axelor.inject.Beans;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;

public class PackagingLineListener {

  @PrePersist
  public void onSave(PackagingLine packagingLine) throws AxelorException {
    Beans.get(PackagingLineService.class).updateStockMoveSet(packagingLine, true);
    Beans.get(PackagingLineService.class).updateQtyRemainingToPackage(packagingLine, true);
  }

  @PreRemove
  public void onRemove(PackagingLine packagingLine) throws AxelorException {
    Beans.get(PackagingLineService.class).updateStockMoveSet(packagingLine, false);
    Beans.get(PackagingLineService.class).updateQtyRemainingToPackage(packagingLine, false);
  }
}
