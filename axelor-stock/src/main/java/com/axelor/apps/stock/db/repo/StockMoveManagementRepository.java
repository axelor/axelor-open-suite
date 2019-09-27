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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class StockMoveManagementRepository extends StockMoveRepository {
  @Override
  public StockMove copy(StockMove entity, boolean deep) {

    StockMove copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setStockMoveSeq(null);
    copy.setName(null);
    copy.setRealDate(null);

    return copy;
  }

  @Override
  public StockMove save(StockMove entity) {
    try {
      StockMove stockMove = super.save(entity);
      SequenceService sequenceService = Beans.get(SequenceService.class);

      if (Strings.isNullOrEmpty(stockMove.getStockMoveSeq())) {
        stockMove.setStockMoveSeq(sequenceService.getDraftSequenceNumber(stockMove));
      }

      if (Strings.isNullOrEmpty(stockMove.getName())
          || stockMove.getName().startsWith(stockMove.getStockMoveSeq())) {
        stockMove.setName(Beans.get(StockMoveService.class).computeName(stockMove));
      }

      return stockMove;
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void remove(StockMove entity) {
    if (entity.getStatusSelect() == STATUS_PLANNED) {
      throw new PersistenceException(I18n.get(IExceptionMessage.STOCK_MOVE_NOT_DELETED));
    } else if (entity.getStatusSelect() == STATUS_REALIZED) {
      entity.setArchived(true);
    } else {
      super.remove(entity);
    }
  }
}
