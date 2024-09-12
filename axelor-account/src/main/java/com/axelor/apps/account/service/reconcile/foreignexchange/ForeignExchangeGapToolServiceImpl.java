package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import java.util.ArrayList;
import java.util.List;

public class ForeignExchangeGapToolServiceImpl implements ForeignExchangeGapToolService {

  @Override
  public List<Integer> getForeignExchangeTypes() {
    return new ArrayList<>();
  }

  @Override
  public boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return false;
  }

  protected boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit) {
    return false;
  }

  @Override
  public boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return false;
  }

  @Override
  public int getInvoicePaymentType(Reconcile reconcile) {
    return 0;
  }

  @Override
  public boolean checkCurrencies(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return false;
  }

  @Override
  public boolean checkIsTotalPayment(
      Reconcile reconcile, MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return false;
  }
}
