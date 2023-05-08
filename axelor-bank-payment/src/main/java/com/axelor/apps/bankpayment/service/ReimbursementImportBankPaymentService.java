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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReimbursementRepository;
import com.axelor.apps.account.service.ReimbursementImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.BankDetailsService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class ReimbursementImportBankPaymentService extends ReimbursementImportService {

  protected RejectImportBankPaymentService rejectImportBankPaymentService;

  @Inject
  public ReimbursementImportBankPaymentService(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepo,
      MoveLineCreateService moveLineCreateService,
      RejectImportService rejectImportService,
      AccountConfigService accountConfigService,
      ReimbursementRepository reimbursementRepo,
      BankDetailsService bankDetailsService,
      RejectImportBankPaymentService rejectImportBankPaymentService) {
    super(
        moveCreateService,
        moveValidateService,
        moveRepo,
        moveLineCreateService,
        rejectImportService,
        accountConfigService,
        reimbursementRepo,
        bankDetailsService);
    this.rejectImportBankPaymentService = rejectImportBankPaymentService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Reimbursement createReimbursementRejectMoveLine(
      String[] reject, Company company, int seq, Move move, LocalDate rejectDate)
      throws AxelorException {
    Reimbursement reimbursement =
        super.createReimbursementRejectMoveLine(reject, company, seq, move, rejectDate);
    reimbursement.setInterbankCodeLine(
        rejectImportBankPaymentService.getInterbankCodeLine(reject[3], 0));
    return reimbursement;
  }

  @Override
  protected MoveLine createMoveLine(
      String[] reject,
      Company company,
      int seq,
      Move move,
      LocalDate rejectDate,
      Reimbursement reimbursement)
      throws AxelorException {
    MoveLine creditMoveLine =
        super.createMoveLine(reject, company, seq, move, rejectDate, reimbursement);
    creditMoveLine.setInterbankCodeLine(
        rejectImportBankPaymentService.getInterbankCodeLine(reject[3], 0));
    return creditMoveLine;
  }
}
