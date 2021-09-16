package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface AccountingReportDas2Service {

  String printPreparatoryProcessDeclaration(AccountingReport accountingReport)
      throws AxelorException;

  MetaFile exportN4DSFile(AccountingReport accountingReport) throws AxelorException, IOException;

  boolean isThereAlreadyDas2ExportInPeriod(AccountingReport accountingReport);

  List<BigInteger> getAccountingReportDas2Pieces(AccountingReport accountingReport);

  AccountingReport getAssociatedDas2Export(AccountingReport accountingReport);
}
