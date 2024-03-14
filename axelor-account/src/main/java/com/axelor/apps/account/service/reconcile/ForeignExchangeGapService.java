package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

public interface ForeignExchangeGapService {

  Move manageForeignExchangeGap(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException;
}
