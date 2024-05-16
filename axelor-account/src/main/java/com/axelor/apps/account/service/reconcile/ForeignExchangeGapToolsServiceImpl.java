package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import java.util.ArrayList;
import java.util.List;

public class ForeignExchangeGapToolsServiceImpl implements ForeignExchangeGapToolsService {

  @Override
  public List<Integer> getForeignExchangeTypes() {
    return new ArrayList<>(
        List.of(
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN,
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS));
  }

  @Override
  public boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit) {
    return isDebit
        ? creditMoveLine.getCurrencyRate().compareTo(debitMoveLine.getCurrencyRate()) > 0
        : debitMoveLine.getCurrencyRate().compareTo(creditMoveLine.getCurrencyRate()) < 0;
  }

  @Override
  public boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return this.isGain(creditMoveLine, debitMoveLine, this.isDebit(creditMoveLine, debitMoveLine));
  }

  @Override
  public boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return debitMoveLine.getDate().isAfter(creditMoveLine.getDate());
  }

  @Override
  public int getInvoicePaymentType(Reconcile reconcile) {
    boolean isGain = this.isGain(reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine());

    return isGain
        ? InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN
        : InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS;
  }
}
