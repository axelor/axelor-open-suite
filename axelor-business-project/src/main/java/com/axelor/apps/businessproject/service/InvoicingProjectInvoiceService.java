package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;
import java.util.Set;

public interface InvoicingProjectInvoiceService {

  List<InvoiceLine> createCusInvFromSupInvLines(
      Invoice invoice, Set<InvoiceLine> invMoveLineSet, int priority) throws AxelorException;
}
