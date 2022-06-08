package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;

public class BankStatementRemoveServiceImpl implements BankStatementRemoveService {

  protected BankStatementRepository bankStatementRepo;
  protected BankStatementService bankStatementService;
  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;

  @Inject
  public BankStatementRemoveServiceImpl(
      BankStatementRepository bankStatementRepo,
      BankStatementService bankStatementService,
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationLineRepository bankReconciliationLineRepository) {
    this.bankStatementRepo = bankStatementRepo;
    this.bankStatementService = bankStatementService;
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
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
    this.checkIfCanRemoveBankStatement(bankStatement);
    List<BankDetails> detailList = bankStatementService.fetchBankDetailsList(bankStatement);
    bankStatementService.deleteBankStatementLines(bankStatement);
    bankStatementService.updateBankDetailsBalanceAndDate(detailList);
    bankStatementRepo.remove(bankStatement);
  }

  protected void checkIfCanRemoveBankStatement(BankStatement bankStatement) throws AxelorException {

    List<BankReconciliation> bankReconciliationList =
        bankReconciliationRepository
            .all()
            .filter("self.bankStatement.id = ?1 ", bankStatement.getId())
            .fetch();
    if (!ObjectUtils.isEmpty(bankReconciliationList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.BANK_STATEMENT_CANNOT_BE_REMOVED_BECAUSE_BANK_RECONCILIATION),
          bankReconciliationList.stream().map(it -> it.getName()).collect(Collectors.joining(",")));
    }

    List<BankStatementLineAFB120> bankStatementLineList =
        bankStatementService.getBankStatementLines(bankStatement);

    if (!ObjectUtils.isEmpty(bankStatementLineList)) {
      for (BankStatementLineAFB120 bankStatementLine : bankStatementLineList) {

        List<BankReconciliationLine> bankReconciliationLineList =
            bankReconciliationLineRepository
                .all()
                .filter("self.bankStatementLine.id = ?1 ", bankStatementLine.getId())
                .fetch();
        if (!ObjectUtils.isEmpty(bankReconciliationLineList)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  IExceptionMessage
                      .BANK_STATEMENT_CANNOT_BE_REMOVED_BECAUSE_BANK_RECONCILIATION_LINE),
              bankReconciliationLineList.stream()
                  .map(it -> it.getName())
                  .collect(Collectors.joining(",")));
        }
      }
    }
  }
}
