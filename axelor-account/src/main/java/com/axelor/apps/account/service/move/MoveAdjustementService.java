/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

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
   * Creating move of passage in gap regulation (on debit)
   *
   * @param debitMoveLine
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void createAdjustmentDebitMove(MoveLine debitMoveLine) throws AxelorException {

    Partner partner = debitMoveLine.getPartner();
    Account account = debitMoveLine.getAccount();
    Move debitMove = debitMoveLine.getMove();
    Company company = debitMove.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    BigDecimal debitAmountRemaining = debitMoveLine.getAmountRemaining();

    Journal miscOperationJournal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    Move adjustmentMove =
        moveCreateService.createMove(
            miscOperationJournal,
            company,
            null,
            partner,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            debitMove.getFunctionalOriginSelect(),
            null,
            null,
            debitMove.getCompanyBankDetails());

    // Création de la ligne au crédit
    MoveLine creditAdjustmentMoveLine =
        moveLineCreateService.createMoveLine(
            adjustmentMove,
            partner,
            account,
            debitAmountRemaining,
            false,
            appAccountService.getTodayDate(company),
            1,
            null,
            null);

    // Création de la ligne au debit
    MoveLine debitAdjustmentMoveLine =
        moveLineCreateService.createMoveLine(
            adjustmentMove,
            partner,
            accountConfigService.getCashPositionVariationAccount(accountConfig),
            debitAmountRemaining,
            true,
            appAccountService.getTodayDate(company),
            2,
            null,
            null);

    adjustmentMove.addMoveLineListItem(creditAdjustmentMoveLine);
    adjustmentMove.addMoveLineListItem(debitAdjustmentMoveLine);

    moveValidateService.accounting(adjustmentMove);
    moveRepository.save(adjustmentMove);
  }

  /**
   * Creating move of passage in gap regulation (on credit)
   *
   * @param debitMoveLine
   * @return
   * @throws AxelorException
   */
  public MoveLine createAdjustmentCreditMove(MoveLine debitMoveLine) throws AxelorException {

    Partner partner = debitMoveLine.getPartner();
    Account account = debitMoveLine.getAccount();
    Move debitMove = debitMoveLine.getMove();
    Company company = debitMove.getCompany();
    BigDecimal debitAmountRemaining = debitMoveLine.getAmountRemaining();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    Journal miscOperationJournal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    Move adjustmentMove =
        moveCreateService.createMove(
            miscOperationJournal,
            company,
            null,
            partner,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            debitMove.getFunctionalOriginSelect(),
            null,
            null,
            debitMove.getCompanyBankDetails());

    // Création de la ligne au crédit
    MoveLine creditAdjustmentMoveLine =
        moveLineCreateService.createMoveLine(
            adjustmentMove,
            partner,
            account,
            debitAmountRemaining,
            false,
            appAccountService.getTodayDate(company),
            1,
            null,
            null);

    // Création de la ligne au débit
    MoveLine debitAdjustmentMoveLine =
        moveLineCreateService.createMoveLine(
            adjustmentMove,
            partner,
            accountConfigService.getCashPositionVariationAccount(accountConfig),
            debitAmountRemaining,
            true,
            appAccountService.getTodayDate(company),
            2,
            null,
            null);

    adjustmentMove.addMoveLineListItem(creditAdjustmentMoveLine);
    adjustmentMove.addMoveLineListItem(debitAdjustmentMoveLine);
    moveValidateService.accounting(adjustmentMove);
    moveRepository.save(adjustmentMove);

    return creditAdjustmentMoveLine;
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
