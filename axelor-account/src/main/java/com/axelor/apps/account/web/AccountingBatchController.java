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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingReportPrintService;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.apps.account.service.AccountingReportToolService;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.account.service.batch.AccountingBatchViewService;
import com.axelor.apps.account.service.batch.BatchAutoMoveLettering;
import com.axelor.apps.account.service.batch.BatchCloseAnnualAccounts;
import com.axelor.apps.account.service.batch.BatchPrintAccountingReportService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Resource;
import com.axelor.studio.db.App;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class AccountingBatchController {

  /**
   * Lancer le batch de relance
   *
   * @param request
   * @param response
   */
  public void actionDebtRecovery(ActionRequest request, ActionResponse response) {

    runBatch(AccountingBatchRepository.ACTION_DEBT_RECOVERY, request, response);
  }

  /**
   * Lancer le batch de détermination des créances douteuses
   *
   * @param request
   * @param response
   */
  public void actionDoubtfulCustomer(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_DOUBTFUL_CUSTOMER, request, response);
  }

  /**
   * Lancer le batch de remboursement
   *
   * @param request
   * @param response
   */
  public void actionReimbursement(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_REIMBURSEMENT, request, response);
  }

  /**
   * Lancer le batch de prélèvement
   *
   * @param request
   * @param response
   */
  public void actionDirectDebit(ActionRequest request, ActionResponse response) {
    runBatch(null, request, response);
  }

  /**
   * Lancer le batch de calcul du compte client
   *
   * @param request
   * @param response
   */
  public void actionAccountingCustomer(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_ACCOUNT_CUSTOMER, request, response);
  }

  /**
   * Lancer le batch de calcul du compte client
   *
   * @param request
   * @param response
   */
  public void actionMoveLineExport(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_MOVE_LINE_EXPORT, request, response);
  }

  public void actionCreditTransfer(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_CREDIT_TRANSFER, request, response);
  }

  public void actionRealizeFixedAssetLines(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_REALIZE_FIXED_ASSET_LINES, request, response);
  }

  public void actionBillOfExchange(ActionRequest request, ActionResponse response) {

    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    accountingBatch = Beans.get(AccountingBatchRepository.class).find(accountingBatch.getId());
    Batch batch = Beans.get(AccountingBatchService.class).billOfExchange(accountingBatch);
    if (batch != null) response.setInfo(batch.getComments());
    response.setReload(true);
  }

  public void actionCloseAnnualAccounts(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_CLOSE_OR_OPEN_THE_ANNUAL_ACCOUNTS, request, response);
  }

  public void runBatch(Integer actionSelect, ActionRequest request, ActionResponse response) {
    try {

      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      if (actionSelect != null && !actionSelect.equals(accountingBatch.getActionSelect())) {
        return;
      }
      AccountingBatchService accountingBatchService = Beans.get(AccountingBatchService.class);
      accountingBatchService.setBatchModel(accountingBatch);

      ControllerCallableTool<Batch> batchControllerCallableTool = new ControllerCallableTool<>();
      Batch batch =
          batchControllerCallableTool.runInSeparateThread(accountingBatchService, response);
      if (batch != null) {
        response.setInfo(batch.getComments());
        if (accountingBatch.getActionSelect()
            == AccountingBatchRepository.ACTION_AUTO_MOVE_LETTERING) {
          ActionView.ActionViewBuilder actionViewBuilder =
              ActionView.define(I18n.get("Move lines reconciled"));

          actionViewBuilder.domain(":batch MEMBER OF self.batchSet");
          actionViewBuilder.context("batch", batch);

          actionViewBuilder.model(MoveLine.class.getName());
          actionViewBuilder.add("grid", "move-line-account-batch-auto-move-lettering-grid");
          actionViewBuilder.add("form", "move-line-form");

          response.setView(actionViewBuilder.map());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void blockCustomersWithLatePayments(ActionRequest request, ActionResponse response) {

    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    accountingBatch = Beans.get(AccountingBatchRepository.class).find(accountingBatch.getId());
    Batch batch =
        Beans.get(AccountingBatchService.class).blockCustomersWithLatePayments(accountingBatch);
    if (batch != null) response.setInfo(batch.getComments());
    response.setReload(true);
  }

  /**
   * Throw the control of move consistency batch
   *
   * @param request
   * @param response
   */
  public void controlMoveConsistency(ActionRequest request, ActionResponse response) {
    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      AccountingBatchService accountingBatchService = Beans.get(AccountingBatchService.class);
      accountingBatchService.setBatchModel(accountingBatch);

      ControllerCallableTool<Batch> batchControllerCallableTool = new ControllerCallableTool<>();
      Batch batch =
          batchControllerCallableTool.runInSeparateThread(accountingBatchService, response);
      if (batch != null && ObjectUtils.notEmpty(batch.getComments())) {
        response.setInfo(batch.getComments());
      }
      response.setReload(true);
      if (batch != null) {
        response.setView(
            ActionView.define(I18n.get("Batch"))
                .model(Batch.class.getName())
                .add("form", "batch-form")
                .param("popup-save", "true")
                .context("_showRecord", batch.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // WS

  /**
   * Lancer le batch à travers un web service.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void run(ActionRequest request, ActionResponse response) throws AxelorException {

    Batch batch =
        Beans.get(AccountingBatchService.class).run((String) request.getContext().get("code"));
    Map<String, Object> mapData = new HashMap<String, Object>();
    mapData.put("anomaly", batch.getAnomaly());
    response.setData(mapData);
  }

  public void printAccountingReport(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      if (accountingBatch != null && accountingBatch.getGenerateGeneralLedger()) {

        if (Beans.get(AccountingBatchService.class).checkIfAnomalyInBatch(accountingBatch)) {
          return;
        }

        AccountingReportService accountingReportService = Beans.get(AccountingReportService.class);

        AccountingReport accountingReport =
            Beans.get(BatchPrintAccountingReportService.class)
                .createAccountingReportFromBatch(accountingBatch);
        int typeSelect = accountingReport.getReportType().getTypeSelect();

        if ((typeSelect >= AccountingReportRepository.EXPORT_ADMINISTRATION
            && typeSelect < AccountingReportRepository.REPORT_ANALYTIC_BALANCE)) {
          MetaFile accessFile = accountingReportService.export(accountingReport);

          if ((typeSelect == AccountingReportRepository.EXPORT_ADMINISTRATION
                  || typeSelect == AccountingReportRepository.EXPORT_N4DS)
              && accessFile != null) {

            response.setView(
                ActionView.define(I18n.get("Export file"))
                    .model(App.class.getName())
                    .add(
                        "html",
                        "ws/rest/com.axelor.meta.db.MetaFile/"
                            + accessFile.getId()
                            + "/content/download?v="
                            + accessFile.getVersion())
                    .param("download", "true")
                    .map());
          }
        } else {
          if (Beans.get(AccountingReportToolService.class)
                  .isThereAlreadyDraftReportInPeriod(accountingReport)
              && accountingReport.getReportType().getTypeSelect()
                  == AccountingReportRepository.REPORT_FEES_DECLARATION_PREPARATORY_PROCESS) {
            response.setError(
                I18n.get(
                    "There is already an ongoing accounting report of this type in draft status for this same period."));
            return;
          }
          String fileLink = accountingReportService.print(accountingReport);
          String name = Beans.get(AccountingReportPrintService.class).computeName(accountingReport);
          response.setView(ActionView.define(name).add("html", fileLink).map());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void actionAccountingCutOff(ActionRequest request, ActionResponse response) {
    runBatch(AccountingBatchRepository.ACTION_ACCOUNTING_CUT_OFF, request, response);
  }

  public void actionAutoMoveLettering(ActionRequest request, ActionResponse response) {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    BatchAutoMoveLettering service = Beans.get(BatchAutoMoveLettering.class);
    if (service.existsAlreadyRunning(accountingBatch)) {
      response.setError(I18n.get(AccountExceptionMessage.BATCH_AUTO_MOVE_LETTERING_ALREADY_EXISTS));
    }
    boolean isMoveLinesAwaitingReconcile = service.getIsMoveLinesAwaitingReconcile(accountingBatch);
    Boolean isShowLinesButtonDisplayed =
        (Boolean) request.getContext().get("isShowMoveLinesInProposalBtnDisplayed");
    if (isMoveLinesAwaitingReconcile
        && (isShowLinesButtonDisplayed == null || !isShowLinesButtonDisplayed)) {
      response.setInfo(
          I18n.get(AccountExceptionMessage.BATCH_AUTO_MOVE_LETTERING_PENDING_PROPOSAL_EXISTS));
      response.setValue("$isShowMoveLinesInProposalBtnDisplayed", true);
    } else {
      response.setValue("$isShowMoveLinesInProposalBtnDisplayed", false);
      runBatch(AccountingBatchRepository.ACTION_AUTO_MOVE_LETTERING, request, response);
    }
  }

  public void setLetteringPartnersDomain(ActionRequest request, ActionResponse response) {
    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      AccountManagementAccountService service = Beans.get(AccountManagementAccountService.class);
      List<Account> accountsBetween =
          service.getAccountsBetween(
              accountingBatch.getFromAccount(), accountingBatch.getToAccount());
      String domain = ":company MEMBER OF self.companySet";
      if (CollectionUtils.isNotEmpty(accountsBetween)) {
        if (service.areAllAccountsOfType(accountsBetween, AccountTypeRepository.TYPE_PAYABLE)) {
          domain += " AND self.isSupplier is true";
        } else if (service.areAllAccountsOfType(
            accountsBetween, AccountTypeRepository.TYPE_RECEIVABLE)) {
          domain += " AND self.isCustomer is true";
        }
      }
      response.setAttr("partnerSet", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showMoveLinesInProposal(ActionRequest request, ActionResponse response) {
    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      BatchAutoMoveLettering service = Beans.get(BatchAutoMoveLettering.class);

      ActionView.ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Move lines linked to reconcile group proposals"));

      actionViewBuilder.domain(service.getMoveLinesAwaitingReconcileFilters(accountingBatch));
      for (Map.Entry<String, Object> context :
          service.getMoveLinesAwaitingReconcileParams(accountingBatch).entrySet()) {
        actionViewBuilder.context(context.getKey(), context.getValue());
      }

      actionViewBuilder.model(MoveLine.class.getName());
      actionViewBuilder.add("grid", "move-line-account-batch-auto-move-lettering-grid");
      actionViewBuilder.add("form", "move-line-form");

      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeMoveLineCutOffFields(ActionRequest request, ActionResponse response) {
    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      Beans.get(MoveLineService.class).computeCutOffProrataAmount(accountingBatch);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void setGeneratedStatusDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    if (!Arrays.asList(
            AccountingBatchRepository.ACTION_CLOSE_OR_OPEN_THE_ANNUAL_ACCOUNTS,
            AccountingBatchRepository.ACTION_ACCOUNTING_CUT_OFF)
        .contains(accountingBatch.getActionSelect())) {
      return;
    }

    Journal journal =
        accountingBatch.getActionSelect() == AccountingBatchRepository.ACTION_ACCOUNTING_CUT_OFF
            ? accountingBatch.getMiscOpeJournal()
            : null;

    response.setAttr(
        "generatedMoveStatusSelect",
        "selection-in",
        Beans.get(MoveToolService.class)
            .getMoveStatusSelection(accountingBatch.getCompany(), journal));
  }

  @ErrorException
  public void checkDaybookMovesOnFiscalYearLabel(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

    Integer daybookMoveCount =
        Beans.get(BatchCloseAnnualAccounts.class).getDaybookMoveCount(accountingBatch);
    String title = "";

    if (daybookMoveCount > 0
        && Optional.of(accountingBatch)
            .map(AccountingBatch::getYear)
            .map(Year::getName)
            .isPresent()) {
      title =
          String.format(
              I18n.get(ITranslation.CLOSURE_OPENING_BATCH_DAYBOOK_MOVE_LABEL),
              accountingBatch.getYear().getName(),
              daybookMoveCount);
    }

    response.setAttr("daybookRemainingLabel", "hidden", StringUtils.isEmpty(title));
    response.setAttr("daybookRemainingLabel", "title", title);
  }

  public void checkDaybookMovesOnFiscalYearWarning(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

    if (AccountingBatchRepository.ACTION_CLOSE_OR_OPEN_THE_ANNUAL_ACCOUNTS
            != accountingBatch.getActionSelect()
        || accountingBatch.getYear() == null) {
      return;
    }

    Integer daybookMoveCount =
        Beans.get(BatchCloseAnnualAccounts.class).getDaybookMoveCount(accountingBatch);

    if (daybookMoveCount > 0) {
      response.setAlert(
          String.format(
              I18n.get(ITranslation.CLOSURE_OPENING_BATCH_DAYBOOK_MOVE_ERROR_LABEL),
              accountingBatch.getYear().getName(),
              daybookMoveCount));
    }
  }

  public void setClosureAccountSet(ActionRequest request, ActionResponse response) {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    List<Account> closureAccountSet =
        Beans.get(AccountingBatchViewService.class)
            .getClosureAccountSet(
                accountingBatch, (boolean) request.getContext().get("closeAllAccounts"));
    response.setValue(
        "closureAccountSet",
        closureAccountSet.stream().map(Resource::toMapCompact).collect(Collectors.toList()));
  }

  public void setOpeningAccountSet(ActionRequest request, ActionResponse response) {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    List<Account> openingAccountSet =
        Beans.get(AccountingBatchViewService.class)
            .getOpeningAccountSet(
                accountingBatch, (boolean) request.getContext().get("openAllAccounts"));
    response.setValue(
        "openingAccountSet",
        openingAccountSet.stream().map(Resource::toMapCompact).collect(Collectors.toList()));
  }

  public void setCloseAllAccounts(ActionRequest request, ActionResponse response) {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    Set<Account> closureAccountSet = accountingBatch.getClosureAccountSet();
    if (ObjectUtils.isEmpty(closureAccountSet)) {
      response.setValue("$closeAllAccounts", false);
      return;
    }
    response.setValue(
        "$closeAllAccounts",
        closureAccountSet.size()
            == Beans.get(AccountingBatchViewService.class)
                .countAllAvailableAccounts(accountingBatch, true)
                .intValue());
  }

  public void setOpenAllAccounts(ActionRequest request, ActionResponse response) {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    Set<Account> openingAccountSet = accountingBatch.getOpeningAccountSet();
    if (ObjectUtils.isEmpty(openingAccountSet)) {
      response.setValue("$openAllAccounts", false);
      return;
    }
    response.setValue(
        "$openAllAccounts",
        openingAccountSet.size()
            == Beans.get(AccountingBatchViewService.class)
                .countAllAvailableAccounts(accountingBatch, false)
                .intValue());
  }
}
