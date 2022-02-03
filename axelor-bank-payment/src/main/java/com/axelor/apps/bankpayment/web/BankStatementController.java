/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class BankStatementController {

  @HandleExceptionResponse
  public void runImport(ActionRequest request, ActionResponse response)
      throws IOException, AxelorException {
    BankStatement bankStatement = request.getContext().asType(BankStatement.class);

    BankStatementRepository bankStatementRepo = Beans.get(BankStatementRepository.class);
    BankStatementService bankStatementService = Beans.get(BankStatementService.class);
    bankStatement = bankStatementRepo.find(bankStatement.getId());
    bankStatementService.runImport(bankStatement, true);
    bankStatementService.checkImport(bankStatement);

    response.setReload(true);
  }

  @HandleExceptionResponse
  public void print(ActionRequest request, ActionResponse response) throws AxelorException {
    BankStatement bankStatement = request.getContext().asType(BankStatement.class);
    bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
    String name = bankStatement.getName();
    String fileLink = Beans.get(BankStatementService.class).print(bankStatement);
    response.setView(ActionView.define(name).add("html", fileLink).map());

    response.setReload(true);
  }

  @HandleExceptionResponse
  public void runBankReconciliation(ActionRequest request, ActionResponse response)
      throws IOException {

    BankStatement bankStatement = request.getContext().asType(BankStatement.class);
    bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
    List<BankReconciliation> bankReconciliationList =
        Beans.get(BankReconciliationCreateService.class).createAllFromBankStatement(bankStatement);

    if (bankReconciliationList != null) {
      response.setView(
          ActionView.define(I18n.get("Bank reconciliations"))
              .model(BankReconciliation.class.getName())
              .add("grid", "bank-reconciliation-grid")
              .add("form", "bank-reconciliation-form")
              .param("search-filters", "bank-reconciliation-filters")
              .domain(
                  "self.id in ("
                      + Joiner.on(",")
                          .join(
                              bankReconciliationList.stream()
                                  .map(BankReconciliation::getId)
                                  .toArray())
                      + ")")
              .map());
    }
    response.setReload(true);
  }
}
