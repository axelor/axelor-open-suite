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
package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancePaymentRefundServiceImpl implements AdvancePaymentRefundService {

  protected InvoiceRepository invoiceRepository;

  @Inject
  public AdvancePaymentRefundServiceImpl(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  public BigDecimal getRefundPaidAmount(Invoice advancePayment) {
    List<Invoice> refundList = getRefundList(advancePayment);

    if (!ObjectUtils.isEmpty(refundList)) {
      return refundList.stream()
          .map(Invoice::getAmountPaid)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return BigDecimal.ZERO;
  }

  protected List<Invoice> getRefundList(Invoice advancePayment) {
    List<Invoice> refundList = new ArrayList<>();

    if (advancePayment == null) {
      return refundList;
    }

    String filter =
        "self.operationSubTypeSelect = :operationSubTypeSelect AND self.operationTypeSelect = :operationTypeSelect AND (self.originalInvoice = :originalInvoice OR :advancePaymentInvoice MEMBER OF self.advancePaymentInvoiceSet) AND self.amountPaid > 0 AND self.statusSelect = :statusSelect";
    Map<String, Object> params = new HashMap<>();
    Integer operationTypeSelect =
        advancePayment.getOperationTypeSelect()
                == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
            : InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
    params.put("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE);
    params.put("operationTypeSelect", operationTypeSelect);
    params.put("originalInvoice", advancePayment);
    params.put("advancePaymentInvoice", advancePayment.getId());
    params.put("statusSelect", InvoiceRepository.STATUS_VALIDATED);
    refundList = invoiceRepository.all().filter(filter).bind(params).fetch();

    return refundList;
  }
}
