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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import java.util.Map;
import javax.persistence.PersistenceException;

public class StockMoveManagementRepository extends StockMoveRepository {

  @Override
  public StockMove copy(StockMove entity, boolean deep) {

    StockMove copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setStockMoveSeq(null);
    copy.setName(null);
    copy.setRealDate(null);
    copy.setPickingEditDate(null);
    copy.setPickingIsEdited(false);
    copy.setAvailabilityRequest(false);
    copy.setSupplierShipmentDate(null);
    copy.setSupplierShipmentRef(null);
    copy.setAvailabilityRequest(false);
    copy.setFullySpreadOverLogisticalFormsFlag(false);

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
        stockMove.setName(Beans.get(StockMoveToolService.class).computeName(stockMove));
      }

      return stockMove;
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void remove(StockMove entity) {
    if (entity.getStatusSelect() == STATUS_PLANNED) {
      throw new PersistenceException(I18n.get(IExceptionMessage.STOCK_MOVE_PLANNED_NOT_DELETED));
    } else if (entity.getStatusSelect() == STATUS_REALIZED) {
      throw new PersistenceException(I18n.get(IExceptionMessage.STOCK_MOVE_REALIZED_NOT_DELETED));
    } else {
      if (entity.getStockMoveOrigin() != null) {
        entity.getStockMoveOrigin().setBackorderId(null);
      }
      super.remove(entity);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long stockMoveId = (Long) json.get("id");
    StockMove stockMove = find(stockMoveId);

    if (stockMove.getStatusSelect() > STATUS_PLANNED
        || stockMove.getStockMoveLineList() == null
        || (stockMove.getFromStockLocation() != null
            && stockMove.getFromStockLocation().getTypeSelect()
                == StockLocationRepository.TYPE_VIRTUAL)) {
      return super.populate(json, context);
    }

    int available = 0, availableForProduct = 0, missing = 0;
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine
          .getProduct()
          .getProductTypeSelect()
          .equals(ProductRepository.PRODUCT_TYPE_SERVICE)) {
        continue;
      }
      Beans.get(StockMoveLineService.class)
          .updateAvailableQty(stockMoveLine, stockMove.getFromStockLocation());
      Product product = stockMoveLine.getProduct();
      if (stockMoveLine.getAvailableQty().compareTo(stockMoveLine.getRealQty()) >= 0
          || product != null && !product.getStockManaged()) {
        available++;
      } else if (stockMoveLine.getAvailableQtyForProduct().compareTo(stockMoveLine.getRealQty())
          >= 0) {
        availableForProduct++;
      } else if (stockMoveLine.getAvailableQty().compareTo(stockMoveLine.getRealQty()) < 0
          && stockMoveLine.getAvailableQtyForProduct().compareTo(stockMoveLine.getRealQty()) < 0) {
        missing++;
      }
    }

    if ((available > 0 || availableForProduct > 0) && missing == 0) {
      json.put("availableStatusSelect", StockMoveRepository.STATUS_AVAILABLE);
    } else if ((available > 0 || availableForProduct > 0) && missing > 0) {
      json.put("availableStatusSelect", StockMoveRepository.STATUS_PARTIALLY_AVAILABLE);
    } else if (available == 0 && availableForProduct == 0 && missing > 0) {
      json.put("availableStatusSelect", StockMoveRepository.STATUS_UNAVAILABLE);
    }
    return super.populate(json, context);
  }
}
