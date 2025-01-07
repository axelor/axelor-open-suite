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
package com.axelor.apps.stock.db.repo.listener;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.axelor.i18n.I18n;
import javax.persistence.PreRemove;

public class MassStockMovableProductListener {

  @PreRemove
  protected void onPreRemove(MassStockMovableProduct movedProduct) throws AxelorException {
    if (movedProduct.getStockMoveLine() != null
        && movedProduct.getStockMoveLine().getStockMove() != null
        && movedProduct.getStockMoveLine().getStockMove().getStatusSelect()
            == StockMoveRepository.STATUS_REALIZED) {

      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_STOCK_MOVE_LINE_CANT_DELETE));
    }
  }
}
