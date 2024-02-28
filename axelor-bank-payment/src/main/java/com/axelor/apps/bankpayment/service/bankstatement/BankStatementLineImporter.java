package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.importer.Importer;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankStatementLineImporter extends Importer {

  protected final int FETCH_LIMIT = 10;
  protected final BankStatementLineRepository bankStatementLineRepository;
  protected final BankStatementRepository bankStatementRepository;
  protected final BankStatementDateService bankStatementDateService;
  protected List<BankStatementLine> bankStatementLineList = new ArrayList<>();
  private BankStatement bankStatement;

  @Inject
  public BankStatementLineImporter(
      BankStatementLineRepository bankStatementLineRepository,
      BankStatementRepository bankStatementRepository,
      BankStatementDateService bankStatementDateService,
      BankStatement bankStatement) {
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementDateService = bankStatementDateService;
    this.bankStatement = bankStatement;
  }

  @Override
  protected ImportHistory process(String bind, String data, Map<String, Object> importContext)
      throws IOException {
    CSVImporter importer = new CSVImporter(bind, data);

    ImporterListener listener =
        new ImporterListener(getConfiguration().getName()) {
          @Override
          public void handle(Model bean, Exception e) {
            if (bankStatement != null) {
              Throwable rootCause = Throwables.getRootCause(e);
              TraceBackService.trace(
                  new AxelorException(
                      rootCause,
                      bankStatement,
                      TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                      rootCause.getMessage()));
            }
            super.handle(bean, e);
          }

          @Override
          public void imported(Integer total, Integer success) {
            try {
              linkLineToBankStatement(bankStatement, bankStatementLineList);
            } catch (Exception e) {
              this.handle(null, e);
            }
            super.imported(total, success);
          }

          @Override
          public void imported(Model bean) {
            if (bean.getClass().equals(BankStatementLine.class)) {
              BankStatementLine bankStatementLine = (BankStatementLine) bean;
              bankStatementLineList.add(bankStatementLine);
            }
            super.imported(bean);
          }
        };

    importer.addListener(listener);
    if (importContext == null) {
      importContext = new HashMap<>();
    }
    setBankStatement(bankStatementRepository.find(bankStatement.getId()));
    importContext.put("BankStatement", bankStatement);
    importer.setContext(importContext);
    importer.run();
    return addHistory(listener);
  }

  @Transactional
  protected void linkLineToBankStatement(
      BankStatement bankStatement, List<BankStatementLine> bankStatementLineList) {
    if (bankStatement != null) {
      int i = 0;
      for (BankStatementLine bankStatementLine : bankStatementLineList) {
        bankStatementLine = bankStatementLineRepository.find(bankStatementLine.getId());
        bankStatement = bankStatementRepository.find(bankStatement.getId());
        bankStatementLine.setBankStatement(bankStatement);
        bankStatementDateService.updateBankStatementDate(
            bankStatement,
            bankStatementLine.getOperationDate(),
            bankStatementLine.getLineTypeSelect());
        bankStatementLineRepository.save(bankStatementLine);
        if (i % FETCH_LIMIT == 0) {
          JPA.clear();
        }
        i++;
      }
    }
  }

  @Override
  protected ImportHistory process(String bind, String data) throws IOException {
    return process(bind, data, null);
  }

  public void setBankStatement(BankStatement bankStatement) {
    this.bankStatement = bankStatement;
  }
}
