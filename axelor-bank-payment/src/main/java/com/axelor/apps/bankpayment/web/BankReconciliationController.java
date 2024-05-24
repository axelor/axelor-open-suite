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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.bankpayment.report.ITranslation;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationToolService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationValidateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class BankReconciliationController {

  public void unreconcile(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      BankReconciliation br =
          Beans.get(BankReconciliationRepository.class)
              .find(context.asType(BankReconciliation.class).getId());
      List<BankReconciliationLine> bankReconciliationLines =
          br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .collect(Collectors.toList());
      if (bankReconciliationLines.isEmpty()) {
        response.setInfo(I18n.get(ITranslation.BANK_RECONCILIATION_SELECT_A_LINE));
      } else {
        BankReconciliationService bankReconciliationService =
            Beans.get(BankReconciliationService.class);
        bankReconciliationService.unreconcileLines(bankReconciliationLines);
        bankReconciliationService.mergeSplitedReconciliationLines(br);
        bankReconciliationService.computeBalances(br);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void reconcileSelected(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      BankReconciliation bankReconciliation = context.asType(BankReconciliation.class);
      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);
      bankReconciliation =
          Beans.get(BankReconciliationRepository.class).find(bankReconciliation.getId());
      Beans.get(BankReconciliationService.class).reconcileSelected(bankReconciliation);
      bankReconciliationService.computeBalances(bankReconciliation);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void automaticReconciliation(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);
      BankReconciliationRepository bankReconciliationRepository =
          Beans.get(BankReconciliationRepository.class);
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliationService.reconciliateAccordingToQueries(
          bankReconciliationRepository.find(bankReconciliation.getId()));
      bankReconciliationService.computeBalances(
          bankReconciliationRepository.find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void loadBankStatement(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);
      BankReconciliationRepository bankReconciliationRepository =
          Beans.get(BankReconciliationRepository.class);
      BankReconciliation bankReconciliation =
          bankReconciliationRepository.find(context.asType(BankReconciliation.class).getId());
      Company company = bankReconciliation.getCompany();
      if (company != null) {
        bankReconciliation = bankReconciliationService.computeInitialBalance(bankReconciliation);
      }
      if (bankReconciliation != null) {
        bankReconciliationService.loadBankStatement(
            bankReconciliationRepository.find(bankReconciliation.getId()));

        if (company != null && company.getBankPaymentConfig() != null) {
          if (bankReconciliation
              .getCompany()
              .getBankPaymentConfig()
              .getHasAutoMoveFromStatementRule()) {
            bankReconciliationService.generateMovesAutoAccounting(
                bankReconciliationRepository.find(bankReconciliation.getId()));
          }
          if (company != null
              && bankReconciliation
                  .getCompany()
                  .getBankPaymentConfig()
                  .getHasAutomaticReconciliation()) {
            bankReconciliationService.reconciliateAccordingToQueries(
                bankReconciliationRepository.find(bankReconciliation.getId()));
          }
        }

        bankReconciliationService.computeBalances(
            bankReconciliationRepository.find(bankReconciliation.getId()));
        response.setReload(true);
      } else {
        response.setAlert(I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_ALREADY_OPEN));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void loadOtherBankStatement(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliation =
          Beans.get(BankReconciliationRepository.class).find(bankReconciliation.getId());
      bankReconciliation.setIncludeOtherBankStatements(true);
      Beans.get(BankReconciliationService.class).loadBankStatement(bankReconciliation, false);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      Beans.get(BankReconciliationService.class)
          .compute(Beans.get(BankReconciliationRepository.class).find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computeBalances(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      Beans.get(BankReconciliationService.class)
          .computeBalances(
              Beans.get(BankReconciliationRepository.class).find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      bankReconciliation =
          Beans.get(BankReconciliationRepository.class).find(bankReconciliation.getId());
      Beans.get(BankReconciliationValidateService.class).validate(bankReconciliation);
      Beans.get(BankReconciliationService.class).computeBalances(bankReconciliation);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateMultipleReconcile(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);

      Map<String, Object> bankReconciliationContext =
          (Map<String, Object>) context.get("_bankReconciliation");

      BankReconciliation bankReconciliation =
          Beans.get(BankReconciliationRepository.class)
              .find(((Integer) bankReconciliationContext.get("id")).longValue());

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

      bankReconciliationService.computeBalances(bankReconciliation);

      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setBankDetailsDomain(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      String domain =
          Beans.get(BankReconciliationService.class).createDomainForBankDetails(bankReconciliation);
      // if nothing was found for the domain, we set it at a default value.
      if (domain.equals("")) {
        response.setAttr("bankDetails", "domain", "self.id IN (0)");
      } else {
        response.setAttr("bankDetails", "domain", domain);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printBankReconciliation(ActionRequest request, ActionResponse response) {
    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    try {
      BankPaymentConfig bankPaymentConfig =
          Beans.get(BankPaymentConfigService.class)
              .getBankPaymentConfig(bankReconciliation.getCompany());
      int dateMargin = bankPaymentConfig.getBnkStmtAutoReconcileDateMargin();
      String fileLink =
          ReportFactory.createReport(
                  IReport.BANK_RECONCILIATION, I18n.get("Bank Reconciliation") + "-${date}")
              .addParam("BankReconciliationId", bankReconciliation.getId())
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam(
                  "Timezone",
                  bankReconciliation.getCompany() != null
                      ? bankReconciliation.getCompany().getTimezone()
                      : null)
              .addParam(
                  "BankReconciliationFromDate",
                  Date.valueOf(bankReconciliation.getFromDate().minusDays(dateMargin)))
              .addParam(
                  "BankReconciliationToDate",
                  Date.valueOf(bankReconciliation.getToDate().plusDays(dateMargin)))
              .addFormat("pdf")
              .toAttach(bankReconciliation)
              .generate()
              .getFileLink();

      response.setView(
          ActionView.define(I18n.get("Bank Reconciliation")).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printBankReconciliationDetailed(ActionRequest request, ActionResponse response) {
    BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
    try {
      String fileLink =
          Beans.get(BankReconciliationService.class).printNewBankReconciliation(bankReconciliation);
      if (StringUtils.notEmpty(fileLink)) {
        response.setView(
            ActionView.define(I18n.get("Bank Reconciliation")).add("html", fileLink).map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setJournalDomain(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      String journalIds = null;

      if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
        journalIds =
            Beans.get(BankReconciliationService.class).getJournalDomain(bankReconciliation);
      }

      if (Strings.isNullOrEmpty(journalIds)) {
        response.setAttr("journal", "domain", "self.id IN (0)");
      } else {
        response.setAttr(
            "journal",
            "domain",
            "self.id IN("
                + journalIds
                + ") AND self.statusSelect = "
                + JournalRepository.STATUS_ACTIVE);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setJournal(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      Journal journal = null;

      if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
        journal = Beans.get(BankReconciliationService.class).getJournal(bankReconciliation);
      }
      response.setValue("journal", journal);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setCashAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      String cashAccountIds = null;

      if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
        cashAccountIds =
            Beans.get(BankReconciliationService.class).getCashAccountDomain(bankReconciliation);
      }

      if (Strings.isNullOrEmpty(cashAccountIds)) {
        response.setAttr("cashAccount", "domain", "self.id IN (0)");
      } else {
        response.setAttr("cashAccount", "domain", "self.id IN(" + cashAccountIds + ")");
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setCashAccount(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      Account cashAccount = null;

      if (EntityHelper.getEntity(bankReconciliation).getBankDetails() != null) {
        cashAccount = Beans.get(BankReconciliationService.class).getCashAccount(bankReconciliation);
      }
      response.setValue("cashAccount", cashAccount);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void autoAccounting(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);
      bankReconciliationService.generateMovesAutoAccounting(bankReconciliation);
      bankReconciliationService.computeBalances(
          Beans.get(BankReconciliationRepository.class).find(bankReconciliation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkIncompleteBankReconciliationLine(
      ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      List<BankReconciliationLine> bankReconciliationLines =
          bankReconciliation.getBankReconciliationLineList();
      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        Beans.get(BankReconciliationLineService.class).checkIncompleteLine(bankReconciliationLine);
      }
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeBankStatement(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      if (bankReconciliation.getBankStatement() != null) {
        bankReconciliation =
            Beans.get(BankReconciliationService.class).onChangeBankStatement(bankReconciliation);
        response.setValues(bankReconciliation);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setBankStatementIsFullyReconciled(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation =
          Beans.get(BankReconciliationRepository.class)
              .find(request.getContext().asType(BankReconciliation.class).getId());
      Beans.get(BankStatementService.class)
          .setIsFullyReconciled(bankReconciliation.getBankStatement());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void showUnreconciledMoveLines(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);
      ActionViewBuilder actionViewBuilder =
          ActionView.define(
              I18n.get(
                  com.axelor.apps.bankpayment.translation.ITranslation
                      .BANK_RECONCILIATION_UNRECONCILED_MOVE_LINE_LIST_PANEL_TITLE));
      actionViewBuilder.model(MoveLine.class.getName());
      if (BankReconciliationToolService.isForeignCurrency(bankReconciliation)) {
        actionViewBuilder.add("grid", "move-line-bank-reconciliation-grid-currency-amount");
      } else {
        actionViewBuilder.add("grid", "move-line-bank-reconciliation-grid");
      }
      actionViewBuilder.add("form", "move-line-form");
      actionViewBuilder.domain(bankReconciliationService.getRequestMoveLines());
      if (bankReconciliation.getCompany() == null) {
        return;
      }
      Map<String, Object> params =
          bankReconciliationService.getBindRequestMoveLine(bankReconciliation);
      Set<String> keys = params.keySet();
      for (String key : keys) {
        actionViewBuilder.context(key, params.get(key));
      }
      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void getDomainForWizard(ActionRequest request, ActionResponse response) {
    try {
      Long bankReconciliationId =
          Long.valueOf(
              (Integer)
                  ((LinkedHashMap<?, ?>) request.getContext().get("_bankReconciliation"))
                      .get("id"));
      BankReconciliation bankReconciliation =
          Beans.get(BankReconciliationRepository.class).find(bankReconciliationId);
      BigDecimal credit = new BigDecimal((String) request.getContext().get("bankStatementCredit"));
      BigDecimal debit = new BigDecimal((String) request.getContext().get("bankStatementDebit"));
      response.setAttr(
          "$toReconcileMoveLineSet",
          "domain",
          Beans.get(BankReconciliationService.class)
              .getDomainForWizard(bankReconciliation, credit, debit));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotalOfSelectedMoveLines(ActionRequest request, ActionResponse response) {
    try {
      Long bankReconciliationId =
          Long.valueOf(
              (Integer)
                  ((LinkedHashMap<?, ?>) request.getContext().get("_bankReconciliation"))
                      .get("id"));
      BankReconciliation bankReconciliation =
          Beans.get(BankReconciliationRepository.class).find(bankReconciliationId);

      List<LinkedHashMap> toReconcileMoveLineSet =
          (List<LinkedHashMap>) (request.getContext().get("toReconcileMoveLineSet"));
      response.setValue(
          "$selectedMoveLineTotal",
          Beans.get(BankReconciliationService.class)
              .getSelectedMoveLineTotal(bankReconciliation, toReconcileMoveLineSet));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void correctButtonVisible(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      response.setAttr(
          "correctBtn",
          "hidden",
          Beans.get(BankReconciliationService.class).getIsCorrectButtonHidden(bankReconciliation));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void correctedLabelFill(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      if (bankReconciliation.getHasBeenCorrected()) {
        response.setAttr(
            "correctedLabel",
            "title",
            Beans.get(BankReconciliationService.class)
                .getCorrectedLabel(
                    bankReconciliation.getCorrectedDateTime(),
                    bankReconciliation.getCorrectedUser()));
        ;
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void correct(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      Beans.get(BankReconciliationService.class).correct(bankReconciliation, request.getUser());
      response.setAttr("correctBtn", "hidden", true);
      response.setValues(bankReconciliation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeSelections(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliation bankReconciliation = request.getContext().asType(BankReconciliation.class);
      BankReconciliationService bankReconcialiationService =
          Beans.get(BankReconciliationService.class);
      response.setValue(
          "$selectionUnreconciledMoveLines",
          bankReconcialiationService.computeUnreconciledMoveLinesSelection(bankReconciliation));
      response.setValue(
          "$selectionBankReconciliationLines",
          bankReconcialiationService.computeBankReconciliationLinesSelection(bankReconciliation));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
