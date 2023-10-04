/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MoveCounterPartServiceImpl implements MoveCounterPartService {

  protected MoveRepository moveRepository;
  protected MoveLineToolService moveLineToolService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected AccountingSituationService accountingSituationService;
  protected AccountConfigService accountConfigService;
  protected PaymentModeService paymentModeService;
  protected AccountManagementAccountService accountManagementAccountService;

  @Inject
  public MoveCounterPartServiceImpl(
      MoveRepository moveRepository,
      MoveLineToolService moveLineToolService,
      MoveLineCreateService moveLineCreateService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      AccountingSituationService accountingSituationService,
      AccountConfigService accountConfigService,
      AccountManagementAccountService accountManagementAccountService,
      PaymentModeService paymentModeService) {
    this.moveRepository = moveRepository;
    this.moveLineToolService = moveLineToolService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.accountingSituationService = accountingSituationService;
    this.accountConfigService = accountConfigService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.paymentModeService = paymentModeService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateCounterpartMoveLine(Move move, LocalDate singleTermDueDate)
      throws AxelorException {
    MoveLine counterPartMoveLine = createCounterpartMoveLine(move);
    if (counterPartMoveLine == null) {
      return;
    }
    move.addMoveLineListItem(counterPartMoveLine);
    moveLineInvoiceTermService.generateDefaultInvoiceTerm(
        counterPartMoveLine, singleTermDueDate, true);

    moveRepository.save(move);
  }

  @Override
  public MoveLine createCounterpartMoveLine(Move move) throws AxelorException {
    Account accountingAccount = getAccountingAccountFromJournal(move);
    BigDecimal amount = getCounterpartAmount(move);

    if (amount.signum() == 0) {
      return null;
    }

    boolean isDebit = amount.compareTo(BigDecimal.ZERO) > 0;
    BigDecimal currencyAmount = this.getCounterpartCurrencyAmount(move, isDebit);

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            accountingAccount,
            BigDecimal.ZERO,
            amount.abs(),
            BigDecimal.ZERO,
            isDebit,
            move.getDate(),
            move.getDate(),
            move.getOriginDate(),
            move.getMoveLineList().size() + 1,
            move.getOrigin(),
            move.getDescription());

    moveLine.setDueDate(move.getOriginDate());
    moveLine.setCurrencyAmount(currencyAmount);
    moveLine.setDescription(move.getDescription());
    moveLine.setCurrencyRate(
        move.getMoveLineList().stream()
            .map(MoveLine::getCurrencyRate)
            .findAny()
            .orElse(BigDecimal.ONE));

    return moveLine;
  }

  protected BigDecimal getCounterpartAmount(Move move) {
    BigDecimal amount = BigDecimal.ZERO;
    for (MoveLine line : move.getMoveLineList()) {
      amount = amount.add(line.getCredit());
      amount = amount.subtract(line.getDebit());
    }
    return amount;
  }

  protected BigDecimal getCounterpartCurrencyAmount(Move move, boolean isDebit) {
    return move.getMoveLineList().stream()
        .map(it -> this.getSignedCurrencyAmount(it, isDebit))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO)
        .abs();
  }

  protected BigDecimal getSignedCurrencyAmount(MoveLine moveLine, boolean isDebit) {
    return isDebit ? moveLine.getCurrencyAmount() : moveLine.getCurrencyAmount().negate();
  }

  protected Account getAccountingAccountFromJournal(Move move) throws AxelorException {
    Account accountingAccount = null;
    int technicalTypeSelect = move.getJournal().getJournalType().getTechnicalTypeSelect();
    if (technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
      accountingAccount =
          accountingSituationService.getSupplierAccount(move.getPartner(), move.getCompany());
    } else if (technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
      accountingAccount =
          accountingSituationService.getCustomerAccount(move.getPartner(), move.getCompany());
    } else if (technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
        && move.getPaymentMode() != null
        && move.getCompany() != null) {
      AccountManagement accountManagement =
          accountManagementAccountService.getAccountManagement(
              move.getPaymentMode().getAccountManagementList(), move.getCompany());
      if (ObjectUtils.notEmpty(accountManagement)) {
        accountingAccount =
            accountManagementAccountService.getCashAccount(
                accountManagement, move.getPaymentMode());
      }
    }
    return accountingAccount;
  }
}
