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

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class BankStatementController {

  protected BankStatementService bankStatementService;
  protected BankStatementRepository bankStatementRepo;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repo;

  @Inject
  public BankStatementController(
      BankStatementService bankStatementService,
      BankStatementRepository bankStatementRepo,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repo) {

    this.bankStatementService = bankStatementService;
    this.bankStatementRepo = bankStatementRepo;
    this.bankStatementLineAFB120Repo = bankStatementLineAFB120Repo;
  }

  public void runImport(ActionRequest request, ActionResponse response) {
    try {
      boolean alreadyImported = false;
      BankStatement bankStatement = request.getContext().asType(BankStatement.class);
      List<BankStatementLineAFB120> initialLines;
      List<BankStatementLineAFB120> finalLines;
      BankStatementLineAFB120 finalBankStatementLineAFB120;
      BankStatementLineAFB120 initialBankStatementLineAFB120;
      BankStatementLineAFB120 tempBankStatementLineAFB120;
      BankDetails bankDetails;
      bankStatement = bankStatementRepo.find(bankStatement.getId());
      bankStatementService.runImport(bankStatement, true);

      // Load lines
      initialLines =
          bankStatementLineAFB120Repo
              .all()
              .filter(
                  "self.bankStatement = :bankStatement AND self.lineTypeSelect = :lineTypeSelect")
              .bind("bankStatement", bankStatement)
              .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
              .fetch();
      finalLines =
          bankStatementLineAFB120Repo
              .all()
              .filter(
                  "self.bankStatement = :bankStatement AND self.lineTypeSelect = :lineTypeSelect")
              .bind("bankStatement", bankStatement)
              .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
              .fetch();

      // Check doublons
      for (BankStatementLineAFB120 bslAFB120 : initialLines) {
        tempBankStatementLineAFB120 =
            bankStatementLineAFB120Repo
                .all()
                .filter(
                    "self.operationDate = :operationDate AND self.lineTypeSelect = :lineTypeSelect AND self.bankStatement != :bankStatement AND self.bankDetails = :bankDetails")
                .bind("operationDate", bslAFB120.getOperationDate())
                .bind("lineTypeSelect", bslAFB120.getLineTypeSelect())
                .bind("bankStatement", bslAFB120.getBankStatement())
                .bind("bankDetails", bslAFB120.getBankDetails())
                .fetchOne();
        if (ObjectUtils.notEmpty(tempBankStatementLineAFB120)) {
          alreadyImported = true;
          break;
        }
      }

      for (BankStatementLineAFB120 bslAFB120 : finalLines) {
        tempBankStatementLineAFB120 =
            bankStatementLineAFB120Repo
                .all()
                .filter(
                    "self.operationDate = :operationDate AND self.lineTypeSelect = :lineTypeSelect AND self.bankStatement != :bankStatement AND self.bankDetails = :bankDetails")
                .bind("operationDate", bslAFB120.getOperationDate())
                .bind("lineTypeSelect", bslAFB120.getLineTypeSelect())
                .bind("bankStatement", bslAFB120.getBankStatement())
                .bind("bankDetails", bslAFB120.getBankDetails())
                .fetchOne();
        if (ObjectUtils.notEmpty(tempBankStatementLineAFB120)) {
          alreadyImported = true;
          break;
        }
      }

      if (!alreadyImported) {
        // Check is following
        bankDetails =
            bankStatementLineAFB120Repo
                .all()
                .filter("self.bankStatement = :bankStatement")
                .bind("bankStatement", bankStatement)
                .order("-id")
                .fetchOne()
                .getBankDetails();

        initialBankStatementLineAFB120 =
            bankStatementLineAFB120Repo
                .all()
                .filter(
                    "self.bankStatement = :bankStatement AND self.lineTypeSelect = :lineTypeSelect")
                .bind("bankStatement", bankStatement)
                .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
                .order("sequence")
                .fetchOne();
        finalBankStatementLineAFB120 =
            bankStatementLineAFB120Repo
                .all()
                .filter(
                    "self.bankStatement != :bankStatement AND self.bankDetails = :bankDetails AND self.lineTypeSelect = :lineTypeSelect")
                .bind("bankStatement", bankStatement)
                .bind("bankDetails", bankDetails)
                .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
                .order("-id")
                .fetchOne();
        if (ObjectUtils.notEmpty(finalBankStatementLineAFB120))
          if (!(initialBankStatementLineAFB120
                  .getCredit()
                  .equals(finalBankStatementLineAFB120.getCredit())
              && initialBankStatementLineAFB120
                  .getDebit()
                  .equals(finalBankStatementLineAFB120.getDebit()))) {
            // delete imported
            response.setError(
                I18n.get(
                    "Current bank statement's initial balance does not match previous bank statement's final balance"));
            bankStatementService.deleteBankStatementLines(
                bankStatementRepo.find(bankStatement.getId()));
          }
      } else {
        // Delete imported
        response.setError(I18n.get("Bank statement already imported. Aborted."));
        bankStatementService.deleteBankStatementLines(
            bankStatementRepo.find(bankStatement.getId()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }

  public void print(ActionRequest request, ActionResponse response) {
    try {
      BankStatement bankStatement = request.getContext().asType(BankStatement.class);
      bankStatement = bankStatementRepo.find(bankStatement.getId());
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
      bankStatement = bankStatementRepo.find(bankStatement.getId());
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
