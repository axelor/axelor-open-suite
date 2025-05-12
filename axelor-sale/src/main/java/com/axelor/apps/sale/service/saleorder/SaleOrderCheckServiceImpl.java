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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderCheckServiceImpl implements SaleOrderCheckService {

  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderCheckServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public String finalizeCheckAlert(SaleOrder saleOrder) throws AxelorException {
    checkSaleOrderLineList(saleOrder);
    if (productSoldAtLoss(saleOrder)) {
      return I18n.get(SaleExceptionMessage.SALE_ORDER_FINALIZE_PRODUCT_SOLD_AT_LOSS);
    }

    if (priceListIsNotValid(saleOrder)) {
      return I18n.get(SaleExceptionMessage.SALE_ORDER_FINALIZE_PRICE_LIST_NOT_VALID);
    }

    return "";
  }

  @Override
  public List<String> confirmCheckAlert(SaleOrder saleOrder) throws AxelorException {
    List<String> alertList = new ArrayList<>();
    if (isTotalAmountZero(saleOrder)) {
      alertList.add(I18n.get(SaleExceptionMessage.SALE_ORDER_CONFIRM_TOTAL_AMOUNT_ZERO));
    }
    return alertList;
  }

  protected boolean isTotalAmountZero(SaleOrder saleOrder) {
    return saleOrder.getExTaxTotal().signum() == 0
        && CollectionUtils.isNotEmpty(saleOrder.getSaleOrderLineList());
  }

  @Override
  public void checkSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_FINALIZE_EMPTY_LIST));
    }
  }

  @Override
  public boolean productSoldAtLoss(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    return saleOrderLineList.stream().anyMatch(line -> line.getSubTotalGrossMargin().signum() < 0);
  }

  @Override
  public boolean priceListIsNotValid(SaleOrder saleOrder) {
    PriceList priceList = saleOrder.getPriceList();
    if (priceList == null) {
      return false;
    }
    LocalDate todayDate = appBaseService.getTodayDate(null);
    LocalDate priceListBeginDate = priceList.getApplicationBeginDate();
    LocalDate priceListEndDate = priceList.getApplicationEndDate();

    boolean beginDateNotValid =
        priceListBeginDate == null || priceListBeginDate.isBefore(todayDate);
    boolean endDateNotValid = priceListEndDate == null || priceListEndDate.isAfter(todayDate);
    return !priceList.getIsActive() || beginDateNotValid || endDateNotValid;
  }
}
