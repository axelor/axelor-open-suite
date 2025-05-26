package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
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
  public void setMovePartnerBankDetails(MoveLineMassEntry moveLine) {
    super.setMovePartnerBankDetails(moveLine);
    PaymentMode paymentMode = moveLine.getMovePaymentMode();
    Partner partner = moveLine.getPartner();
    Company company = moveLine.getMoveMassEntry().getCompany();

    if (paymentMode != null && partner != null && company != null) {
      bankDetailsBankPaymentService
          .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
          .stream()
          .findAny()
          .ifPresent(moveLine::setMovePartnerBankDetails);
    }
  }
}
