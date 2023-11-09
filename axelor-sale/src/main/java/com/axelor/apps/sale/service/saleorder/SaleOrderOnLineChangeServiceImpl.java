/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaSequence;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderOnLineChangeServiceImpl implements SaleOrderOnLineChangeService {
  protected AppSaleService appSaleService;
  protected SaleOrderService saleOrderService;
  protected SaleOrderLineService saleOrderLineService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public SaleOrderOnLineChangeServiceImpl(
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository) {
    this.appSaleService = appSaleService;
    this.saleOrderService = saleOrderService;
    this.saleOrderLineService = saleOrderLineService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  public List<SaleOrderLine> handleComplementaryProducts(SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList == null) {
      saleOrderLineList = new ArrayList<>();
    }

    SaleOrderLine originSoLine = null;
    for (SaleOrderLine soLine : saleOrderLineList) {
      if (soLine.getIsComplementaryProductsUnhandledYet()) {
        originSoLine = soLine;
        if (originSoLine.getManualId() == null || originSoLine.getManualId().equals("")) {
          this.setNewManualId(originSoLine);
        }
        break;
      }
    }

    if (originSoLine != null
        && originSoLine.getProduct() != null
        && originSoLine.getSelectedComplementaryProductList() != null) {
      for (ComplementaryProductSelected compProductSelected :
          originSoLine.getSelectedComplementaryProductList()) {
        // Search if there is already a line for this product to modify or remove
        SaleOrderLine newSoLine = null;
        for (SaleOrderLine soLine : saleOrderLineList) {
          if (originSoLine.getManualId().equals(soLine.getParentId())
              && soLine.getProduct() != null
              && soLine.getProduct().equals(compProductSelected.getProduct())) {
            // Edit line if it already exists instead of recreating, otherwise remove if already
            // exists and is no longer selected
            if (compProductSelected.getIsSelected()) {
              newSoLine = soLine;
            } else {
              saleOrderLineList.remove(soLine);
            }
            break;
          }
        }

        if (newSoLine == null) {
          if (compProductSelected.getIsSelected()) {
            newSoLine = new SaleOrderLine();
            newSoLine.setProduct(compProductSelected.getProduct());
            newSoLine.setSaleOrder(saleOrder);
            newSoLine.setQty(
                originSoLine
                    .getQty()
                    .multiply(compProductSelected.getQty())
                    .setScale(appSaleService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));
            saleOrderLineService.computeProductInformation(newSoLine, newSoLine.getSaleOrder());
            saleOrderLineService.computeValues(newSoLine.getSaleOrder(), newSoLine);

            newSoLine.setParentId(originSoLine.getManualId());

            int targetIndex = saleOrderLineList.indexOf(originSoLine) + 1;
            saleOrderLineList.add(targetIndex, newSoLine);
          }
        } else {
          newSoLine.setQty(
              originSoLine
                  .getQty()
                  .multiply(compProductSelected.getQty())
                  .setScale(appSaleService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));

          saleOrderLineService.computeProductInformation(newSoLine, newSoLine.getSaleOrder());
          saleOrderLineService.computeValues(newSoLine.getSaleOrder(), newSoLine);
        }
      }
      originSoLine.setIsComplementaryProductsUnhandledYet(false);
    }

    for (int i = 0; i < saleOrderLineList.size(); i++) {
      saleOrderLineList.get(i).setSequence(i);
    }

    return saleOrderLineList;
  }

  @Transactional
  protected void setNewManualId(SaleOrderLine saleOrderLine) {
    saleOrderLine.setManualId(JpaSequence.nextValue("sale.order.line.idSeq"));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder updateProductQtyWithPackHeaderQty(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    boolean isStartOfPack = false;
    BigDecimal newQty = BigDecimal.ZERO;
    BigDecimal oldQty = BigDecimal.ZERO;
    saleOrderService.sortSaleOrderLineList(saleOrder);

    for (SaleOrderLine SOLine : saleOrderLineList) {

      if (SOLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK && !isStartOfPack) {
        newQty = SOLine.getQty();
        oldQty = saleOrderLineRepository.find(SOLine.getId()).getQty();
        if (newQty.compareTo(oldQty) != 0) {
          isStartOfPack = true;
          SOLine = EntityHelper.getEntity(SOLine);
          saleOrderLineRepository.save(SOLine);
        }
      } else if (isStartOfPack) {
        if (SOLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK) {
          break;
        }
        saleOrderLineService.updateProductQty(SOLine, saleOrder, oldQty, newQty);
      }
    }
    return saleOrder;
  }

  @Override
  public void onLineChange(SaleOrder saleOrder) throws AxelorException {
    this.handleComplementaryProducts(saleOrder);
    if (saleOrder.getSaleOrderLineList().stream()
        .anyMatch(
            saleOrderLine ->
                saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK)) {
      if (appSaleService.getAppSale().getEnablePackManagement()
          && saleOrderLineService.isStartOfPackTypeLineQtyChanged(
              saleOrder.getSaleOrderLineList())) {
        this.updateProductQtyWithPackHeaderQty(saleOrder);
      }
    }
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderMarginService.computeMarginSaleOrder(saleOrder);
  }
}
