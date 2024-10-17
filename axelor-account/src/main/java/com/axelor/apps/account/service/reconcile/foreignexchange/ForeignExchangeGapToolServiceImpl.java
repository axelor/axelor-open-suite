package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ForeignExchangeGapToolServiceImpl implements ForeignExchangeGapToolService {

  protected AccountConfigService accountConfigService;

  @Inject
  public ForeignExchangeGapToolServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

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
  public boolean checkForeignExchangeAccounts(Company company) throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Account gainsAccount = accountConfig.getForeignExchangeGainsAccount();
    Account lossesAccount = accountConfig.getForeignExchangeLossesAccount();

    if (gainsAccount == null && lossesAccount == null) {
      return false;
    }

    accountConfigService.getForeignExchangeAccount(accountConfig, true);
    accountConfigService.getForeignExchangeAccount(accountConfig, false);
    return true;
  }
}
