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
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class MoveAdjustementService {

  protected MoveLineCreateService moveLineCreateService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;

  @Inject
  public MoveAdjustementService(
      AppAccountService appAccountService,
      MoveLineCreateService moveLineCreateService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      AccountConfigService accountConfigService,
      MoveRepository moveRepository) {

    this.moveLineCreateService = moveLineCreateService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
  }

  /**
   * Creating move of passage in gap regulation
   *
   * @param moveLine
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine createAdjustmentMove(MoveLine moveLine, boolean isDebit) throws AxelorException {

    Partner partner = moveLine.getPartner();
    Account account = moveLine.getAccount();
    Move move = moveLine.getMove();
    Company company = move.getCompany();
    BigDecimal debitAmountRemaining = moveLine.getAmountRemaining();
    Currency currency = move.getCurrency();
    Currency companyCurrency = Beans.get(CompanyConfigService.class).getCompanyCurrency(company);
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Account cashPositionVariationAccount =
        accountConfigService.getCashPositionVariationAccount(accountConfig);
    LocalDate date = appAccountService.getTodayDate(company);

    Journal miscOperationJournal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    BigDecimal currencyRate =
        Beans.get(CurrencyService.class).getCurrencyConversionRate(currency, companyCurrency, date);

    BigDecimal amountRemainingInSpecificMoveCurrency =
        debitAmountRemaining.divide(
            currencyRate, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    Move adjustmentMove =
        moveCreateService.createMove(
            miscOperationJournal,
            company,
            currency,
            partner,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            move.getFunctionalOriginSelect(),
            null,
            null,
            move.getCompanyBankDetails());

    // Création de la ligne au crédit
    MoveLine creditAdjustmentMoveLine =
        moveLineCreateService.createMoveLine(
            adjustmentMove,
            partner,
            isDebit ? account : cashPositionVariationAccount,
            amountRemainingInSpecificMoveCurrency,
            false,
            date,
            1,
            null,
            null);

    // Création de la ligne au debit
    MoveLine debitAdjustmentMoveLine =
        moveLineCreateService.createMoveLine(
            adjustmentMove,
            partner,
            isDebit ? cashPositionVariationAccount : account,
            amountRemainingInSpecificMoveCurrency,
            true,
            date,
            2,
            null,
            null);

    adjustmentMove.addMoveLineListItem(creditAdjustmentMoveLine);
    adjustmentMove.addMoveLineListItem(debitAdjustmentMoveLine);

    moveValidateService.accounting(adjustmentMove);
    moveRepository.save(adjustmentMove);

    return isDebit ? creditAdjustmentMoveLine : debitAdjustmentMoveLine;
  }

  /**
   * Méthode permettant de créer une écriture du passage du compte de l'écriture au debit vers le
   * compte de l'écriture au credit.
   *
   * @param debitMoveLineToReconcile Ecriture au débit
   * @param creditMoveLineToReconcile Ecriture au crédit
   * @param amount Montant
   * @return L'écriture de passage du compte de l'écriture au debit vers le compte de l'écriture au
   *     credit.
   * @throws AxelorException
   */
  public Move createMoveToPassOnTheOtherAccount(
      MoveLine debitMoveLineToReconcile, MoveLine creditMoveLineToReconcile, BigDecimal amount)
      throws AxelorException {

    Partner partnerDebit = debitMoveLineToReconcile.getPartner();
    Partner partnerCredit = creditMoveLineToReconcile.getPartner();

    Company company = debitMoveLineToReconcile.getMove().getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    Journal journal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    // Move
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            null,
            partnerDebit,
            null,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            debitMoveLineToReconcile.getMove().getFunctionalOriginSelect(),
            null,
            null,
            debitMoveLineToReconcile.getMove().getCompanyBankDetails());

    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partnerCredit,
            creditMoveLineToReconcile.getAccount(),
            amount,
            true,
            appAccountService.getTodayDate(company),
            1,
            null,
            null);

    MoveLine creditMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partnerDebit,
            debitMoveLineToReconcile.getAccount(),
            amount,
            false,
            appAccountService.getTodayDate(company),
            2,
            null,
            null);

    move.addMoveLineListItem(debitMoveLine);
    move.addMoveLineListItem(creditMoveLine);

    moveValidateService.accounting(move);

    return move;
  }
}
