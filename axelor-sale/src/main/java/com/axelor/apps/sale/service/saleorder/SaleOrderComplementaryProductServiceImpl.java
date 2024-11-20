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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.ComplementaryProductSelected;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.axelor.db.JpaSequence;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.RoundingMode;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SaleOrderComplementaryProductServiceImpl
    implements SaleOrderComplementaryProductService {

  protected final AppSaleService appSaleService;
  protected final SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;

  @Inject
  public SaleOrderComplementaryProductServiceImpl(
      AppSaleService appSaleService,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService) {
    this.appSaleService = appSaleService;
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
  }

  @Override
  public List<SaleOrderLine> handleComplementaryProducts(SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return saleOrderLineList;
    }
    SaleOrderLine originSoLine = null;
    SaleOrderLine parentSol = null;

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      Pair<SaleOrderLine, SaleOrderLine> pair = getUnhandledSol(saleOrderLine, null);
      originSoLine = pair.getLeft();
      parentSol = pair.getRight();
      if (originSoLine != null) {
        break;
      }
    }
    if (originSoLine == null) {
      return saleOrderLineList;
    }

    boolean isSubLine = parentSol != null;
    createNewLine(saleOrder, originSoLine, parentSol, isSubLine);

    return saleOrderLineList;
  }

  protected void createNewLine(
      SaleOrder saleOrder,
      SaleOrderLine originSoLine,
      SaleOrderLine parentSaleOrderLine,
      boolean isSubLine)
      throws AxelorException {
    if (originSoLine == null) {
      return;
    }
    this.setNewManualId(originSoLine);
    List<SaleOrderLine> saleOrderLineList =
        isSubLine ? originSoLine.getSubSaleOrderLineList() : saleOrder.getSaleOrderLineList();
    List<ComplementaryProductSelected> complementaryProductSelectedList =
        originSoLine.getSelectedComplementaryProductList();

    if (originSoLine.getProduct() != null
        && CollectionUtils.isNotEmpty(complementaryProductSelectedList)) {
      for (ComplementaryProductSelected compProductSelected : complementaryProductSelectedList) {
        // Search if there is already a line for this product to modify or remove
        SaleOrderLine newSoLine = null;
        if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
          for (SaleOrderLine soLine : saleOrderLineList) {
            if (originSoLine.getManualId().equals(soLine.getParentId())
                && soLine.getProduct() != null
                && soLine.getProduct().equals(compProductSelected.getProduct())) {
              // Edit line if it already exists instead of recreating, otherwise remove if already
              // exists and is no longer selected
              if (compProductSelected.getIsSelected()) {
                newSoLine = soLine;
              } else {
                if (isSubLine) {
                  originSoLine.removeSubSaleOrderLineListItem(soLine);
                } else {
                  saleOrder.removeSaleOrderLineListItem(soLine);
                }
              }
              break;
            }
          }
        }

        createOrUpdateComplementaryLine(
            saleOrder,
            originSoLine,
            parentSaleOrderLine,
            isSubLine,
            compProductSelected,
            newSoLine,
            saleOrderLineList);
      }
      originSoLine.setIsComplementaryProductsUnhandledYet(false);
    }
    List<SaleOrderLine> updatedSaleOrderLineList =
        isSubLine
            ? parentSaleOrderLine.getSubSaleOrderLineList()
            : saleOrder.getSaleOrderLineList();
    for (int i = 0; i < updatedSaleOrderLineList.size(); i++) {
      updatedSaleOrderLineList.get(i).setSequence(i + 1);
    }
  }

  protected void createOrUpdateComplementaryLine(
      SaleOrder saleOrder,
      SaleOrderLine originSoLine,
      SaleOrderLine parentSaleOrderLine,
      boolean isSubLine,
      ComplementaryProductSelected compProductSelected,
      SaleOrderLine newSoLine,
      List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    if (newSoLine != null) {
      newSoLine.setQty(
          originSoLine
              .getQty()
              .multiply(compProductSelected.getQty())
              .setScale(appSaleService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));

      saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, newSoLine);
      return;
    }
    if (compProductSelected.getIsSelected()) {
      newSoLine = new SaleOrderLine();
      newSoLine.setProduct(compProductSelected.getProduct());
      if (isSubLine) {
        newSoLine.setParentSaleOrderLine(parentSaleOrderLine);
      } else {
        newSoLine.setSaleOrder(saleOrder);
      }

      newSoLine.setQty(
          originSoLine
              .getQty()
              .multiply(compProductSelected.getQty())
              .setScale(appSaleService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));
      saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, newSoLine);

      newSoLine.setParentId(originSoLine.getManualId());

      if (isSubLine) {
        parentSaleOrderLine.addSubSaleOrderLineListItem(newSoLine);
      } else {
        saleOrder.addSaleOrderLineListItem(newSoLine);
      }
    }
  }

  protected Pair<SaleOrderLine, SaleOrderLine> getUnhandledSol(
      SaleOrderLine saleOrderLine, SaleOrderLine parentSol) {
    Pair<SaleOrderLine, SaleOrderLine> pair = new MutablePair<>();
    SaleOrderLine unhandledSol = null;
    if (saleOrderLine.getIsComplementaryProductsUnhandledYet()) {
      return Pair.of(saleOrderLine, parentSol);
    }

    List<SaleOrderLine> subLines = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(subLines)) {
      unhandledSol =
          subLines.stream()
              .filter(SaleOrderLine::getIsComplementaryProductsUnhandledYet)
              .findAny()
              .orElse(null);
      if (unhandledSol != null) {
        return Pair.of(unhandledSol, saleOrderLine);
      }

      for (SaleOrderLine subLine : subLines) {
        pair = getUnhandledSol(subLine, saleOrderLine);
        if (pair != null && pair.getLeft() != null) {
          return pair;
        }
      }
    }
    return pair;
  }

  @Transactional
  @Override
  public void setNewManualId(SaleOrderLine saleOrderLine) {
    if (saleOrderLine.getManualId() == null || saleOrderLine.getManualId().equals("")) {
      saleOrderLine.setManualId(JpaSequence.nextValue("sale.order.line.idSeq"));
    }
  }
}
