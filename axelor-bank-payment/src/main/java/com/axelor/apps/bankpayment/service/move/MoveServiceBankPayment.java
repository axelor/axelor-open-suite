package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.BankDetails;

public interface MoveServiceBankPayment {
  BankDetails checkMovePartnerBankDetails(Move move);
}
