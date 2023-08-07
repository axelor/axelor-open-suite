/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class InvoicePaymentController {

  private final int CALCULATION_SCALE = 10;

  public void computeAmountPaid(ActionRequest request, ActionResponse response) {
    try {
      InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);
      Map<String, Object> partialInvoice =
          (Map<String, Object>) request.getContext().get("_invoice");
      Invoice invoice =
          Beans.get(InvoiceRepository.class)
              .find(Long.valueOf(partialInvoice.get("id").toString()));
      if (invoice != null
          && invoicePayment != null
          && invoice.getCompanyInTaxTotal().compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal ratio =
            invoicePayment
                .getAmount()
                .divide(invoice.getCompanyInTaxTotal(), CALCULATION_SCALE, RoundingMode.HALF_UP);
        Beans.get(BudgetDistributionService.class).computePaidAmount(invoice, ratio);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
