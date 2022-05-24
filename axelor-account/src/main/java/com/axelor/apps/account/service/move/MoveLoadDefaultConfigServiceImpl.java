/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class MoveLoadDefaultConfigServiceImpl implements MoveLoadDefaultConfigService {

  protected FiscalPositionAccountService fiscalPositionAccountService;
  protected AccountingSituationService accountingSituationService;
  protected TaxService taxService;

  @Inject
  public MoveLoadDefaultConfigServiceImpl(
      FiscalPositionAccountService fiscalPositionAccountService,
      AccountingSituationService accountingSituationService,
      TaxService taxService) {
    this.fiscalPositionAccountService = fiscalPositionAccountService;
    this.accountingSituationService = accountingSituationService;
    this.taxService = taxService;
  }

  @Override
  public Account getAccountingAccountFromAccountConfig(Move move) {
    AccountingSituation accountSituation =
        accountingSituationService.getAccountingSituation(move.getPartner(), move.getCompany());
    Account accountingAccount = null;

    JournalType journalType = move.getJournal().getJournalType();
    if (journalType != null) {
      if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
        accountingAccount = accountSituation.getDefaultExpenseAccount();
      } else if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
        accountingAccount = accountSituation.getDefaultIncomeAccount();
      } else if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
        if (move.getPaymentMode() != null) {
          if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.IN)) {
            accountingAccount = accountSituation.getCustomerAccount();
          } else if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.OUT)) {
            accountingAccount = accountSituation.getSupplierAccount();
          }
        }
      }
    }
    if (move.getPartner().getFiscalPosition() != null) {
      accountingAccount =
          fiscalPositionAccountService.getAccount(
              move.getPartner().getFiscalPosition(), accountingAccount);
    }

    return accountingAccount;
  }

  @Override
  public TaxLine getTaxLine(Move move, MoveLine moveLine, Account accountingAccount)
      throws AxelorException {
    Tax tax;
    TaxLine taxLine;
    Partner partner = move.getPartner();
    if (accountingAccount == null || accountingAccount.getDefaultTax() == null) {
      return null;
    }
    tax = accountingAccount.getDefaultTax();

    if (!ObjectUtils.isEmpty(partner) && !ObjectUtils.isEmpty(partner.getFiscalPosition())) {
      tax = fiscalPositionAccountService.getTax(partner.getFiscalPosition(), tax);
    }

    taxLine = taxService.getTaxLine(tax, moveLine.getDate());
    return taxLine;
  }
}
