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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public interface PurchaseOrderInvoiceService {

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice generateInvoice(PurchaseOrder purchaseOrder) throws AxelorException;

  public Invoice createInvoice(PurchaseOrder purchaseOrder) throws AxelorException;

  public InvoiceGenerator createInvoiceGenerator(PurchaseOrder purchaseOrder)
      throws AxelorException;

  public InvoiceGenerator createInvoiceGenerator(PurchaseOrder purchaseOrder, boolean isRefund)
      throws AxelorException;

  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList) throws AxelorException;

  public List<InvoiceLine> createInvoiceLine(Invoice invoice, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException;

  public BigDecimal getInvoicedAmount(PurchaseOrder purchaseOrder);

  public BigDecimal getInvoicedAmount(
      PurchaseOrder purchaseOrder, Long currentInvoiceId, boolean excludeCurrentInvoice);
}
