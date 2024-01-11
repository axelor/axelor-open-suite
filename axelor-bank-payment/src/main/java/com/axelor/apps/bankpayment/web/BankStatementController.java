/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementRemoveService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class BankStatementController {

  public void runImport(ActionRequest request, ActionResponse response) {
    try {
      BankStatement bankStatement = request.getContext().asType(BankStatement.class);

      BankStatementRepository bankStatementRepo = Beans.get(BankStatementRepository.class);
      BankStatementService bankStatementService = Beans.get(BankStatementService.class);
      bankStatement = bankStatementRepo.find(bankStatement.getId());
      bankStatementService.runImport(bankStatement, true);

    } catch (Exception e) {
      TraceBackService.trace(
          response, e, ExceptionOriginRepository.BANK_STATEMENT, ResponseMessageType.ERROR);
    }
    response.setReload(true);
  }

  public void print(ActionRequest request, ActionResponse response) {
    try {
      BankStatement bankStatement = request.getContext().asType(BankStatement.class);
      bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
      String name = bankStatement.getName();
      String fileLink = Beans.get(BankStatementService.class).print(bankStatement);
      response.setView(ActionView.define(name).add("html", fileLink).map());

    } catch (Exception e) {
      TraceBackService.trace(response, e, ExceptionOriginRepository.BANK_STATEMENT);
    }

    response.setReload(true);
  }

  public void runBankReconciliation(ActionRequest request, ActionResponse response) {

    try {
      BankStatement bankStatement = request.getContext().asType(BankStatement.class);
      bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
      List<BankReconciliation> bankReconciliationList =
          Beans.get(BankReconciliationCreateService.class)
              .createAllFromBankStatement(bankStatement);

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
    } catch (Exception e) {
      TraceBackService.trace(response, e, ExceptionOriginRepository.BANK_STATEMENT);
    }
    response.setReload(true);
  }

  @SuppressWarnings("unchecked")
  public void deleteMultipleStatements(ActionRequest request, ActionResponse response) {
    try {
      List<Long> statementIds =
          (List)
              (((List) request.getContext().get("_ids"))
                  .stream()
                      .filter(ObjectUtils::notEmpty)
                      .map(input -> Long.parseLong(input.toString()))
                      .collect(Collectors.toList()));
      if (!CollectionUtils.isEmpty(statementIds)) {
        BankStatementRemoveService bankStatementRemoveService =
            Beans.get(BankStatementRemoveService.class);
        if (statementIds.size() == 1) {
          bankStatementRemoveService.deleteStatement(
              Beans.get(BankStatementRepository.class).find(statementIds.get(0)));
        } else {
          int errorNB = bankStatementRemoveService.deleteMultiple(statementIds);
          if (errorNB > 0) {
            response.setInfo(
                String.format(
                    I18n.get(BankPaymentExceptionMessage.STATEMENT_REMOVE_NOT_OK_NB), errorNB));
          } else {
            response.setInfo(I18n.get(BankPaymentExceptionMessage.STATEMENT_REMOVE_OK));
            response.setReload(true);
          }
        }
      } else response.setInfo(I18n.get(BankPaymentExceptionMessage.NO_STATEMENT_TO_REMOVE));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(
          response, e, ExceptionOriginRepository.BANK_STATEMENT, ResponseMessageType.ERROR);
    }
  }

  public void deleteStatement(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      BankStatement bankStatement = request.getContext().asType(BankStatement.class);
      bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());

      Beans.get(BankStatementRemoveService.class).deleteStatement(bankStatement);

      response.setView(
          ActionView.define(I18n.get("Bank Statements"))
              .model(BankStatement.class.getName())
              .add("grid", "bank-statement-grid")
              .add("form", "bank-statement-form")
              .map());
      response.setCanClose(true);

    } catch (Exception e) {
      TraceBackService.trace(
          response, e, ExceptionOriginRepository.BANK_STATEMENT, ResponseMessageType.ERROR);
    }
  }
}
