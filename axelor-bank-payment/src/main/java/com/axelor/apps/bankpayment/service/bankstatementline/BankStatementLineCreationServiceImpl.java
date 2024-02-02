package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BankStatementLineCreationServiceImpl implements BankStatementLineCreationService {

  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;

  @Inject
  public BankStatementLineCreationServiceImpl(
      CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment) {
    this.currencyScaleServiceBankPayment = currencyScaleServiceBankPayment;
  }

  @Override
  public BankStatementLine createBankStatementLine(
      BankStatement bankStatement,
      int sequence,
      BankDetails bankDetails,
      BigDecimal debit,
      BigDecimal credit,
      Currency currency,
      String description,
      LocalDate operationDate,
      LocalDate valueDate,
      InterbankCodeLine operationInterbankCodeLine,
      InterbankCodeLine rejectInterbankCodeLine,
      String origin,
      String reference) {

    BankStatementLine bankStatementLine = new BankStatementLine();
    bankStatementLine.setBankStatement(bankStatement);
    bankStatementLine.setSequence(sequence);
    bankStatementLine.setBankDetails(bankDetails);
    bankStatementLine.setCurrency(currency);
    bankStatementLine.setDebit(
        currencyScaleServiceBankPayment.getScaledValue(bankStatementLine, debit));
    bankStatementLine.setCredit(
        currencyScaleServiceBankPayment.getScaledValue(bankStatementLine, credit));
    bankStatementLine.setDescription(description);
    bankStatementLine.setOperationDate(operationDate);
    bankStatementLine.setValueDate(valueDate);
    bankStatementLine.setOperationInterbankCodeLine(operationInterbankCodeLine);
    bankStatementLine.setRejectInterbankCodeLine(rejectInterbankCodeLine);
    bankStatementLine.setOrigin(origin);
    bankStatementLine.setReference(reference);

    // Used for Bank reconcile process
    bankStatementLine.setAmountRemainToReconcile(
        currencyScaleServiceBankPayment.getScaledValue(
            bankStatementLine, bankStatementLine.getDebit().add(bankStatementLine.getCredit())));

    return bankStatementLine;
  }
}
