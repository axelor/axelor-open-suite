/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.massentry.MassEntryToolService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordServiceImpl;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;

public class MoveLineMassEntryRecordBankPaymentServiceImpl
    extends MoveLineMassEntryRecordServiceImpl {
  protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveLineMassEntryRecordBankPaymentServiceImpl(
      MoveLineMassEntryService moveLineMassEntryService,
      MoveLineRecordService moveLineRecordService,
      TaxAccountToolService taxAccountToolService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      MassEntryToolService massEntryToolService,
      MoveLineTaxService moveLineTaxService,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      MoveLineToolService moveLineToolService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        moveLineMassEntryService,
        moveLineRecordService,
        taxAccountToolService,
        moveLoadDefaultConfigService,
        massEntryToolService,
        moveLineTaxService,
        analyticMoveLineRepository,
        moveLineToolService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public void setMovePartnerBankDetails(MoveLineMassEntry moveLine, Move move) {
    super.setMovePartnerBankDetails(moveLine, move);
    PaymentMode paymentMode = moveLine.getMovePaymentMode();
    Partner partner = moveLine.getPartner();
    Company company = move.getCompany();

    if (paymentMode != null
        && paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD
        && partner != null
        && company != null) {
      bankDetailsBankPaymentService
          .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
          .stream()
          .findAny()
          .ifPresent(moveLine::setMovePartnerBankDetails);
    }
  }
}
