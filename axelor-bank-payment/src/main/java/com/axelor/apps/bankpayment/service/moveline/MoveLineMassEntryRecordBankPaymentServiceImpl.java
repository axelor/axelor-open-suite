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
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;

public class MoveLineMassEntryRecordBankPaymentServiceImpl
    extends MoveLineMassEntryRecordServiceImpl
    implements MoveLineMassEntryRecordServiceBankPayment {
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

  @Override
  public BankDetails checkMovePartnerBankDetails(MoveLineMassEntry moveLine, Move move) {
    PaymentMode paymentMode = moveLine.getMovePaymentMode();
    Company company = move.getCompany();
    BankDetails movePartnerBankDetails = moveLine.getMovePartnerBankDetails();

    if (bankDetailsBankPaymentService.isBankDetailsNotLinkedToActiveUmr(
        paymentMode, company, movePartnerBankDetails)) {
      return null;
    }
    return movePartnerBankDetails;
  }
}
