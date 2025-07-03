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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppSale;
import com.google.inject.Inject;

public class SaleOrderLineCheckServiceImpl implements SaleOrderLineCheckService {

  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderLineCheckServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public void productOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {}

  @Override
  public void qtyOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    checkOrderedQty(saleOrderLine);
  }

  @Override
  public void unitOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {}

  @Override
  public String checkParentLineType(SaleOrderLine parentSaleOrderLine) {
    if (parentSaleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_TITLE) {
      return I18n.get(SaleExceptionMessage.SALE_ORDER_LINE_PARENT_WRONG_TYPE);
    }
    return null;
  }

  protected void checkOrderedQty(SaleOrderLine saleOrderLine) throws AxelorException {
    AppSale appSale = appSaleService.getAppSale();
    boolean isSplitEnabled = appSale.getIsQuotationAndOrderSplitEnabled();

    if (!isSplitEnabled) {
      return;
    }

    if (saleOrderLine.getQty().compareTo(saleOrderLine.getOrderedQty()) < 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_QUOTATION_CHECK_ORDERED_QTY));
    }
  }
}
