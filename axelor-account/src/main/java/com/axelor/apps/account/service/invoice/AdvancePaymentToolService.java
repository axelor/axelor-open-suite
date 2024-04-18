package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface AdvancePaymentToolService {
  /**
   * Return the move lines from the advance payments on sale orders
   *
   * @param invoice
   * @return
   */
  List<MoveLine> getMoveLinesFromAdvancePayments(Invoice invoice) throws AxelorException;

  /**
   * Return the move lines from the advance payments from previous invoices
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  List<MoveLine> getMoveLinesFromInvoiceAdvancePayments(Invoice invoice) throws AxelorException;

  /**
   * Return the move line from the advance payment from related sale order lines.
   *
   * @param invoice
   * @return
   */
  List<MoveLine> getMoveLinesFromSOAdvancePayments(Invoice invoice);
}
