package com.axelor.apps.businessproject.service.subcontractortask;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.db.SubcontractorTask;
import java.util.List;

public interface SubcontractorTaskInvoiceService {
  List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<SubcontractorTask> subContractorTasks, int priority)
      throws AxelorException;

  List<InvoiceLine> createInvoiceLine(
      Invoice invoice, SubcontractorTask subContractorTask, int priority) throws AxelorException;
}
