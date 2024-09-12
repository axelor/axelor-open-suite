package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import java.util.ArrayList;
import java.util.List;

public class ForeignExchangeGapToolServiceImpl implements ForeignExchangeGapToolService {

  @Override
  public List<Integer> getForeignExchangeTypes() {
    return new ArrayList<>(
        List.of(
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN,
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS));
  }

  @Override
  public boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return this.isGain(creditMoveLine, debitMoveLine, this.isDebit(creditMoveLine, debitMoveLine));
  }

  protected boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit) {
    return isDebit
        ? creditMoveLine.getCurrencyRate().compareTo(debitMoveLine.getCurrencyRate()) > 0
        : debitMoveLine.getCurrencyRate().compareTo(creditMoveLine.getCurrencyRate()) < 0;
  }

  @Override
  public boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return debitMoveLine.getDate().isAfter(creditMoveLine.getDate());
  }

  @Override
  public int getInvoicePaymentType(Reconcile reconcile) {
    return this.isGain(reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine())
        ? InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN
        : InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS;
  }

  @Override
  public boolean checkCurrencies(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return debitMoveLine != null
        && creditMoveLine != null
        && !creditMoveLine.getCurrency().equals(creditMoveLine.getCompanyCurrency())
        && !debitMoveLine.getCurrency().equals(debitMoveLine.getCompanyCurrency())
        && creditMoveLine.getCurrency().equals(debitMoveLine.getCurrency());
  }

  @Override
  public boolean checkIsTotalPayment(
      Reconcile reconcile, MoveLine creditMoveLine, MoveLine debitMoveLine) {
    boolean paymentIsDebit = this.isDebit(creditMoveLine, debitMoveLine);

    return reconcile
            .getAmount()
            .compareTo(paymentIsDebit ? creditMoveLine.getCredit() : debitMoveLine.getDebit())
        == 0;
  }
}
