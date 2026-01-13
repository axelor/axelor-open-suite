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
package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class StoredProductProcessingCancelServiceImpl
    implements MassStockMovableProductProcessingCancelService<StoredProduct> {

  @Override
  public void preCancel(StoredProduct movableProduct) throws AxelorException {
    // Nothing
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void postCancel(StoredProduct movableProduct) throws AxelorException {
    movableProduct.setStoredQty(BigDecimal.ZERO);
    var massStockMove = movableProduct.getMassStockMove();

    massStockMove.setStatusSelect(MassStockMoveRepository.STATUS_IN_PROGRESS);
  }
}
