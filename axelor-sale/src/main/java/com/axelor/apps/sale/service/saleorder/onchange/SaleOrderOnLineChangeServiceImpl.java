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
package com.axelor.apps.sale.service.saleorder.onchange;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGlobalDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.db.EntityHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class SaleOrderOnLineChangeServiceImpl implements SaleOrderOnLineChangeService {
  protected AppSaleService appSaleService;
  protected SaleOrderService saleOrderService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderLinePackService saleOrderLinePackService;
  protected SaleOrderComplementaryProductService saleOrderComplementaryProductService;
  protected SaleOrderGlobalDiscountService saleOrderGlobalDiscountService;

  @Inject
  public SaleOrderOnLineChangeServiceImpl(
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderComplementaryProductService saleOrderComplementaryProductService,
      SaleOrderGlobalDiscountService saleOrderGlobalDiscountService) {
    this.appSaleService = appSaleService;
    this.saleOrderService = saleOrderService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderLinePackService = saleOrderLinePackService;
    this.saleOrderComplementaryProductService = saleOrderComplementaryProductService;
    this.saleOrderGlobalDiscountService = saleOrderGlobalDiscountService;
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
        saleOrderLineComputeService.updateProductQty(SOLine, saleOrder, oldQty, newQty);
      }
    }
    return saleOrder;
  }

  @Override
  public String onLineChange(SaleOrder saleOrder) throws AxelorException {
    String message = "";
    saleOrderComplementaryProductService.handleComplementaryProducts(saleOrder);
    if (saleOrder.getSaleOrderLineList() != null && !saleOrder.getSaleOrderLineList().isEmpty()) {
      if (saleOrder.getSaleOrderLineList().stream()
              .anyMatch(
                  saleOrderLine ->
                      saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK)
          && appSaleService.getAppSale().getEnablePackManagement()
          && saleOrderLinePackService.isStartOfPackTypeLineQtyChanged(
              saleOrder.getSaleOrderLineList())) {
        this.updateProductQtyWithPackHeaderQty(saleOrder);
      }
      if (appSaleService.getAppBase().getIsGlobalDiscountEnabled()) {
        saleOrderGlobalDiscountService.applyGlobalDiscountOnLines(saleOrder);
      }
    } else {
      saleOrder.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
      saleOrder.setDiscountAmount(BigDecimal.ZERO);
    }
    saleOrderLineComputeService.computeLevels(saleOrder.getSaleOrderLineList(), null);
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderMarginService.computeMarginSaleOrder(saleOrder);
    return message;
  }
}
