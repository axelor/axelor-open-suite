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
package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Objects;

public class PickedProductProcessingCancelServiceImpl
    implements MassStockMovableProductProcessingCancelService<PickedProduct> {

  @Override
  public void preCancel(PickedProduct movableProduct) throws AxelorException {
    Objects.requireNonNull(movableProduct);

    if (movableProduct.getStoredProductList() != null
        && movableProduct.getStoredProductList().stream()
            .anyMatch(it -> it.getStockMoveLine() != null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_ALREADY_STORED_PRODUCT),
          movableProduct.getProduct().getFullName());
    }

    removeStoredProducts(movableProduct);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void postCancel(PickedProduct movableProduct) throws AxelorException {
    movableProduct.setPickedQty(BigDecimal.ZERO);
    var massStockMove = movableProduct.getMassStockMove();

    if (massStockMove.getPickedProductList().stream()
        .allMatch(storedProduct -> storedProduct.getStockMoveLine() == null)) {
      massStockMove.setStatusSelect(MassStockMoveRepository.STATUS_CANCELED);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void removeStoredProducts(PickedProduct movableProduct) {
    for (StoredProduct storedProduct : movableProduct.getStoredProductList()) {
      storedProduct.setMassStockMove(null);
    }
  }
}
