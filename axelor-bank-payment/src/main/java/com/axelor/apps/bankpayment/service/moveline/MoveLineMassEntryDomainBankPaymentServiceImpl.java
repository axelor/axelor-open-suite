package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryDomainServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class MoveLineMassEntryDomainBankPaymentServiceImpl
    extends MoveLineMassEntryDomainServiceImpl {
  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveLineMassEntryDomainBankPaymentServiceImpl(
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public String createDomainForMovePartnerBankDetails(
      Move move, MoveLineMassEntry moveLineMassEntry) {
    Partner partner = moveLineMassEntry.getPartner();
    String domain = super.createDomainForMovePartnerBankDetails(move, moveLineMassEntry);

    if (move != null && partner != null && !partner.getBankDetailsList().isEmpty()) {

      PaymentMode paymentMode = moveLineMassEntry.getMovePaymentMode();
      Company company = move.getCompany();
      List<BankDetails> bankDetailsList =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              paymentMode, partner, company);
      if (paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD) {
        domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
      }
    }
    return domain;
  }
}
