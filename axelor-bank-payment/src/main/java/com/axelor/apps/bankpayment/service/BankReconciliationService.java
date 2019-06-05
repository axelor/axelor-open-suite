/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BankReconciliationService {

  protected MoveService moveService;
  protected MoveRepository moveRepository;
  protected MoveLineService moveLineService;
  protected BankReconciliationRepository bankReconciliationRepository;

  @Inject
  public BankReconciliationService(
      MoveService moveService,
      MoveRepository moveRepository,
      MoveLineService moveLineService,
      BankReconciliationRepository bankReconciliationRepository) {

    this.moveService = moveService;
    this.moveRepository = moveRepository;
    this.moveLineService = moveLineService;
    this.bankReconciliationRepository = bankReconciliationRepository;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void compute(BankReconciliation bankReconciliation) throws AxelorException {

    BigDecimal computedBalance = bankReconciliation.getStartingBalance();

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {

      computedBalance = computedBalance.add(bankReconciliationLine.getAmount());
    }

    bankReconciliation.setComputedBalance(computedBalance);

    bankReconciliationRepository.save(bankReconciliation);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void validate(BankReconciliation bankReconciliation) throws AxelorException {

    this.checkBalance(bankReconciliation);

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {

      if (!bankReconciliationLine.getIsPosted()) {

        if (bankReconciliationLine.getMoveLine() == null) {
          this.validate(bankReconciliationLine);
        } else {
          this.checkAmount(bankReconciliationLine);
        }
      }
    }

    bankReconciliation.setStatusSelect(BankReconciliationRepository.STATUS_VALIDATED);

    bankReconciliationRepository.save(bankReconciliation);
  }

  public void checkBalance(BankReconciliation bankReconciliation) throws AxelorException {

    if (bankReconciliation.getComputedBalance().compareTo(bankReconciliation.getEndingBalance())
        != 0) {
      throw new AxelorException(
          bankReconciliation,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.BANK_STATEMENT_1),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION));
    }
  }

  public void validate(BankReconciliationLine bankReconciliationLine) throws AxelorException {

    BigDecimal amount = bankReconciliationLine.getAmount();

    // TODO add currency conversion

    if (amount.compareTo(BigDecimal.ZERO) == 0) {

      return;
    }

    BankReconciliation bankReconciliation = bankReconciliationLine.getBankReconciliation();

    Partner partner = bankReconciliationLine.getPartner();

    LocalDate effectDate = bankReconciliationLine.getEffectDate();

    String name = bankReconciliationLine.getName();
    String reference = bankReconciliationLine.getReference();

    Move move =
        moveService
            .getMoveCreateService()
            .createMove(
                bankReconciliation.getJournal(),
                bankReconciliation.getCompany(),
                null,
                partner,
                effectDate,
                null,
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    boolean isNegate = amount.compareTo(BigDecimal.ZERO) < 0;

    MoveLine partnerMoveLine =
        moveLineService.createMoveLine(
            move,
            partner,
            bankReconciliationLine.getAccount(),
            amount,
            isNegate,
            effectDate,
            effectDate,
            1,
            reference,
            name);
    move.addMoveLineListItem(partnerMoveLine);

    move.addMoveLineListItem(
        moveLineService.createMoveLine(
            move,
            partner,
            bankReconciliation.getCashAccount(),
            amount,
            !isNegate,
            effectDate,
            effectDate,
            1,
            reference,
            name));

    moveRepository.save(move);

    moveService.getMoveValidateService().validate(move);

    bankReconciliationLine.setMoveLine(partnerMoveLine);

    bankReconciliationLine.setIsPosted(true);
  }

  public void checkAmount(BankReconciliationLine bankReconciliationLine) throws AxelorException {

    MoveLine moveLine = bankReconciliationLine.getMoveLine();

    if (bankReconciliationLine.getAmount().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          bankReconciliationLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.BANK_STATEMENT_3),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          bankReconciliationLine.getReference());
    }

    if ((bankReconciliationLine.getAmount().compareTo(BigDecimal.ZERO) > 0
            && bankReconciliationLine.getAmount().compareTo(moveLine.getCredit()) != 0)
        || (bankReconciliationLine.getAmount().compareTo(BigDecimal.ZERO) < 0
            && bankReconciliationLine.getAmount().compareTo(moveLine.getDebit()) != 0)) {
      throw new AxelorException(
          bankReconciliationLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.BANK_STATEMENT_2),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          bankReconciliationLine.getReference());
    }
  }

  public BankReconciliation createBankReconciliation(
      Company company,
      LocalDate fromDate,
      LocalDate toDate,
      Currency currency,
      BankStatement bankStatement)
      throws IOException {

    BankReconciliation bankReconciliation = new BankReconciliation();
    bankReconciliation.setCompany(company);
    bankReconciliation.setFromDate(fromDate);
    bankReconciliation.setToDate(toDate);
    bankReconciliation.setCurrency(currency);
    bankReconciliation.setBankStatement(bankStatement);
    bankReconciliation.setName(this.computeName(bankReconciliation));

    return bankReconciliation;
  }

  private String computeName(BankReconciliation bankReconciliation) {

    String name = "";
    if (bankReconciliation.getCompany() != null) {
      name += bankReconciliation.getCompany().getCode();
    }
    if (bankReconciliation.getCurrency() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getCurrency().getCode();
    }
    if (bankReconciliation.getFromDate() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getFromDate().format(DateTimeFormatter.ofPattern("YYYY/MM/DD"));
    }
    if (bankReconciliation.getToDate() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getToDate().format(DateTimeFormatter.ofPattern("YYYY/MM/DD"));
    }

    return name;
  }
}
