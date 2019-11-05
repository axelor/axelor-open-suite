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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class StockMoveLineController {

  public void getProductPrice(ActionRequest request, ActionResponse response) {

    StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
    Context context = request.getContext();
    Integer parentPackPriceSelect = (Integer) context.getParent().get("packPriceSelect");

    if (stockMoveLine.getPackPriceSelect() != null
        && stockMoveLine.getPackPriceSelect() == StockMoveLineRepository.SUBLINE_PRICE_ONLY
        && stockMoveLine.getLineTypeSelect() == StockMoveLineRepository.TYPE_PACK) {
      response.setValue("unitPriceUntaxed", 0.00);
      response.setValue("unitPriceTaxed", 0.00);

    } else if (parentPackPriceSelect != null) {
      if (stockMoveLine.getIsSubLine() != null) {
        if (parentPackPriceSelect == StockMoveLineRepository.PACK_PRICE_ONLY) {
          response.setValue("unitPriceUntaxed", 0.00);
          response.setValue("unitPriceTaxed", 0.00);
        }
      }
    }
  }

  public List<StockMoveLine> updateQty(
      List<StockMoveLine> moveLines,
      BigDecimal oldKitQty,
      BigDecimal newKitQty,
      boolean isRealQty) {

    BigDecimal qty = BigDecimal.ZERO;

    if (moveLines != null) {
      if (newKitQty.compareTo(BigDecimal.ZERO) != 0) {
        for (StockMoveLine line : moveLines) {
          qty = (line.getQty().divide(oldKitQty, 2, RoundingMode.HALF_EVEN)).multiply(newKitQty);
          line.setQty(qty.setScale(2, RoundingMode.HALF_EVEN));
          line.setRealQty(qty.setScale(2, RoundingMode.HALF_EVEN));
        }
      } else {
        for (StockMoveLine line : moveLines) {
          line.setQty(qty.setScale(2, RoundingMode.HALF_EVEN));
          line.setRealQty(qty.setScale(2, RoundingMode.HALF_EVEN));
        }
      }
    }

    return moveLines;
  }

  public List<StockMoveLine> updateRealQty(
      List<StockMoveLine> moveLines,
      BigDecimal oldKitQty,
      BigDecimal newKitQty,
      boolean isRealQty) {

    BigDecimal qty = BigDecimal.ZERO;

    if (moveLines != null) {
      if (newKitQty.compareTo(BigDecimal.ZERO) != 0) {
        for (StockMoveLine line : moveLines) {
          qty =
              (line.getRealQty().divide(oldKitQty, 2, RoundingMode.HALF_EVEN)).multiply(newKitQty);
          line.setRealQty(qty.setScale(2, RoundingMode.HALF_EVEN));
        }
      } else {
        for (StockMoveLine line : moveLines) {
          line.setRealQty(qty.setScale(2, RoundingMode.HALF_EVEN));
        }
      }
    }

    return moveLines;
  }

  public void updateSubLineQty(ActionRequest request, ActionResponse response) {

    StockMoveLine titleMoveLine = request.getContext().asType(StockMoveLine.class);
    BigDecimal oldKitQty = BigDecimal.ONE;
    BigDecimal newKitQty = BigDecimal.ZERO;
    List<StockMoveLine> subLines = null;

    if (titleMoveLine.getOldQty().compareTo(BigDecimal.ZERO) == 0) {
      if (titleMoveLine.getId() != null) {
        StockMoveLine line = Beans.get(StockMoveLineRepository.class).find(titleMoveLine.getId());
        if (line.getQty().compareTo(BigDecimal.ZERO) != 0) {
          oldKitQty = line.getQty();
        }
      }
    } else {
      oldKitQty = titleMoveLine.getOldQty();
    }

    if (titleMoveLine.getQty().compareTo(BigDecimal.ZERO) != 0) {
      newKitQty = titleMoveLine.getQty();
    }

    if (!titleMoveLine.getIsSubLine()) {
      subLines = this.updateQty(titleMoveLine.getSubLineList(), oldKitQty, newKitQty, false);
    }

    response.setValue("oldQty", newKitQty);
    response.setValue("subLineList", subLines);
  }

  public void updateSubLineRealQty(ActionRequest request, ActionResponse response) {

    StockMoveLine titleMoveLine = request.getContext().asType(StockMoveLine.class);
    BigDecimal oldKitQty = BigDecimal.ONE;
    BigDecimal newKitQty = BigDecimal.ZERO;
    List<StockMoveLine> subLines = null;

    if (titleMoveLine.getOldQty().compareTo(BigDecimal.ZERO) == 0) {
      if (titleMoveLine.getId() != null) {
        StockMoveLine line = Beans.get(StockMoveLineRepository.class).find(titleMoveLine.getId());
        if (line.getQty().compareTo(BigDecimal.ZERO) != 0) {
          oldKitQty = line.getQty();
        }
      }
    } else {
      oldKitQty = titleMoveLine.getOldQty();
    }

    if (titleMoveLine.getRealQty().compareTo(BigDecimal.ZERO) != 0) {
      newKitQty = titleMoveLine.getRealQty();
    }

    if (!titleMoveLine.getIsSubLine()) {
      subLines = this.updateRealQty(titleMoveLine.getSubLineList(), oldKitQty, newKitQty, true);
    }

    response.setValue("oldQty", newKitQty);
    response.setValue("subLineList", subLines);
  }

  public void resetSubLines(ActionRequest request, ActionResponse response) {

    StockMoveLine packLine = request.getContext().asType(StockMoveLine.class);
    List<StockMoveLine> subLines = packLine.getSubLineList();

    if (subLines != null) {
      for (StockMoveLine line : subLines) {
        line.setUnitPriceTaxed(BigDecimal.ZERO);
        line.setUnitPriceUntaxed(BigDecimal.ZERO);
      }
    }
    response.setValue("subLineList", subLines);
  }

  /**
   * Called from stock move form view, on clicking allocateAll button on one stock move line. Call
   * {@link ReservedQtyService#allocateAll(StockMoveLine)}.
   *
   * @param request
   * @param response
   */
  public void allocateAll(ActionRequest request, ActionResponse response) {
    try {
      StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
      stockMoveLine = Beans.get(StockMoveLineRepository.class).find(stockMoveLine.getId());
      Product product = stockMoveLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).allocateAll(stockMoveLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock move form view, on clicking allocateAll button on one stock move line. Call
   * {@link ReservedQtyService#updateReservedQty(stockMoveLine, BigDecimal.ZERO)}.
   *
   * @param request
   * @param response
   */
  public void deallocateAll(ActionRequest request, ActionResponse response) {
    try {
      StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
      stockMoveLine = Beans.get(StockMoveLineRepository.class).find(stockMoveLine.getId());
      Beans.get(ReservedQtyService.class).updateReservedQty(stockMoveLine, BigDecimal.ZERO);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock move line form view, on request qty click. Call {@link
   * ReservedQtyService#requestQty(StockMoveLine)}.
   *
   * @param request
   * @param response
   */
  public void requestQty(ActionRequest request, ActionResponse response) {
    try {
      StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
      stockMoveLine = Beans.get(StockMoveLineRepository.class).find(stockMoveLine.getId());
      Product product = stockMoveLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).requestQty(stockMoveLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock move line form view, on request qty click. Call {@link
   * ReservedQtyService#cancelReservation(StockMoveLine)}.
   *
   * @param request
   * @param response
   */
  public void cancelReservation(ActionRequest request, ActionResponse response) {
    try {
      StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
      stockMoveLine = Beans.get(StockMoveLineRepository.class).find(stockMoveLine.getId());
      Product product = stockMoveLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).cancelReservation(stockMoveLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock move line request quantity wizard view. Call {@link
   * ReservedQtyService#updateReservedQty(StockMoveLine, BigDecimal)}.
   *
   * @param request
   * @param response
   */
  public void changeReservedQty(ActionRequest request, ActionResponse response) {
    try {
      StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
      BigDecimal newReservedQty = stockMoveLine.getReservedQty();
      stockMoveLine = Beans.get(StockMoveLineRepository.class).find(stockMoveLine.getId());

      Product product = stockMoveLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).updateReservedQty(stockMoveLine, newReservedQty);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
