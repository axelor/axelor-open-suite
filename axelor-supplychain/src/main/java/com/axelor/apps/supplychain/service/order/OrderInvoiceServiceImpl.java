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
package com.axelor.apps.supplychain.service.order;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class OrderInvoiceServiceImpl implements OrderInvoiceService {

  protected CommonInvoiceService commonInvoiceService;
  protected InvoiceRepository invoiceRepository;

  @Inject
  public OrderInvoiceServiceImpl(
      CommonInvoiceService commonInvoiceService, InvoiceRepository invoiceRepository) {
    this.commonInvoiceService = commonInvoiceService;
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  public BigDecimal amountToBeInvoiced(SaleOrder saleOrder) {
    return commonInvoiceService.computeSumInvoices(
        invoiceRepository
            .all()
            .filter(" self.saleOrder.id = :saleOrderId " + getCommonQuery())
            .bind(getInvoiceQueryParameters(saleOrder))
            .fetch());
  }

  @Override
  public BigDecimal amountToBeInvoiced(PurchaseOrder purchaseOrder) {
    return commonInvoiceService.computeSumInvoices(
        invoiceRepository
            .all()
            .filter(" self.purchaseOrder.id = :purchaseOrderId " + getCommonQuery())
            .bind(getInvoiceQueryParameters(purchaseOrder))
            .fetch());
  }

  protected String getCommonQuery() {
    return "AND self.statusSelect != :invoiceStatusCanceled "
        + "AND self.operationSubTypeSelect != :advanceOperationSubTypeSelect "
        + "AND (self.operationTypeSelect = :operationTypeSelect OR self.operationTypeSelect = :refundOperationTypeSelect)";
  }

  protected Map<String, Object> getInvoiceQueryParameters(SaleOrder saleOrder) {
    Map<String, Object> parameters = getInvoiceQueryCommonParameters();
    parameters.put("saleOrderId", saleOrder.getId());
    parameters.put("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    parameters.put("refundOperationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
    return parameters;
  }

  protected Map<String, Object> getInvoiceQueryParameters(PurchaseOrder purchaseOrder) {
    Map<String, Object> parameters = getInvoiceQueryCommonParameters();
    parameters.put("purchaseOrderId", purchaseOrder.getId());
    parameters.put("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);
    parameters.put("refundOperationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND);
    return parameters;
  }

  protected Map<String, Object> getInvoiceQueryCommonParameters() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("invoiceStatusCanceled", InvoiceRepository.STATUS_CANCELED);
    parameters.put("advanceOperationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE);
    return parameters;
  }
}
