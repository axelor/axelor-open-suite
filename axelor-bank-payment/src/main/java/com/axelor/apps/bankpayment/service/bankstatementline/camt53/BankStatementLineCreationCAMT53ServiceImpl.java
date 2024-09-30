package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineCAMT53;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineCAMT53Repository;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreationService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BankStatementLineCreationCAMT53ServiceImpl
    implements BankStatementLineCreationCAMT53Service {

  protected BankStatementLineCreationService bankStatementLineCreationService;

  @Inject
  public BankStatementLineCreationCAMT53ServiceImpl(
      BankStatementLineCreationService bankStatementLineCreationService) {
    this.bankStatementLineCreationService = bankStatementLineCreationService;
  }

  @Override
  public BankStatementLineCAMT53 createBankStatementLine(
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
      String reference,
      int lineType,
      Integer commissionExemptionIndexSelect) {
    BankStatementLine bankStatementLine =
        bankStatementLineCreationService.createBankStatementLine(
            bankStatement,
            sequence,
            bankDetails,
            debit,
            credit,
            currency,
            description,
            operationDate,
            valueDate,
            operationInterbankCodeLine,
            rejectInterbankCodeLine,
            origin,
            reference);
    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        Mapper.toBean(BankStatementLineCAMT53.class, Mapper.toMap(bankStatementLine));
    bankStatementLineCAMT53.setCommissionExemptionIndexSelect(commissionExemptionIndexSelect);
    bankStatementLineCAMT53.setLineTypeSelect(lineType);

    if (lineType != BankStatementLineCAMT53Repository.LINE_TYPE_MOVEMENT) {
      bankStatementLineCAMT53.setAmountRemainToReconcile(BigDecimal.ZERO);
    }
    return bankStatementLineCAMT53;
  }
}
