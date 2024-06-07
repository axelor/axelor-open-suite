package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import java.math.BigDecimal;
import java.util.List;

public interface ForeignExchangeGapToolsService {

  List<Integer> getForeignExchangeTypes();

  boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine);

  boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine);

  int getInvoicePaymentType(Reconcile reconcile);

  boolean checkCurrencies(MoveLine creditMoveLine, MoveLine debitMoveLine);

  boolean checkIsTotalPayment(
      BigDecimal reconcileAmount, MoveLine creditMoveLine, MoveLine debitMoveLine);
}
