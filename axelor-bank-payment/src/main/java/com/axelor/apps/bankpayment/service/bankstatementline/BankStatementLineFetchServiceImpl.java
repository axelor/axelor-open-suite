package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.base.db.BankDetails;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BankStatementLineFetchServiceImpl implements BankStatementLineFetchService {

  protected BankPaymentBankStatementLineAFB120Repository
      bankPaymentBankStatementLineAFB120Repository;
  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementLineFetchServiceImpl(
      BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository,
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankPaymentBankStatementLineAFB120Repository =
        bankPaymentBankStatementLineAFB120Repository;
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  @Override
  public List<BankStatementLine> getBankStatementLines(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines = new ArrayList<>();
    bankStatementLines.addAll(
        bankPaymentBankStatementLineAFB120Repository
            .all()
            .filter("self.bankStatement = :bankStatement")
            .bind("bankStatement", bankStatement)
            .fetch());
    bankStatementLines.addAll(
        bankStatementLineRepository.findByBankStatement(bankStatement).fetch());
    return bankStatementLines;
  }

  @Override
  public List<BankDetails> getBankDetailsFromStatementLines(BankStatement bankStatement) {
    return bankStatementLineRepository.findByBankStatement(bankStatement).fetch().stream()
        .map(BankStatementLine::getBankDetails)
        .distinct()
        .collect(Collectors.toList());
  }
}
