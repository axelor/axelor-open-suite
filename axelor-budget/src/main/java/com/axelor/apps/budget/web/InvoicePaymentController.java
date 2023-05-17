package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetBudgetDistributionService;
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
        Beans.get(BudgetBudgetDistributionService.class).computePaidAmount(invoice, ratio);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
