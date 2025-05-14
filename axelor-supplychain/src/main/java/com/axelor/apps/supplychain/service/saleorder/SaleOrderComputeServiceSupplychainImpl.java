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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineCreateTaxLineService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.AdvancePaymentRefundService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderComputeServiceSupplychainImpl extends SaleOrderComputeServiceImpl {

  protected AdvancePaymentRefundService refundService;
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public SaleOrderComputeServiceSupplychainImpl(
      SaleOrderLineCreateTaxLineService saleOrderLineCreateTaxLineService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SubSaleOrderLineComputeService subSaleOrderLineComputeService,
      AdvancePaymentRefundService refundService,
      AppSaleService appSaleService) {
    super(
        saleOrderLineCreateTaxLineService,
        saleOrderLineComputeService,
        saleOrderLinePackService,
        subSaleOrderLineComputeService,
        appSaleService);
    this.refundService = refundService;
  }

  @Override
  public void _computeSaleOrder(SaleOrder saleOrder) throws AxelorException {

    super._computeSaleOrder(saleOrder);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return;
    }

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
    Beans.get(SaleOrderSupplychainService.class).updateAmountToBeSpreadOverTheTimetable(saleOrder);
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
                "self.saleOrder.id = :saleOrderId AND self.operationSubTypeSelect = :operationSubTypeSelect AND self.operationTypeSelect = :operationTypeSelect")
            .bind("saleOrderId", saleOrder.getId())
            .bind("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
            .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
            .fetch();
    if (advancePaymentInvoiceList == null || advancePaymentInvoiceList.isEmpty()) {
      return total;
    }
    for (Invoice advance : advancePaymentInvoiceList) {
      BigDecimal advancePaymentAmount = advance.getAmountPaid();
      advancePaymentAmount =
          advancePaymentAmount.subtract(refundService.getRefundPaidAmount(advance));
      total = total.add(advancePaymentAmount);
    }
    return total;
  }
}
