package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface ForeignExchangeGapToolService {

  List<Integer> getForeignExchangeTypes();

  boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine);

  boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine);

  int getInvoicePaymentType(Reconcile reconcile);

  boolean checkCurrencies(MoveLine creditMoveLine, MoveLine debitMoveLine);

  boolean checkForeignExchangeAccounts(Company company) throws AxelorException;
}
