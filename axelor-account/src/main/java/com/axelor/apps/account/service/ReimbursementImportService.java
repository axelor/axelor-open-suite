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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReimbursementRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReimbursementImportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveService moveService;
  protected MoveRepository moveRepo;
  protected MoveLineService moveLineService;
  protected RejectImportService rejectImportService;
  protected AccountConfigService accountConfigService;
  protected ReimbursementRepository reimbursementRepo;

  @Inject
  public ReimbursementImportService(
      MoveService moveService,
      MoveRepository moveRepo,
      MoveLineService moveLineService,
      RejectImportService rejectImportService,
      AccountConfigService accountConfigService,
      ReimbursementRepository reimbursementRepo) {

    this.moveService = moveService;
    this.moveRepo = moveRepo;
    this.moveLineService = moveLineService;
    this.rejectImportService = rejectImportService;
    this.accountConfigService = accountConfigService;
    this.reimbursementRepo = reimbursementRepo;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void runReimbursementImport(Company company) throws AxelorException, IOException {

    this.testCompanyField(company);

    AccountConfig accountConfig = company.getAccountConfig();

    this.createReimbursementRejectMove(
        rejectImportService.getCFONBFile(
            accountConfig.getReimbursementImportFolderPathCFONB(),
            accountConfig.getTempReimbImportFolderPathCFONB(),
            company,
            0),
        company);
  }

  public void createReimbursementRejectMove(List<String[]> rejectList, Company company)
      throws AxelorException {
    int seq = 1;
    if (rejectList != null && !rejectList.isEmpty()) {
      LocalDate rejectDate = rejectImportService.createRejectDate(rejectList.get(0)[0]);
      Move move = this.createMoveReject(company, rejectDate);
      for (String[] reject : rejectList) {

        this.createReimbursementRejectMoveLine(reject, company, seq, move, rejectDate);
        seq++;
      }
      if (move != null) {
        // Création d'une ligne au débit
        MoveLine debitMoveLine =
            moveLineService.createMoveLine(
                move,
                null,
                company.getAccountConfig().getReimbursementAccount(),
                this.getTotalAmount(move),
                true,
                rejectDate,
                seq,
                null,
                null);
        move.getMoveLineList().add(debitMoveLine);
        this.validateMove(move);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public Reimbursement createReimbursementRejectMoveLine(
      String[] reject, Company company, int seq, Move move, LocalDate rejectDate)
      throws AxelorException {

    String refReject = reject[1];
    //	String amountReject = reject[2];
    InterbankCodeLine causeReject = rejectImportService.getInterbankCodeLine(reject[3], 0);
    MoveLineRepository moveLineRepo = Beans.get(MoveLineRepository.class);

    Reimbursement reimbursement =
        reimbursementRepo
            .all()
            .filter("UPPER(self.ref) = ?1 AND self.company = ?2", refReject, company)
            .fetchOne();
    if (reimbursement == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.REIMBURSEMENT_3),
          refReject,
          company.getName());
    }

    Partner partner = reimbursement.getPartner();
    BigDecimal amount = reimbursement.getAmountReimbursed();

    // Création de la ligne au crédit
    MoveLine creditMoveLine =
        moveLineService.createMoveLine(
            move,
            partner,
            company.getAccountConfig().getCustomerAccount(),
            amount,
            false,
            rejectDate,
            seq,
            refReject,
            null);
    move.getMoveLineList().add(creditMoveLine);

    moveLineRepo.save(creditMoveLine);

    moveRepo.save(move);
    creditMoveLine.setInterbankCodeLine(causeReject);

    reimbursement.setRejectedOk(true);
    reimbursement.setRejectDate(rejectDate);
    reimbursement.setRejectMoveLine(creditMoveLine);
    reimbursement.setInterbankCodeLine(causeReject);
    reimbursementRepo.save(reimbursement);

    return reimbursement;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Move createMoveReject(Company company, LocalDate date) throws AxelorException {
    return moveRepo.save(
        moveService
            .getMoveCreateService()
            .createMove(
                company.getAccountConfig().getRejectJournal(),
                company,
                null,
                null,
                date,
                null,
                MoveRepository.TECHNICAL_ORIGIN_IMPORT));
  }

  public BigDecimal getTotalAmount(Move move) {
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (MoveLine moveLine : move.getMoveLineList()) {
      totalAmount = totalAmount.add(moveLine.getCredit());
    }
    return totalAmount;
  }

  @Transactional(rollbackOn = {Exception.class})
  public MoveLine createOppositeRejectMoveLine(Move move, int seq, LocalDate rejectDate)
      throws AxelorException {
    // Création d'une ligne au débit
    MoveLine debitMoveLine =
        moveLineService.createMoveLine(
            move,
            null,
            move.getCompany().getAccountConfig().getReimbursementAccount(),
            this.getTotalAmount(move),
            true,
            rejectDate,
            seq,
            null,
            null);
    move.getMoveLineList().add(debitMoveLine);
    moveRepo.save(move);
    return debitMoveLine;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void validateMove(Move move) throws AxelorException {
    moveService.getMoveValidateService().validate(move);
    moveRepo.save(move);
  }

  @Transactional
  public void deleteMove(Move move) {
    moveRepo.remove(move);
  }

  /**
   * Procédure permettant de tester la présence des champs et des séquences nécessaire aux rejets de
   * remboursement.
   *
   * @param company Une société
   * @throws AxelorException
   */
  public void testCompanyField(Company company) throws AxelorException {
    log.debug("Test de la société {}", company.getName());

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    accountConfigService.getReimbursementAccount(accountConfig);
    accountConfigService.getRejectJournal(accountConfig);
    accountConfigService.getReimbursementImportFolderPathCFONB(accountConfig);
    accountConfigService.getTempReimbImportFolderPathCFONB(accountConfig);
  }
}
