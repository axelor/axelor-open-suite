package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.base.db.BankDetails;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class BankStatementLineFetchServiceImpl implements BankStatementLineFetchService {

  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementLineFetchServiceImpl(
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  @Override
  public List<BankStatementLine> getBankStatementLines(BankStatement bankStatement) {
    return bankStatementLineRepository.findByBankStatement(bankStatement).fetch();
  }

  @Override
  public List<BankDetails> getBankDetailsFromStatementLines(BankStatement bankStatement) {
    return bankStatementLineRepository.findByBankStatement(bankStatement).fetch().stream()
        .map(BankStatementLine::getBankDetails)
        .distinct()
        .collect(Collectors.toList());
  }
}
