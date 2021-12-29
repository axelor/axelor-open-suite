/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service.response.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class InvoiceResponseGenerator extends ResponseGenerator {

  @Inject InvoiceRepository invoiceRepo;

  @Override
  public void init() {
    modelFields.addAll(
        Arrays.asList(
            "id",
            "invoiceId",
            "address",
            "addressStr",
            "amountRemaining",
            "currency",
            "dueDate",
            "exTaxTotal",
            "inTaxTotal",
            "invoiceDate",
            "invoiceLineList",
            "invoiceLineTaxList",
            "invoicePaymentList",
            "partner",
            "sequence",
            "statusSelect",
            "taxTotal",
            "validatedDate",
            "ventilatedDate"));

    extraFieldMap.put("invoiceSeq", this::getInvoiceSeq);
    extraFieldMap.put("_discountTotal", this::getDiscountTotal);
    extraFieldMap.put("orders", this::getSaleOrders);
    classType = Invoice.class;
  }

  private String getInvoiceSeq(Object object) {
    Invoice invoice = (Invoice) object;
    return invoice.getInvoiceId();
  }

  private BigDecimal getDiscountTotal(Object object) {
    Invoice invoice = (Invoice) object;
    invoice = invoiceRepo.find(invoice.getId());
    BigDecimal sum = BigDecimal.ZERO;
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      BigDecimal totalWTDiscount = invoiceLine.getPrice().multiply(invoiceLine.getQty());
      BigDecimal totalInDiscount = invoiceLine.getExTaxTotal();
      sum = sum.add(totalWTDiscount.subtract(totalInDiscount));
    }
    return sum.setScale(2, RoundingMode.HALF_EVEN);
  }

  private List<Map<String, Object>> getSaleOrders(Object object) {
    Invoice invoice = (Invoice) object;
    Set<SaleOrder> orders = new HashSet<>();
    List<InvoiceLine> lines = invoice.getInvoiceLineList();
    if (invoice.getSaleOrder() != null) {
      orders.add(invoice.getSaleOrder());
    }
    lines.stream()
        .filter(it -> it.getSaleOrderLine() != null)
        .map(InvoiceLine::getSaleOrderLine)
        .map(SaleOrderLine::getSaleOrder)
        .filter(Objects::nonNull)
        .forEach(orders::add);
    List<Map<String, Object>> dataList = new ArrayList<>();
    for (SaleOrder order : orders) {
      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put("id", order.getId());
      dataMap.put("sequence", order.getSaleOrderSeq());
      dataList.add(dataMap);
    }
    return dataList;
  }
}
