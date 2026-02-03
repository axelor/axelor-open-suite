package com.axelor.apps.businessproject.web;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoicePaymentController {

  public void setProjectStatusPaid(ActionRequest request, ActionResponse response) {
    InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);

    invoicePayment = Beans.get(InvoicePaymentRepository.class).find(invoicePayment.getId());

    try {
      Project project = invoicePayment.getInvoice().getProject();
      Beans.get(ProjectStatusChangeService.class).setPaidStatus(project);
    } catch (AxelorAlertException e) {
      TraceBackService.trace(response, e);
    }
  }
}
