package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.base.AxelorException;

public interface InvoiceJournalService {
  /**
   * Fetches the journal to apply to an invoice, based on the operationType and A.T.I amount
   *
   * @param invoice Invoice to fetch the journal for.
   * @return The suitable journal or null (!) if invoice's company is empty.
   * @throws AxelorException If operationTypeSelect is empty
   */
  Journal getJournal(Invoice invoice) throws AxelorException;
}
