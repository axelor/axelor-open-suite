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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ChequeRejection;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.ChequeRejectionRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherCancelService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class ChequeRejectionService {

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveLineCreateService moveLineCreateService;
  protected SequenceService sequenceService;
  protected AccountConfigService accountConfigService;
  protected ChequeRejectionRepository chequeRejectionRepository;
  protected MoveReverseService moveReverseService;
  protected PaymentVoucherCancelService paymentVoucherCancelService;

  @Inject
  public ChequeRejectionService(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      SequenceService sequenceService,
      AccountConfigService accountConfigService,
      ChequeRejectionRepository chequeRejectionRepository,
      MoveReverseService moveReverseService,
      PaymentVoucherCancelService paymentVoucherCancelService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveLineCreateService = moveLineCreateService;
    this.sequenceService = sequenceService;
    this.accountConfigService = accountConfigService;
    this.chequeRejectionRepository = chequeRejectionRepository;
    this.moveReverseService = moveReverseService;
    this.paymentVoucherCancelService = paymentVoucherCancelService;
  }

  /**
   * procédure de validation du rejet de chèque
   *
   * @param chequeRejection Un rejet de chèque brouillon
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void validateChequeRejection(ChequeRejection chequeRejection) throws AxelorException {

    Company company = chequeRejection.getCompany();

    this.testCompanyField(company);

    this.setSequence(chequeRejection);

    if (chequeRejection.getPaymentVoucher().getGeneratedMove() != null) {
      Move move = this.createChequeRejectionMove(chequeRejection, company);

      chequeRejection.setMove(move);
    }

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

    InterbankCodeLine interbankCodeLine = chequeRejection.getInterbankCodeLine();

    String description = chequeRejection.getDescription();

    LocalDate rejectionDate = chequeRejection.getRejectionDate();

    Move move = moveReverseService.generateReverse(paymentMove, true, false, true, rejectionDate);

    move.setJournal(journal);
    move.setOrigin(chequeRejection.getName());
    move.setDescription(description);

    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setOrigin(chequeRejection.getName());
      moveLine.setDescription(description);
      moveLine.setInterbankCodeLine(interbankCodeLine);
    }

    move.setRejectOk(true);

    paymentVoucherCancelService.cancelPaymentVoucher(paymentVoucher);

    moveValidateService.accounting(move);

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
            SequenceRepository.CHEQUE_REJECT,
            chequeRejection.getCompany(),
            ChequeRejection.class,
            "name");

    if (seq == null) {
      throw new AxelorException(
          chequeRejection,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CHECK_REJECTION_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          chequeRejection.getCompany().getName());
    }

    chequeRejection.setName(seq);
  }
}
