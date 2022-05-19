package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BankStatementRemoveServiceImpl implements BankStatementRemoveService {

  protected BankStatementRepository bankStatementRepo;
  protected BankStatementService bankStatementService;

  @Inject
  public BankStatementRemoveServiceImpl(
      BankStatementRepository bankStatementRepo, BankStatementService bankStatementService) {
    this.bankStatementRepo = bankStatementRepo;
    this.bankStatementService = bankStatementService;
  }

  @Override
  public int deleteMultiple(List<Long> idList) {
    int errorNB = 0;
    if (idList == null) {
      return errorNB;
    }
    BankStatement bankStatement;
    for (Long id : idList) {
      try {
        bankStatement = bankStatementRepo.find((Long) id);
        this.deleteStatement(bankStatement);
      } catch (Exception e) {
        TraceBackService.trace(e);
        errorNB += 1;
      } finally {
        JPA.clear();
      }
    }
    return errorNB;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void deleteStatement(BankStatement bankStatement) throws Exception {
    List<BankDetails> detailList = bankStatementService.fetchBankDetailsList(bankStatement);
    bankStatementService.deleteBankStatementLines(bankStatement);
    bankStatementService.updateBankDetailsBalanceAndDate(detailList);
    bankStatementRepo.remove(bankStatement);
  }
}
