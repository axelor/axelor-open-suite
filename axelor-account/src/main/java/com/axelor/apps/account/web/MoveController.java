/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.move.record.MoveDefaultService;
import com.axelor.apps.account.service.move.record.MoveRecordService;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class MoveController {

  public void accounting(ActionRequest request, ActionResponse response) {

    Move move = request.getContext().asType(Move.class);
    move = Beans.get(MoveRepository.class).find(move.getId());
    try {
      Beans.get(MoveValidateService.class).accounting(move);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void updateLines(ActionRequest request, ActionResponse response) {

    Move move = request.getContext().asType(Move.class);

    try {

      move =
          Beans.get(MoveViewHelperService.class)
              .updateMoveLinesDateExcludeFromPeriodOnlyWithoutSave(move);
      response.setValue("moveLineList", move.getMoveLineList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateReverse(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      Move move = context.asType(Move.class);
      move = Beans.get(MoveRepository.class).find(move.getId());

      Map<String, Object> assistantMap =
          Beans.get(ExtractContextMoveService.class)
              .getMapFromMoveWizardGenerateReverseForm(context);

      Move newMove = Beans.get(MoveReverseService.class).generateReverse(move, assistantMap);
      if (newMove != null) {
        response.setView(
            ActionView.define(I18n.get("Account moves"))
                .model("com.axelor.apps.account.db.Move")
                .add("grid", "move-grid")
                .add("form", "move-form")
                .param("forceEdit", "true")
                .context("_showRecord", newMove.getId().toString())
                .map());
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void massReverseMove(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      List<Long> moveIds = (List<Long>) context.get("_ids");

      if (CollectionUtils.isNotEmpty(moveIds)) {
        List<Move> moveList =
            Beans.get(MoveRepository.class)
                .all()
                .filter("self.id IN :moveList AND self.statusSelect <> :simulatedStatus")
                .bind("moveList", moveIds)
                .bind("simulatedStatus", MoveRepository.STATUS_SIMULATED)
                .fetch();

        if (CollectionUtils.isNotEmpty(moveList)) {
          Map<String, Object> assistantMap =
              Beans.get(ExtractContextMoveService.class)
                  .getMapFromMoveWizardMassReverseForm(context);

          String reverseMoveIds =
              Beans.get(MoveReverseService.class).massReverse(moveList, assistantMap).stream()
                  .map(Move::getId)
                  .map(Objects::toString)
                  .collect(Collectors.joining(","));

          response.setView(
              ActionView.define(I18n.get("Account moves"))
                  .model("com.axelor.apps.account.db.Move")
                  .add("grid", "move-grid")
                  .add("form", "move-form")
                  .param("forceEdit", "true")
                  .domain(
                      String.format(
                          "self.id IN (%s)", reverseMoveIds.isEmpty() ? "0" : reverseMoveIds))
                  .map());

          return;
        }
      }

      response.setError(I18n.get(AccountExceptionMessage.NO_MOVES_SELECTED_MASS_REVERSE));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void accountingMultipleMoves(ActionRequest request, ActionResponse response) {
    List<Integer> moveIds = (List<Integer>) request.getContext().get("_ids");
    try {
      if (moveIds != null && !moveIds.isEmpty()) {
        String error = Beans.get(MoveValidateService.class).accountingMultiple(moveIds);
        if (error.length() > 0) {
          response.setInfo(
              String.format(I18n.get(AccountExceptionMessage.MOVE_ACCOUNTING_NOT_OK), error));
        } else {
          response.setInfo(I18n.get(AccountExceptionMessage.MOVE_ACCOUNTING_OK));
        }

        response.setReload(true);
      } else {
        response.setInfo(I18n.get(AccountExceptionMessage.NO_MOVES_SELECTED));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  public void simulateMultipleMoves(ActionRequest request, ActionResponse response) {
    List<Long> moveIds = (List<Long>) request.getContext().get("_ids");
    try {
      if (moveIds != null && !moveIds.isEmpty()) {

        List<? extends Move> moveList =
            Beans.get(MoveRepository.class)
                .all()
                .filter(
                    "self.id in ?1 AND self.statusSelect = ?2 AND self.journal.authorizeSimulatedMove = true",
                    moveIds,
                    MoveRepository.STATUS_NEW)
                .order("date")
                .fetch();

        if (!moveList.isEmpty()) {
          PeriodServiceAccount periodServiceAccount = Beans.get(PeriodServiceAccount.class);
          User user = AuthUtils.getUser();
          for (Integer id : (List<Integer>) request.getContext().get("_ids")) {
            Move move = Beans.get(MoveRepository.class).find(Long.valueOf(id));
            if (!periodServiceAccount.isAuthorizedToAccountOnPeriod(move, user)) {
              throw new AxelorException(
                  TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                  String.format(
                      I18n.get(AccountExceptionMessage.ACCOUNT_PERIOD_TEMPORARILY_CLOSED),
                      move.getReference()));
            }
          }
          Beans.get(MoveSimulateService.class).simulateMultiple(moveList);
          response.setInfo(I18n.get(AccountExceptionMessage.MOVE_SIMULATION_OK));
          response.setReload(true);
        } else {
          response.setInfo(I18n.get(AccountExceptionMessage.NO_NEW_MOVES_SELECTED));
        }
      } else {
        response.setInfo(I18n.get(AccountExceptionMessage.NO_NEW_MOVES_SELECTED));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void deleteMove(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Move move = request.getContext().asType(Move.class);
      move = Beans.get(MoveRepository.class).find(move.getId());

      this.removeOneMove(move, response);

      if (!move.getStatusSelect().equals(MoveRepository.STATUS_ACCOUNTED)) {
        boolean isActivateSimulatedMoves =
            Optional.of(AuthUtils.getUser())
                .map(User::getActiveCompany)
                .map(Company::getAccountConfig)
                .map(AccountConfig::getIsActivateSimulatedMove)
                .orElse(false);

        response.setView(
            ActionView.define(I18n.get("Moves"))
                .model(Move.class.getName())
                .add("grid", "move-grid")
                .add("form", "move-form")
                .param("search-filters", "move-filters")
                .context("_isActivateSimulatedMoves", isActivateSimulatedMoves)
                .map());
        response.setCanClose(true);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected void removeOneMove(Move move, ActionResponse response) throws Exception {
    MoveRemoveService moveRemoveService = Beans.get(MoveRemoveService.class);
    if (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
        || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
      moveRemoveService.deleteMove(move);
      response.setInfo(I18n.get(AccountExceptionMessage.MOVE_REMOVED_OK));
    } else if (move.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK)) {
      moveRemoveService.archiveDaybookMove(move);
      response.setInfo(I18n.get(AccountExceptionMessage.MOVE_ARCHIVE_OK));
    } else if (move.getStatusSelect().equals(MoveRepository.STATUS_CANCELED)) {
      moveRemoveService.archiveMove(move);
      response.setInfo(I18n.get(AccountExceptionMessage.MOVE_ARCHIVE_OK));
    }
  }

  @SuppressWarnings("unchecked")
  public void deleteMultipleMoves(ActionRequest request, ActionResponse response) {
    try {
      List<Long> moveIds = (List<Long>) request.getContext().get("_ids");
      String flashMessage = I18n.get(AccountExceptionMessage.NO_MOVE_TO_REMOVE_OR_ARCHIVE);

      if (!CollectionUtils.isEmpty(moveIds)) {
        List<? extends Move> moveList =
            Beans.get(MoveRepository.class)
                .all()
                .filter(
                    "self.id in ?1 AND self.statusSelect in (?2,?3,?4,?5) AND (self.archived = false or self.archived = null)",
                    moveIds,
                    MoveRepository.STATUS_NEW,
                    MoveRepository.STATUS_DAYBOOK,
                    MoveRepository.STATUS_CANCELED,
                    MoveRepository.STATUS_SIMULATED)
                .fetch();
        int moveNb = moveList.size();

        if (!moveList.isEmpty()) {
          int errorNB = Beans.get(MoveRemoveService.class).deleteMultiple(moveList);
          flashMessage =
              errorNB > 0
                  ? String.format(
                      I18n.get(AccountExceptionMessage.MOVE_ARCHIVE_OR_REMOVE_NOT_OK_NB), errorNB)
                  : String.format(
                      I18n.get(AccountExceptionMessage.MOVE_ARCHIVE_OR_REMOVE_OK), moveNb);
        }
      }

      response.setInfo(flashMessage);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printMove(ActionRequest request, ActionResponse response) {

    Move move = request.getContext().asType(Move.class);
    try {
      move = Beans.get(MoveRepository.class).find(move.getId());

      String moveName = move.getReference().toString();

      String fileLink =
          ReportFactory.createReport(IReport.ACCOUNT_MOVE, moveName + "-${date}")
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam(
                  "Timezone", move.getCompany() != null ? move.getCompany().getTimezone() : null)
              .addParam("moveId", move.getId())
              .generate()
              .getFileLink();

      response.setView(ActionView.define(moveName).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void showMoveLines(ActionRequest request, ActionResponse response) {

    try {
      ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Move Lines"));
      actionViewBuilder.model(MoveLine.class.getName());
      actionViewBuilder.add("grid", "move-line-grid");
      actionViewBuilder.add("form", "move-line-form");
      actionViewBuilder.param("search-filters", "move-line-filters");

      if (request.getContext().get("_accountingReportId") != null) {
        Long accountingReportId =
            Long.valueOf(request.getContext().get("_accountingReportId").toString());
        actionViewBuilder.domain("self.move.accountingReport.id = " + accountingReportId);
      }
      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotals(ActionRequest request, ActionResponse response) {
    Move move = request.getContext().asType(Move.class);

    try {
      Map<String, Object> values = Beans.get(MoveComputeService.class).computeTotals(move);
      response.setValues(values);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void autoTaxLineGenerate(ActionRequest request, ActionResponse response) {
    Move move = request.getContext().asType(Move.class);
    try {
      if (move.getMoveLineList() != null
          && !move.getMoveLineList().isEmpty()
          && (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
              || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED))) {
        Beans.get(MoveLineTaxService.class).autoTaxLineGenerateNoSave(move, null);

        response.setValue("moveLineList", move.getMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void filterPartner(ActionRequest request, ActionResponse response) {
    Move move = request.getContext().asType(Move.class);
    if (move != null) {
      try {
        String domain =
            Beans.get(MoveViewHelperService.class)
                .filterPartner(move.getCompany(), move.getJournal());
        response.setAttr("partner", "domain", domain);
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  public void manageMoveLineAxis(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move.getCompany() != null) {
        AccountConfig accountConfig =
            Beans.get(AccountConfigService.class).getAccountConfig(move.getCompany());
        if (accountConfig != null
            && Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()
            && accountConfig.getManageAnalyticAccounting()) {
          AnalyticAxis analyticAxis = null;
          for (int i = 1; i <= 5; i++) {
            response.setAttr(
                "moveLineList.axis" + i + "AnalyticAccount",
                "hidden",
                !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
            for (AnalyticAxisByCompany analyticAxisByCompany :
                accountConfig.getAnalyticAxisByCompanyList()) {
              if (analyticAxisByCompany.getSequence() + 1 == i) {
                analyticAxis = analyticAxisByCompany.getAnalyticAxis();
              }
            }
            if (analyticAxis != null) {
              response.setAttr(
                  "moveLineList.axis" + i + "AnalyticAccount", "title", analyticAxis.getName());
              analyticAxis = null;
            }
          }
        } else {
          response.setAttr("moveLineList.analyticDistributionTemplate", "hidden", true);
          response.setAttr("moveLineList.analyticMoveLineList", "hidden", true);
          for (int i = 1; i <= 5; i++) {
            response.setAttr("moveLineList.axis" + i + "AnalyticAccount", "hidden", true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateCounterpart(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Beans.get(MoveCounterPartService.class)
          .generateCounterpartMoveLine(move, this.extractDueDate(request));
      response.setValue("moveLineList", move.getMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void exceptionCounterpart(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Beans.get(MoveToolService.class).exceptionOnGenerateCounterpart(move);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setDescriptionOnLines(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Beans.get(MoveToolService.class).setDescriptionOnMoveLineList(move);
      response.setValue("moveLineList", move.getMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setSimulate(ActionRequest request, ActionResponse response) {
    try {
      Move move =
          Beans.get(MoveRepository.class).find(request.getContext().asType(Move.class).getId());
      Beans.get(MoveSimulateService.class).simulate(move);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateDescription(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move.getDescription() == null) {
        response.setAlert(I18n.get(AccountExceptionMessage.MOVE_CHECK_DESCRIPTION));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void applyCutOffDates(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveComputeService moveComputeService = Beans.get(MoveComputeService.class);

      LocalDate cutOffStartDate =
          LocalDate.parse((String) request.getContext().get("cutOffStartDate"));
      LocalDate cutOffEndDate = LocalDate.parse((String) request.getContext().get("cutOffEndDate"));

      if (moveComputeService.checkManageCutOffDates(move)) {
        moveComputeService.applyCutOffDates(move, cutOffStartDate, cutOffEndDate);

        response.setValue("moveLineList", move.getMoveLineList());
      } else {
        response.setInfo(I18n.get(AccountExceptionMessage.NO_CUT_OFF_TO_APPLY));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setMoveLineDates(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      move = Beans.get(MoveLineControlService.class).setMoveLineDates(move);
      response.setValue("moveLineList", move.getMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected LocalDate extractDueDate(ActionRequest request) {
    if (!request.getContext().containsKey("dueDate")
        || request.getContext().get("dueDate") == null) {
      return null;
    }

    Object dueDateObj = request.getContext().get("dueDate");
    if (LocalDate.class.equals(EntityHelper.getEntityClass(dueDateObj))) {
      return (LocalDate) dueDateObj;
    } else {
      return LocalDate.parse((String) dueDateObj);
    }
  }

  public void checkTermsInPayment(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      String errorMessage =
          Beans.get(MoveInvoiceTermService.class).checkIfInvoiceTermInPayment(move);

      if (StringUtils.notEmpty(errorMessage)) {
        if (move.getId() != null) {
          response.setValue(
              "paymentCondition",
              Beans.get(MoveRepository.class).find(move.getId()).getPaymentCondition());
        }

        response.setError(errorMessage);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on load and in partner, company or payment mode change. Fill the bank details with a
   * default value.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void fillCompanyBankDetails(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Move move = request.getContext().asType(Move.class);
    PaymentMode paymentMode = move.getPaymentMode();
    Company company = move.getCompany();
    Partner partner = move.getPartner();
    if (company == null) {
      response.setValue("companyBankDetails", null);
      return;
    }
    if (partner != null) {
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
    }
    BankDetails defaultBankDetails =
        Beans.get(BankDetailsService.class)
            .getDefaultCompanyBankDetails(company, paymentMode, partner, null);
    response.setValue("companyBankDetails", defaultBankDetails);
  }

  public void setDefaultCurrency(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Map<String, Object> resultMap = Beans.get(MoveDefaultService.class).setDefaultCurrency(move);

      response.setValues(resultMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setCurrencyCode(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Map<String, Object> resultMap = Beans.get(MoveRecordSetService.class).setCurrencyCode(move);

      response.setValues(resultMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setJournal(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Map<String, Object> resultMap = Beans.get(MoveRecordSetService.class).setJournal(move);

      response.setValues(resultMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onNew(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveContext result = Beans.get(MoveRecordService.class).onNew(move);

      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onLoad(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveContext result = Beans.get(MoveRecordService.class).onLoad(move);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onSaveCheck(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveContext result = Beans.get(MoveRecordService.class).onSaveCheck(move);
      // As this method will make a update in the invoiceTerms set values move
      response.setValues(move);
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onSaveBefore(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);

      boolean paymentConditionChange =
          Optional.ofNullable(context.get("paymentConditionChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      boolean headerChange =
          Optional.ofNullable(context.get("headerChange"))
              .map(value -> (Boolean) value)
              .orElse(false);

      MoveContext result =
          Beans.get(MoveRecordService.class)
              .onSaveBefore(move, paymentConditionChange, headerChange);
      // As this method will make a update in the invoiceTerms set values move
      response.setValues(move);
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onSaveAfter(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveContext result = Beans.get(MoveRecordService.class).onSaveAfter(move);
      // As this method will make a update in the invoiceTerms set values move
      response.setValues(move);
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeDate(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);

      boolean paymentConditionChange =
          Optional.ofNullable(context.get("paymentConditionChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      boolean dateChange =
          Optional.ofNullable(context.get("dateChange"))
              .map(value -> (Boolean) value)
              .orElse(false);

      MoveContext result =
          Beans.get(MoveRecordService.class).onChangeDate(move, paymentConditionChange, dateChange);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeJournal(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      MoveContext result = Beans.get(MoveRecordService.class).onChangeJournal(move);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangePartner(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);

      boolean paymentConditionChange =
          Optional.ofNullable(context.get("paymentConditionChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      boolean dateChange =
          Optional.ofNullable(context.get("dateChange"))
              .map(value -> (Boolean) value)
              .orElse(false);

      MoveContext result =
          Beans.get(MoveRecordService.class)
              .onChangePartner(move, paymentConditionChange, dateChange);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeMoveLineList(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);

      boolean paymentConditionChange =
          Optional.ofNullable(context.get("paymentConditionChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      boolean dateChange =
          Optional.ofNullable(context.get("dateChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      MoveContext result =
          Beans.get(MoveRecordService.class)
              .onChangeMoveLineList(move, paymentConditionChange, dateChange);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeOriginDate(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);

      boolean paymentConditionChange =
          Optional.ofNullable(context.get("paymentConditionChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      boolean dateChange =
          Optional.ofNullable(context.get("dateChange"))
              .map(value -> (Boolean) value)
              .orElse(false);

      MoveContext result =
          Beans.get(MoveRecordService.class)
              .onChangeOriginDate(move, paymentConditionChange, dateChange);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeOrigin(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveContext result = Beans.get(MoveRecordService.class).onChangeOrigin(move);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        response.setError(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangePaymentCondition(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);

      boolean paymentConditionChange =
          Optional.ofNullable(context.get("paymentConditionChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      boolean headerChange =
          Optional.ofNullable(context.get("headerChange"))
              .map(value -> (Boolean) value)
              .orElse(false);
      boolean dateChange =
          Optional.ofNullable(context.get("dateChange"))
              .map(value -> (Boolean) value)
              .orElse(false);

      MoveContext result =
          Beans.get(MoveRecordService.class)
              .onChangePaymentCondition(move, paymentConditionChange, dateChange, headerChange);
      response.setValues(result.getValues());
      response.setAttrs(result.getAttrs());
      if (!result.getFlash().isEmpty()) {
        response.setInfo(result.getFlash());
      }
      if (!result.getNotify().isEmpty()) {
        response.setNotify(result.getNotify());
      }
      if (!result.getAlert().isEmpty()) {
        response.setAlert(result.getAlert());
      }
      if (!result.getError().isEmpty()) {
        // Specific to this case because paymentCondition need to be changed to its former value.
        // And setError or setAlert does not make setValue work
        response.setInfo(result.getError());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
