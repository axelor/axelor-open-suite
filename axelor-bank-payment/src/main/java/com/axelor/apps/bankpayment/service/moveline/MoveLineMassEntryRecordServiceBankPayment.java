package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.BankDetails;

public interface MoveLineMassEntryRecordServiceBankPayment {
  BankDetails checkMovePartnerBankDetails(MoveLineMassEntry moveLine, Move move);
}
