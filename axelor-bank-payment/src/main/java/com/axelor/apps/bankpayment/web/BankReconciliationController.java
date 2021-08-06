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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationValidateService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.EntityHelper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class BankReconciliationController {
  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankReconciliationService bankReconciliationService;
  protected MoveLineRepository moveLineRepository;
  protected BankReconciliationLineService bankReconciliationLineService;

  @Inject
  public BankReconciliationController(
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationService bankReconciliationService,
      MoveLineRepository moveLineRepository,
      BankReconciliationLineService bankReconciliationLineService) {
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.bankReconciliationService = bankReconciliationService;
    this.moveLineRepository = moveLineRepository;
    this.bankReconciliationLineService = bankReconciliationLineService;
  }

  public void updateAmounts(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    BankReconciliation br =
        bankReconciliationRepository.find(context.asType(BankReconciliation.class).getId());

    //if (bankReconciliationService.updateAmounts(br)) response.setReload(true);
  }

  public void unreconcile(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    BankReconciliation br =
        bankReconciliationRepository.find(context.asType(BankReconciliation.class).getId());
    List<BankReconciliationLine> bankReconciliationLines =
        br.getBankReconciliationLineList().stream()
            .filter(line -> line.getIsSelectedBankReconciliation())
            .collect(Collectors.toList());
    if (bankReconciliationLines.isEmpty()) {
      response.setFlash(I18n.get("Please select a reconciliation line"));
    } else {
      bankReconciliationService.unreconcileLines(bankReconciliationLines);
      response.setReload(true);
    }
  }

  public void reconcileSelected(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    BankReconciliation br = context.asType(BankReconciliation.class);
    BankReconciliationLine bankReconciliationLine;
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter(
                "self.isSelectedBankReconciliation = true AND self.move.journal = :journal AND self.move.statusSelect < :statusSelect AND self.account = :cashAccount AND ((self.debit > 0 AND self.bankReconciledAmount < self.debit) OR (self.credit > 0 AND self.bankReconciledAmount < self.credit))")
            .bind("cashAccount", br.getCashAccount())
            .bind("statusSelect", MoveRepository.STATUS_CANCELED)
            .bind("journal", br.getJournal())
            .fetch();
    if (br.getBankReconciliationLineList().stream()
                .filter(line -> line.getIsSelectedBankReconciliation())
                .count()
            == 0
        || moveLines.size() == 0) {
      if (br.getBankReconciliationLineList().stream()
                  .filter(line -> line.getIsSelectedBankReconciliation())
                  .count()
              == 0
          && moveLines.size() == 0)
        response.setError(I18n.get("Please select one bank reconciliation line and one move line"));
      else if (br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .count()
          == 0) response.setError(I18n.get("Please select one bank reconciliation line"));
      else if (moveLines.size() == 0) response.setError(I18n.get("Please select one move line"));
    } else if (br.getBankReconciliationLineList().stream()
                .filter(line -> line.getIsSelectedBankReconciliation())
                .count()
            > 1
        || moveLines.size() > 1) {
      if (br.getBankReconciliationLineList().stream()
                  .filter(line -> line.getIsSelectedBankReconciliation())
                  .count()
              > 1
          && moveLines.size() > 1)
        response.setError(
            I18n.get("Please select only one bank reconciliation line and only one move line"));
      else if (br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .count()
          > 1) response.setError(I18n.get("Please select only one bank reconciliation line"));
      else if (moveLines.size() > 1)
        response.setError(I18n.get("Please select only one move line"));
    } else {
      bankReconciliationLine =
          br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .collect(Collectors.toList())
              .get(0);
      bankReconciliationLine.setMoveLine(moveLines.get(0));
      bankReconciliationLine =
          bankReconciliationLineService.setMoveLine(bankReconciliationLine, moveLines.get(0));
      br = bankReconciliationRepository.find(br.getId());
      response.setValue("bankReconciliationLineList", br.getBankReconciliationLineList());
      response.setReload(true);
    }
  }

  public void automaticReconciliation(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliationService.reconciliateAccordingToQueries(
          bankReconciliationRepository.find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void loadBankStatement(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliationService.loadBankStatement(
          bankReconciliationRepository.find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void loadOtherBankStatement(ActionRequest request, ActionResponse response) {

    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
      bankReconciliation.setIncludeOtherBankStatements(true);
      bankReconciliationService.loadBankStatement(bankReconciliation, false);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {

    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliationService.compute(
          bankReconciliationRepository.find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeBalances(ActionRequest request, ActionResponse response) {

    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliationService.computeBalances(
          bankReconciliationRepository.find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {

    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      Beans.get(BankReconciliationValidateService.class)
          .validate(bankReconciliationRepository.find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateMultipleReconcile(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      Map<String, Object> bankReconciliationContext =
          (Map<String, Object>) context.get("_bankReconciliation");

      BankReconciliation bankReconciliation =
          bankReconciliationRepository.find(
              ((Integer) bankReconciliationContext.get("id")).longValue());

      List<HashMap<String, Object>> moveLinesToReconcileContext =
          (List<HashMap<String, Object>>) context.get("toReconcileMoveLineSet");

      Map<String, Object> selectedBankReconciliationLineContext =
          (Map<String, Object>) context.get("_selectedBankReconciliationLine");
      BankReconciliationLine bankReconciliationLine =
          Beans.get(BankReconciliationLineRepository.class)
              .find(((Integer) selectedBankReconciliationLineContext.get("id")).longValue());

      Beans.get(BankReconciliationValidateService.class)
          .validateMultipleBankReconciles(
              bankReconciliation, bankReconciliationLine, moveLinesToReconcileContext);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setBankDetailsDomain(ActionRequest request, ActionResponse response) {
    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    String domain = bankReconciliationService.createDomainForBankDetails(bankReconciliation);
    // if nothing was found for the domain, we set it at a default value.
    if (domain.equals("")) {
      response.setAttr("bankDetails", "domain", "self.id IN (0)");
    } else {
      response.setAttr("bankDetails", "domain", domain);
    }
  }

  public void printBankReconciliation(ActionRequest request, ActionResponse response) {
    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    try {
      String fileLink =
          ReportFactory.createReport(
                  IReport.BANK_RECONCILIATION, "Bank Reconciliation" + "-${date}")
              .addParam("BankReconciliationId", bankReconciliation.getId())
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam(
                  "Timezone",
                  bankReconciliation.getCompany() != null
                      ? bankReconciliation.getCompany().getTimezone()
                      : null)
              .addFormat("pdf")
              .toAttach(bankReconciliation)
              .generate()
              .getFileLink();

      response.setView(ActionView.define("Bank Reconciliation").add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setJournalDomain(ActionRequest request, ActionResponse response) {

    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    String journalIds = null;

    if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
      journalIds = bankReconciliationService.getJournalDomain(bankReconciliation);
    }

    if (Strings.isNullOrEmpty(journalIds)) {
      response.setAttr("journal", "domain", "self.id IN (0)");
    } else {
      response.setAttr("journal", "domain", "self.id IN(" + journalIds + ")");
    }
  }

  public void setJournal(ActionRequest request, ActionResponse response) {

    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    Journal journal = null;

    if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
      journal = bankReconciliationService.getJournal(bankReconciliation);
    }
    response.setValue("journal", journal);
  }

  public void setCashAccountDomain(ActionRequest request, ActionResponse response) {

    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    String cashAccountIds = null;

    if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
      cashAccountIds = bankReconciliationService.getCashAccountDomain(bankReconciliation);
    }

    if (Strings.isNullOrEmpty(cashAccountIds)) {
      response.setAttr("cashAccount", "domain", "self.id IN (0)");
    } else {
      response.setAttr("cashAccount", "domain", "self.id IN(" + cashAccountIds + ")");
    }
  }

  public void setCashAccount(ActionRequest request, ActionResponse response) {

    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    Account cashAccount = null;

    if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
      cashAccount = bankReconciliationService.getCashAccount(bankReconciliation);
    }
    response.setValue("cashAccount", cashAccount);
  }

  public void autoAccounting(ActionRequest request, ActionResponse response) {
    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    bankReconciliationService.generateMovesAutoAccounting(bankReconciliation);
    response.setReload(true);
  }
}
