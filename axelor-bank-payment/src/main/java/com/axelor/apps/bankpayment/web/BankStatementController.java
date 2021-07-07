/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.BankStatementRule;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.BankStatementRuleRepository;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class BankStatementController {

  protected BankStatementService bankStatementService;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected BankStatementRuleRepository bankStatementRuleRepository;
  protected MoveService moveService;
  protected PeriodService periodService;

  @Inject
  public BankStatementController(
      BankStatementService bankStatementService,
      BankStatementLineRepository bankStatementLineRepository,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      BankStatementRuleRepository bankStatementRuleRepository,
      MoveService moveService,
      PeriodService periodService) {

    this.bankStatementService = bankStatementService;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
    this.bankStatementRuleRepository = bankStatementRuleRepository;
    this.periodService = periodService;
    this.moveService = moveService;
  }

  public void runImport(ActionRequest request, ActionResponse response) {
    try {
      BankStatement bankStatement = request.getContext().asType(BankStatement.class);
      bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
      bankStatementService.runImport(bankStatement, true);
      Context scriptContext;
      Move move;
      List<BankStatementLineAFB120> bankStatementLines =
          bankStatementLineAFB120Repository
              .all()
              .filter(
                  "self.lineTypeSelect = :lineTypeSelect AND self.bankStatement = :bankStatement")
              .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT)
              .bind("bankStatement", bankStatement)
              .fetch();

      List<BankStatementRule> bankStatementRules;

      // Check periods
      List<LocalDate> datesWithoutPeriods = new ArrayList<LocalDate>();
      for (BankStatementLineAFB120 bankStatementLineAFB120 : bankStatementLines) {
        if (CollectionUtils.isEmpty(datesWithoutPeriods))
          datesWithoutPeriods.add(bankStatementLineAFB120.getValueDate());
        else if (!datesWithoutPeriods.contains(bankStatementLineAFB120.getValueDate())) {
          datesWithoutPeriods.add(bankStatementLineAFB120.getValueDate());
        }
      }

      for (int i = datesWithoutPeriods.size() - 1; i >= 0; i--) {
        if (periodService.getPeriod(
                datesWithoutPeriods.get(i),
                bankStatementLines.get(0).getBankDetails().getCompany(),
                YearRepository.TYPE_FISCAL)
            != null) {
          datesWithoutPeriods.remove(i);
        }
      }

      if (!datesWithoutPeriods.isEmpty()) {
        String message = I18n.get("Please configure periods for following dates :");
        for (LocalDate checkDate : datesWithoutPeriods) {
          message = message.concat(checkDate.toString() + ", ");
        }
        message = message.substring(0, message.length() - 2);
        response.setError(message);
      } else {

        for (BankStatementLineAFB120 bankStatementLineAFB120 : bankStatementLines) {
          scriptContext =
              new Context(
                  Mapper.toMap(bankStatementLineAFB120), BankStatementLineAFB120.class.getClass());
          bankStatementRules =
              bankStatementRuleRepository
                  .all()
                  .filter(
                      "self.ruleType = :ruleType AND self.accountManagement.interbankCodeLine = :interbankCodeLine")
                  .bind("ruleType", BankStatementRuleRepository.RULE_TYPE_ACCOUNTING_AUTO)
                  .bind(
                      "interbankCodeLine", bankStatementLineAFB120.getOperationInterbankCodeLine())
                  .fetch();
          for (BankStatementRule bankStatementRule : bankStatementRules) {
            if (Boolean.TRUE.equals(
                new GroovyScriptHelper(scriptContext)
                    .eval(
                        bankStatementRule
                            .getBankStatementQuery()
                            .getQuery()
                            .replaceAll("%s", "'" + bankStatementRule.getSearchLabel() + "'")))) {
              if (bankStatementRule.getAccountManagement().getJournal() == null) {
                response.setFlash(
                    String.format(
                        I18n.get("Please configure journal for account management %s"),
                        bankStatementRule.getAccountManagement()));
                continue;
              }
              move = bankStatementService.generateMove(bankStatementLineAFB120, bankStatementRule);
              System.err.println(move);
              if (move == null) {
                response.setError(
                    String.format(
                        I18n.get("Please configure period for date %s"),
                        bankStatementLineAFB120.getValueDate().toString()));
                continue;
              }
              moveService.getMoveValidateService().validate(move);
              break;
            }
          }
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
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
      TraceBackService.trace(response, e);
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
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }
}
