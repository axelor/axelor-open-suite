package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.db.InvoicingProject;

public interface ProjectGenerateInvoiceService {

  Invoice generateInvoice(InvoicingProject invoicingProject) throws AxelorException;
}
