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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CommonInvoiceServiceImpl implements CommonInvoiceService {

  @Override
  public BigDecimal computeAmountToInvoicePercent(
      Model model, BigDecimal amount, boolean isPercent, BigDecimal total) throws AxelorException {
    if (total.compareTo(BigDecimal.ZERO) == 0) {
      if (amount.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
      } else {
        throw new AxelorException(
            model,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.SO_INVOICE_AMOUNT_MAX));
      }
    }
    if (!isPercent) {
      amount =
          amount
              .multiply(new BigDecimal("100"))
              .divide(total, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    }
    if (amount.compareTo(new BigDecimal("100")) > 0) {
      throw new AxelorException(
          model,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_AMOUNT_MAX));
    }

    return amount;
  }

  public BigDecimal computeSumInvoices(List<Invoice> invoices) {
    BigDecimal sumInvoices = BigDecimal.ZERO;
    for (Invoice invoice : invoices) {
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND
          || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
        sumInvoices = sumInvoices.subtract(invoice.getExTaxTotal());
      } else {
        sumInvoices = sumInvoices.add(invoice.getExTaxTotal());
      }
    }
    return sumInvoices;
  }
}
