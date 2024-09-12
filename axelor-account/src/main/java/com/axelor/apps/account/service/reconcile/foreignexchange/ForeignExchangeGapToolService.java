package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import java.math.BigDecimal;
import java.util.List;

public interface ForeignExchangeGapToolService {

  List<Integer> getForeignExchangeTypes();

  boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine);

  boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine);

  int getInvoicePaymentType(Reconcile reconcile);

  boolean checkCurrencies(MoveLine creditMoveLine, MoveLine debitMoveLine);

  boolean checkIsTotalPayment(
      BigDecimal reconciledAmount, MoveLine creditMoveLine, MoveLine debitMoveLine);
}
