package com.axelor.apps.bankpayment.service.bankstatement.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportAbstractService;
import com.axelor.apps.bankpayment.service.bankstatementline.camt53.BankStatementLineCreateCAMT53Service;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public class BankStatementImportCAMT53Service extends BankStatementImportAbstractService {

  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;
  protected BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service;

  @Inject
  public BankStatementImportCAMT53Service(
      BankStatementRepository bankStatementRepository,
      BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service) {
    super(bankStatementRepository);
    this.bankStatementLineCreateCAMT53Service = bankStatementLineCreateCAMT53Service;
  }

  @Override
  @Transactional
  public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
    bankStatementLineCreateCAMT53Service.process(bankStatement);
  }

  @Override
  protected void checkImport(BankStatement bankStatement) throws AxelorException, IOException {}

  @Override
  protected void updateBankDetailsBalance(BankStatement bankStatement) {}
}
