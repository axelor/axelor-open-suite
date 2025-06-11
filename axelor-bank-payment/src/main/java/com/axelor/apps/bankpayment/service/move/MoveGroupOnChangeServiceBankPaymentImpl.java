package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.record.MoveGroupOnChangeServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.google.inject.Inject;
import java.util.Map;

public class MoveGroupOnChangeServiceBankPaymentImpl extends MoveGroupOnChangeServiceImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveGroupOnChangeServiceBankPaymentImpl(
      MoveRecordSetService moveRecordSetService,
      MoveAttrsService moveAttrsService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(moveRecordSetService, moveAttrsService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public Map<String, Object> getPaymentModeOnChangeValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = super.getPaymentModeOnChangeValuesMap(move);
    BankDetails currentPartnerBankDetails = move.getPartnerBankDetails();
    PaymentMode paymentMode = move.getPaymentMode();

    if (bankDetailsBankPaymentService.isBankDetailsNotLinkedToActiveUmr(
        paymentMode, move.getCompany(), currentPartnerBankDetails)) {
      valuesMap.put("partnerBankDetails", null);
    }

    return valuesMap;
  }
}
