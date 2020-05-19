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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTaxService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderComputeServiceSupplychainImpl extends SaleOrderComputeServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public SaleOrderComputeServiceSupplychainImpl(
      SaleOrderLineService saleOrderLineService, SaleOrderLineTaxService saleOrderLineTaxService) {

    super(saleOrderLineService, saleOrderLineTaxService);
  }

  @Override
  public void _computeSaleOrder(SaleOrder saleOrder) throws AxelorException {

    super._computeSaleOrder(saleOrder);

    int maxDelay = 0;

    if (saleOrder.getSaleOrderLineList() != null && !saleOrder.getSaleOrderLineList().isEmpty()) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

        if ((saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE
            || saleOrderLine.getSaleSupplySelect()
                == SaleOrderLineRepository.SALE_SUPPLY_PURCHASE)) {
          maxDelay =
              Integer.max(
                  maxDelay,
                  saleOrderLine.getStandardDelay() == null ? 0 : saleOrderLine.getStandardDelay());
        }
      }
    }
    saleOrder.setStandardDelay(maxDelay);

    if (Beans.get(AppAccountService.class).getAppAccount().getManageAdvancePaymentInvoice()) {
      saleOrder.setAdvanceTotal(computeTotalInvoiceAdvancePayment(saleOrder));
    }
    Beans.get(SaleOrderServiceSupplychainImpl.class)
        .updateAmountToBeSpreadOverTheTimetable(saleOrder);
  }

  protected BigDecimal computeTotalInvoiceAdvancePayment(SaleOrder saleOrder) {
    BigDecimal total = BigDecimal.ZERO;

    if (saleOrder.getId() == null) {
      return total;
    }

    List<Invoice> advancePaymentInvoiceList =
        Beans.get(InvoiceRepository.class)
            .all()
            .filter(
                "self.saleOrder.id = :saleOrderId AND self.operationSubTypeSelect = :operationSubTypeSelect")
            .bind("saleOrderId", saleOrder.getId())
            .bind("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
            .fetch();
    if (advancePaymentInvoiceList == null || advancePaymentInvoiceList.isEmpty()) {
      return total;
    }
    for (Invoice advance : advancePaymentInvoiceList) {
      total = total.add(advance.getAmountPaid());
    }
    return total;
  }
}
