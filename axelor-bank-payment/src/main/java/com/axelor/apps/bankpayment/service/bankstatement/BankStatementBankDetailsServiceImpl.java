package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BankStatementBankDetailsServiceImpl implements BankStatementBankDetailsService {

  protected BankStatementLineFetchService bankStatementLineFetchService;

  @Inject
  public BankStatementBankDetailsServiceImpl(
      BankStatementLineFetchService bankStatementLineFetchService) {
    this.bankStatementLineFetchService = bankStatementLineFetchService;
  }

  @Transactional
  @Override
  public void updateBankDetailsBalance(BankStatement bankStatement) {
    List<BankDetails> bankDetailsList =
        bankStatementLineFetchService.getBankDetailsFromStatementLines(bankStatement);
    if (!ObjectUtils.isEmpty(bankDetailsList)) {
      for (BankDetails bankDetails : bankDetailsList) {
        BankStatementLine finalBankStatementLine =
            bankStatementLineFetchService
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement, bankDetails, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
                .order("-operationDate")
                .order("-sequence")
                .fetchOne();
        bankDetails.setBalance(
            (finalBankStatementLine.getCredit().subtract(finalBankStatementLine.getDebit())));
        bankDetails.setBalanceUpdatedDate(finalBankStatementLine.getOperationDate());
      }
    }
  }
}
