/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.importer.Importer;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
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
  private int anomaly;

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
      throws IOException, AxelorException {
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
            anomaly++;
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
    if (anomaly > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_IMPORT_ERROR));
    }
    return addHistory(listener);
  }

  protected void linkLineToBankStatement(
      BankStatement bankStatement, List<BankStatementLine> bankStatementLineList) {
    if (bankStatement != null) {
      int i = 0;
      for (BankStatementLine bankStatementLine : bankStatementLineList) {
        linkLineToBankStatement(bankStatement, bankStatementLine);
        if (i % FETCH_LIMIT == 0) {
          JPA.clear();
        }
        i++;
      }
    }
  }

  @Transactional
  protected void linkLineToBankStatement(
      BankStatement bankStatement, BankStatementLine bankStatementLine) {
    bankStatementLine = bankStatementLineRepository.find(bankStatementLine.getId());
    bankStatement = bankStatementRepository.find(bankStatement.getId());
    bankStatementLine.setBankStatement(bankStatement);
    bankStatementDateService.updateBankStatementDate(
        bankStatement, bankStatementLine.getOperationDate(), bankStatementLine.getLineTypeSelect());
    bankStatementLineRepository.save(bankStatementLine);
  }

  @Override
  protected ImportHistory process(String bind, String data) throws IOException, AxelorException {
    return process(bind, data, null);
  }

  public void setBankStatement(BankStatement bankStatement) {
    this.bankStatement = bankStatement;
  }
}
