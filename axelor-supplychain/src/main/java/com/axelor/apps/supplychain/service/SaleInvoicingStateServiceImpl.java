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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SaleInvoicingStateServiceImpl implements SaleInvoicingStateService {

  protected InvoiceLineRepository invoiceLineRepository;

  @Inject
  public SaleInvoicingStateServiceImpl(InvoiceLineRepository invoiceLineRepository) {
    this.invoiceLineRepository = invoiceLineRepository;
  }

  @Transactional
  @Override
  public void updateInvoicingState(SaleOrder saleOrder) {
    updateSaleOrderLinesInvoicingState(saleOrder.getSaleOrderLineList());
    saleOrder.setInvoicingState(computeSaleOrderInvoicingState(saleOrder));
  }

  @Override
  public int getSaleOrderLineInvoicingState(SaleOrderLine saleOrderLine) {
    int invoicingState = 0;

    BigDecimal amountInvoiced = saleOrderLine.getAmountInvoiced();
    BigDecimal exTaxTotal = saleOrderLine.getExTaxTotal();
    BigDecimal difference = exTaxTotal.subtract(amountInvoiced);

    if (difference.compareTo(BigDecimal.ZERO) == 0) {
      invoicingState = SALE_ORDER_INVOICE_INVOICED;
    }

    if (difference.compareTo(BigDecimal.ZERO) != 0) {
      invoicingState = SALE_ORDER_INVOICE_PARTIALLY_INVOICED;
    }

    if (amountInvoiced.compareTo(BigDecimal.ZERO) == 0) {
      if (atLeastOneInvoiceVentilated(saleOrderLine)
          && exTaxTotal.compareTo(BigDecimal.ZERO) == 0) {
        invoicingState = SALE_ORDER_INVOICE_INVOICED;
      } else {
        invoicingState = SALE_ORDER_INVOICE_NOT_INVOICED;
      }
    }

    return invoicingState;
  }

  protected boolean atLeastOneInvoiceVentilated(SaleOrderLine saleOrderLine) {
    return invoiceLineRepository
            .all()
            .filter(
                "self.saleOrderLine = :saleOrderLine AND self.invoice.statusSelect = :statusSelect")
            .bind("saleOrderLine", saleOrderLine.getId())
            .bind("statusSelect", InvoiceRepository.STATUS_VENTILATED)
            .count()
        > 0;
  }

  @Override
  public int computeSaleOrderInvoicingState(SaleOrder saleOrder) {

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    if (saleOrderLineList == null || saleOrderLineList.isEmpty()) {
      return SaleOrderRepository.INVOICING_STATE_NOT_INVOICED;
    }

    saleOrderLineList =
        saleOrderLineList.stream()
            .filter(
                saleOrderLine ->
                    saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL)
            .collect(Collectors.toList());

    if (saleOrderLineList.stream()
        .allMatch(
            saleOrderLine ->
                saleOrderLine.getInvoicingState()
                    == SaleOrderLineRepository.INVOICING_STATE_NOT_INVOICED)) {
      return SaleOrderRepository.INVOICING_STATE_NOT_INVOICED;
    }

    if (saleOrderLineList.stream()
        .allMatch(
            saleOrderLine ->
                saleOrderLine.getInvoicingState()
                    == SaleOrderLineRepository.INVOICING_STATE_INVOICED)) {
      return SaleOrderRepository.INVOICING_STATE_INVOICED;
    }

    return SaleOrderRepository.INVOICING_STATE_PARTIALLY_INVOICED;
  }

  @Transactional
  @Override
  public void updateSaleOrderLinesInvoicingState(List<SaleOrderLine> saleOrderLineList) {
    if (ObjectUtils.isEmpty(saleOrderLineList)) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL) {
        saleOrderLine.setInvoicingState(getSaleOrderLineInvoicingState(saleOrderLine));
      }
    }
  }
}
