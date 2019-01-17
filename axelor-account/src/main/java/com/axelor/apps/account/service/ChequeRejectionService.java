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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ChequeRejection;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.ChequeRejectionRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ChequeRejectionService {

  protected MoveService moveService;
  protected MoveLineService moveLineService;
  protected SequenceService sequenceService;
  protected AccountConfigService accountConfigService;
  protected ChequeRejectionRepository chequeRejectionRepository;

  @Inject
  public ChequeRejectionService(
      MoveService moveService,
      MoveLineService moveLineService,
      SequenceService sequenceService,
      AccountConfigService accountConfigService,
      ChequeRejectionRepository chequeRejectionRepository) {

    this.moveService = moveService;
    this.moveLineService = moveLineService;
    this.sequenceService = sequenceService;
    this.accountConfigService = accountConfigService;
    this.chequeRejectionRepository = chequeRejectionRepository;
  }

  /**
   * procédure de validation du rejet de chèque
   *
   * @param chequeRejection Un rejet de chèque brouillon
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void validateChequeRejection(ChequeRejection chequeRejection) throws AxelorException {

    Company company = chequeRejection.getCompany();

    this.testCompanyField(company);

    this.setSequence(chequeRejection);

    Move move = this.createChequeRejectionMove(chequeRejection, company);

    chequeRejection.setMove(move);

    chequeRejection.setStatusSelect(ChequeRejectionRepository.STATUS_VALIDATED);

    chequeRejectionRepository.save(chequeRejection);
  }

  /**
   * Méthode permettant de créer une écriture de rejet de chèque (L'extourne de l'écriture de
   * paiement)
   *
   * @param chequeRejection Un rejet de cheque brouillon
   * @param company Une société
   * @return L'écriture de rejet de chèque
   * @throws AxelorException
   */
  public Move createChequeRejectionMove(ChequeRejection chequeRejection, Company company)
      throws AxelorException {
    this.testCompanyField(company);

    Journal journal = company.getAccountConfig().getRejectJournal();

    PaymentVoucher paymentVoucher = chequeRejection.getPaymentVoucher();

    Move paymentMove = paymentVoucher.getGeneratedMove();

    Partner partner = paymentVoucher.getPartner();

    InterbankCodeLine interbankCodeLine = chequeRejection.getInterbankCodeLine();

    String description = chequeRejection.getDescription();

    LocalDate rejectionDate = chequeRejection.getRejectionDate();

    // Move
    Move move =
        moveService
            .getMoveCreateService()
            .createMove(
                journal,
                company,
                null,
                partner,
                rejectionDate,
                null,
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    int ref = 1;

    for (MoveLine moveLine : paymentMove.getMoveLineList()) {

      if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
        // Debit MoveLine
        MoveLine debitMoveLine =
            moveLineService.createMoveLine(
                move,
                partner,
                moveLine.getAccount(),
                moveLine.getCredit(),
                true,
                rejectionDate,
                ref,
                chequeRejection.getName(),
                chequeRejection.getDescription());
        move.getMoveLineList().add(debitMoveLine);
        debitMoveLine.setInterbankCodeLine(interbankCodeLine);
        debitMoveLine.setDescription(description);

      } else {
        // Credit MoveLine
        MoveLine creditMoveLine =
            moveLineService.createMoveLine(
                move,
                partner,
                moveLine.getAccount(),
                moveLine.getDebit(),
                false,
                rejectionDate,
                ref,
                chequeRejection.getName(),
                chequeRejection.getDescription());
        move.getMoveLineList().add(creditMoveLine);
        creditMoveLine.setInterbankCodeLine(interbankCodeLine);
        creditMoveLine.setDescription(description);
      }

      ref++;
    }

    move.setRejectOk(true);

    moveService.getMoveValidateService().validate(move);

    return move;
  }

  /**
   * Procédure permettant de vérifier les champs d'une société
   *
   * @param company Une société
   * @throws AxelorException
   */
  public void testCompanyField(Company company) throws AxelorException {

    accountConfigService.getRejectJournal(accountConfigService.getAccountConfig(company));
  }

  /**
   * Procédure permettant d'assigner une séquence de rejet de chèque
   *
   * @param chequeRejection Un rejet de chèque
   * @throws AxelorException
   */
  public void setSequence(ChequeRejection chequeRejection) throws AxelorException {

    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.CHEQUE_REJECT, chequeRejection.getCompany());

    if (seq == null) {
      throw new AxelorException(
          chequeRejection,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CHECK_REJECTION_1),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          chequeRejection.getCompany().getName());
    }

    chequeRejection.setName(seq);
  }
}
