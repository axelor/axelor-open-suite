/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ForeignExchangeGapToolServiceImpl implements ForeignExchangeGapToolService {

  protected AccountConfigService accountConfigService;
  protected CurrencyService currencyService;

  @Inject
  public ForeignExchangeGapToolServiceImpl(
      AccountConfigService accountConfigService, CurrencyService currencyService) {
    this.accountConfigService = accountConfigService;
    this.currencyService = currencyService;
  }

  @Override
  public List<Integer> getForeignExchangeTypes() {
    return new ArrayList<>(
        List.of(
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN,
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS));
  }

  @Override
  public boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine) throws AxelorException {
    return this.isGain(creditMoveLine, debitMoveLine, this.isDebit(creditMoveLine, debitMoveLine));
  }

  protected boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit)
      throws AxelorException {
    BigDecimal creditCurrencyRate = this.getCurrencyRate(creditMoveLine, debitMoveLine);
    BigDecimal debitCurrencyRate = this.getCurrencyRate(debitMoveLine, creditMoveLine);

    return isDebit
        ? creditCurrencyRate.compareTo(debitCurrencyRate) > 0
        : debitCurrencyRate.compareTo(creditCurrencyRate) < 0;
  }

  @Override
  public boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return debitMoveLine.getDate().isAfter(creditMoveLine.getDate());
  }

  @Override
  public int getInvoicePaymentType(Reconcile reconcile) throws AxelorException {
    return this.isGain(reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine())
        ? InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN
        : InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS;
  }

  @Override
  public boolean checkCurrencies(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return debitMoveLine != null
        && creditMoveLine != null
        && (this.isSameForeignCurrency(creditMoveLine, debitMoveLine)
            || this.isSettledInCompanyCurrency(creditMoveLine, debitMoveLine));
  }

  protected boolean isSameForeignCurrency(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return this.isMultiCurrency(creditMoveLine)
        && this.isMultiCurrency(debitMoveLine)
        && creditMoveLine.getCurrency().equals(debitMoveLine.getCurrency());
  }

  protected boolean isSettledInCompanyCurrency(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    boolean isDebit = this.isDebit(creditMoveLine, debitMoveLine);
    MoveLine paymentMoveLine = isDebit ? debitMoveLine : creditMoveLine;
    MoveLine invoiceMoveLine = isDebit ? creditMoveLine : debitMoveLine;

    return !this.isMultiCurrency(paymentMoveLine) && this.isMultiCurrency(invoiceMoveLine);
  }

  @Override
  public boolean isMultiCurrency(MoveLine moveLine) {
    return !Objects.equals(moveLine.getCurrency(), moveLine.getCompanyCurrency());
  }

  @Override
  public BigDecimal getCurrencyRate(MoveLine moveLine, MoveLine otherMoveLine)
      throws AxelorException {
    if (this.isMultiCurrency(moveLine)) {
      return moveLine.getCurrencyRate();
    }

    return currencyService.getCurrencyConversionRate(
        otherMoveLine.getCurrency(), moveLine.getCompanyCurrency(), moveLine.getDate());
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
