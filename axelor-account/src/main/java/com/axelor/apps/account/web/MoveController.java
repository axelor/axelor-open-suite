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
package com.axelor.apps.account.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveCreateFromInvoiceService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  public void getPeriod(ActionRequest request, ActionResponse response) {

    Move move = request.getContext().asType(Move.class);

    try {
      if (move.getDate() != null && move.getCompany() != null) {
        Period period =
            Beans.get(PeriodService.class)
                .getPeriod(move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL);
        if (period != null && (move.getPeriod() == null || !period.equals(move.getPeriod()))) {

          response.setValue("period", period);
        }
      } else {
        response.setValue("period", null);
      }
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
            ActionView.define(I18n.get("Account move"))
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
      List<Long> moveIds = (List<Long>) request.getContext().get("_ids");

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
                  .getMapFromMoveWizardMassReverseForm(request.getContext());

          String reverseMoveIds =
              Beans.get(MoveReverseService.class).massReverse(moveList, assistantMap).stream()
                  .map(Move::getId)
                  .map(Objects::toString)
                  .collect(Collectors.joining(","));

          response.setView(
              ActionView.define(I18n.get("Account move"))
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

      response.setError(I18n.get(IExceptionMessage.NO_MOVES_SELECTED_MASS_REVERSE));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void accountingMultipleMoves(ActionRequest request, ActionResponse response) {
    List<Long> moveIds = (List<Long>) request.getContext().get("_ids");
    try {
      if (moveIds != null && !moveIds.isEmpty()) {

        List<? extends Move> moveList =
            Beans.get(MoveRepository.class)
                .all()
                .filter(
                    "self.id in ?1 AND self.statusSelect NOT IN (?2, ?3)",
                    moveIds,
                    MoveRepository.STATUS_ACCOUNTED,
                    MoveRepository.STATUS_CANCELED)
                .order("date")
                .fetch();
        if (CollectionUtils.isNotEmpty(moveList)) {
          PeriodServiceAccount periodServiceAccount = Beans.get(PeriodServiceAccount.class);
          User user = AuthUtils.getUser();
          for (Integer id : (List<Integer>) request.getContext().get("_ids")) {
            Move move = Beans.get(MoveRepository.class).find(Long.valueOf(id));
            if (!periodServiceAccount.isAuthorizedToAccountOnPeriod(move.getPeriod(), user)) {
              throw new AxelorException(
                  TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                  String.format(
                      I18n.get(IExceptionMessage.ACCOUNT_PERIOD_TEMPORARILY_CLOSED),
                      move.getReference()));
            }
          }
          String error = Beans.get(MoveValidateService.class).accountingMultiple(moveList);
          if (error.length() > 0) {
            response.setFlash(
                String.format(I18n.get(IExceptionMessage.MOVE_ACCOUNTING_NOT_OK), error));
          } else {
            response.setFlash(I18n.get(IExceptionMessage.MOVE_ACCOUNTING_OK));
          }

          response.setReload(true);
        } else {
          response.setFlash(I18n.get(IExceptionMessage.NO_MOVES_SELECTED));
        }
      } else {
        response.setFlash(I18n.get(IExceptionMessage.NO_MOVES_SELECTED));
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
            if (!periodServiceAccount.isAuthorizedToAccountOnPeriod(move.getPeriod(), user)) {
              throw new AxelorException(
                  TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                  String.format(
                      I18n.get(IExceptionMessage.ACCOUNT_PERIOD_TEMPORARILY_CLOSED),
                      move.getReference()));
            }
          }
          Beans.get(MoveSimulateService.class).simulateMultiple(moveList);
          response.setFlash(I18n.get(IExceptionMessage.MOVE_SIMULATION_OK));
          response.setReload(true);
        } else {
          response.setFlash(I18n.get(IExceptionMessage.NO_NEW_MOVES_SELECTED));
        }
      } else {
        response.setFlash(I18n.get(IExceptionMessage.NO_NEW_MOVES_SELECTED));
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

        response.setView(
            ActionView.define(I18n.get("Moves"))
                .model(Move.class.getName())
                .add("grid", "move-grid")
                .add("form", "move-form")
                .param("search-filters", "move-filters")
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
      response.setFlash(I18n.get(IExceptionMessage.MOVE_REMOVED_OK));
    } else if (move.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK)) {
      moveRemoveService.archiveDaybookMove(move);
      response.setFlash(I18n.get(IExceptionMessage.MOVE_ARCHIVE_OK));
    } else if (move.getStatusSelect().equals(MoveRepository.STATUS_CANCELED)) {
      moveRemoveService.archiveMove(move);
      response.setFlash(I18n.get(IExceptionMessage.MOVE_ARCHIVE_OK));
    }
  }

  @SuppressWarnings("unchecked")
  public void deleteMultipleMoves(ActionRequest request, ActionResponse response) {
    try {
      List<Long> moveIds = (List<Long>) request.getContext().get("_ids");
      String flashMessage = I18n.get(IExceptionMessage.NO_MOVE_TO_REMOVE_OR_ARCHIVE);

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
                      I18n.get(IExceptionMessage.MOVE_ARCHIVE_OR_REMOVE_NOT_OK_NB), errorNB)
                  : String.format(I18n.get(IExceptionMessage.MOVE_ARCHIVE_OR_REMOVE_OK), moveNb);
        }
      }

      response.setFlash(flashMessage);
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

  public void updateInDayBookMode(ActionRequest request, ActionResponse response) {

    Move move = request.getContext().asType(Move.class);
    move = Beans.get(MoveRepository.class).find(move.getId());

    try {
      if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
          || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED) {
        Beans.get(MoveValidateService.class).updateInDayBookMode(move);
        response.setReload(true);
      }
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
    Move move =
        Beans.get(MoveRepository.class).find(request.getContext().asType(Move.class).getId());
    try {
      if (move.getMoveLineList() != null
          && !move.getMoveLineList().isEmpty()
          && (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
              || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED))) {
        Beans.get(MoveLineTaxService.class).autoTaxLineGenerate(move);

        if (request.getContext().get("_source").equals("autoTaxLineGenerateBtn")) {
          response.setReload(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void filterPartner(ActionRequest request, ActionResponse response) {
    Move move = request.getContext().asType(Move.class);
    if (move != null) {
      try {
        String domain = Beans.get(MoveViewHelperService.class).filterPartner(move);
        response.setAttr("partner", "domain", domain);
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  public void isHiddenMoveLineListViewer(ActionRequest request, ActionResponse response) {

    Move move = request.getContext().asType(Move.class);
    boolean isHidden = true;
    try {
      if (move.getMoveLineList() != null
          && move.getStatusSelect() < MoveRepository.STATUS_ACCOUNTED) {
        for (MoveLine moveLine : move.getMoveLineList()) {
          if (moveLine.getAmountPaid().compareTo(BigDecimal.ZERO) > 0
              || moveLine.getReconcileGroup() != null) {
            isHidden = false;
          }
        }
      }
      response.setAttr("$reconcileTags", "hidden", isHidden);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkRemoveLines(ActionRequest request, ActionResponse response) {
    try {
      Move moveView = request.getContext().asType(Move.class);
      if (moveView.getId() == null) {
        return;
      }
      Move moveBD = Beans.get(MoveRepository.class).find(moveView.getId());
      List<String> moveLineReconciledAndRemovedNameList = new ArrayList<>();
      for (MoveLine moveLineBD : moveBD.getMoveLineList()) {
        if (!moveView.getMoveLineList().contains(moveLineBD)) {
          if (moveLineBD.getReconcileGroup() != null) {
            moveLineReconciledAndRemovedNameList.add(moveLineBD.getName());
          }
        }
      }
      if (moveLineReconciledAndRemovedNameList != null
          && !moveLineReconciledAndRemovedNameList.isEmpty()) {
        response.setError(
            String.format(
                I18n.get(IExceptionMessage.MOVE_LINE_RECONCILE_LINE_CANNOT_BE_REMOVED),
                moveLineReconciledAndRemovedNameList.toString()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
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
              if (analyticAxisByCompany.getOrderSelect() == i) {
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
      Move move =
          Beans.get(MoveRepository.class).find(request.getContext().asType(Move.class).getId());
      Beans.get(MoveCounterPartService.class)
          .generateCounterpartMoveLine(move, this.extractDueDate(request));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void exceptionCounterpart(ActionRequest request, ActionResponse response) {
    try {
      Move move =
          Beans.get(MoveRepository.class).find(request.getContext().asType(Move.class).getId());
      Beans.get(MoveToolService.class).exceptionOnGenerateCounterpart(move);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setOriginAndDescriptionOnLines(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Beans.get(MoveToolService.class).setOriginAndDescriptionOnMoveLineList(move);
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

  public void validateOriginDescription(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move.getOrigin() == null && move.getDescription() == null) {
        response.setAlert(I18n.get(IExceptionMessage.MOVE_CHECK_ORIGIN_AND_DESCRIPTION));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validatePeriodPermission(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      Move move = request.getContext().asType(Move.class);
      PeriodServiceAccount periodServiceAccount = Beans.get(PeriodServiceAccount.class);
      User user = AuthUtils.getUser();
      Period period = move.getPeriod();
      boolean isAuthorizedToAccountOnPeriod =
          periodServiceAccount.isAuthorizedToAccountOnPeriod(period, user);

      Beans.get(PeriodService.class).checkClosedPeriod(period, isAuthorizedToAccountOnPeriod);
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
        response.setFlash(I18n.get(IExceptionMessage.NO_CUT_OFF_TO_APPLY));
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

  public void setMoveLineOriginDates(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      move = Beans.get(MoveLineControlService.class).setMoveLineOriginDates(move);
      response.setValue("moveLineList", move.getMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkDates(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveLineToolService moveLineService = Beans.get(MoveLineToolService.class);
      if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
        for (MoveLine moveline : move.getMoveLineList()) {
          moveLineService.checkDateInPeriod(move, moveline);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void filterJournalPartnerCompatibleType(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      String journalPartnerCompatibleDomain =
          Beans.get(JournalService.class).filterJournalPartnerCompatibleType(move);
      if (journalPartnerCompatibleDomain != null) {
        response.setAttr("partner", "domain", journalPartnerCompatibleDomain);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeJournal(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move.getPartner() != null) {
        boolean isPartnerNotCompatible =
            Beans.get(MoveCreateFromInvoiceService.class).isPartnerNotCompatible(move);
        if (isPartnerNotCompatible) {
          response.setValue("partner", null);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void roundInvoiceTermPercentages(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      move = Beans.get(MoveRepository.class).find(move.getId());
      Beans.get(MoveInvoiceTermService.class).roundInvoiceTermPercentages(move);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void isAuthorizedOnPeriod(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      User user = AuthUtils.getUser();
      response.setValue(
          "$validatePeriod",
          !Beans.get(PeriodServiceAccount.class)
              .isAuthorizedToAccountOnPeriod(move.getPeriod(), user));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkDuplicatedMoveOrigin(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      if (Beans.get(MoveToolService.class).checkMoveOriginIsDuplicated(move)) {
        response.setAlert(
            String.format(
                I18n.get(IExceptionMessage.MOVE_DUPLICATE_ORIGIN_NON_BLOCKING_MESSAGE),
                move.getReference(),
                move.getPartner() != null ? move.getPartner().getFullName() : "",
                move.getPeriod().getYear().getName()));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void updateInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      MoveInvoiceTermService moveInvoiceTermService = Beans.get(MoveInvoiceTermService.class);

      if (request.getContext().containsKey("paymentConditionChange")
          && (boolean) request.getContext().get("paymentConditionChange")) {
        Move move = request.getContext().asType(Move.class);
        move = Beans.get(MoveRepository.class).find(move.getId());

        moveInvoiceTermService.recreateInvoiceTerms(move);

        if (moveInvoiceTermService.displayDueDate(move)) {
          response.setAttr(
              "$dueDate", "value", moveInvoiceTermService.computeDueDate(move, true, false));
        }
      } else if (request.getContext().containsKey("headerChange")
          && (boolean) request.getContext().get("headerChange")) {
        Move move = request.getContext().asType(Move.class);
        move = Beans.get(MoveRepository.class).find(move.getId());

        boolean isAllUpdated = moveInvoiceTermService.updateInvoiceTerms(move);

        if (!isAllUpdated) {
          response.setFlash(I18n.get(IExceptionMessage.MOVE_INVOICE_TERM_CANNOT_UPDATE));
        }
      }

      response.setValue("$paymentConditionChange", false);
      response.setValue("$headerChange", false);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void updatePartner(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Move previousMove = Beans.get(MoveRepository.class).find(move.getId());

      if (previousMove != null && !Objects.equals(move.getPartner(), previousMove.getPartner())) {
        Beans.get(MoveLineService.class)
            .updatePartner(move.getMoveLineList(), move.getPartner(), previousMove.getPartner());

        response.setValue("moveLineList", move.getMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void displayAndComputeDueDate(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveInvoiceTermService moveInvoiceTermService = Beans.get(MoveInvoiceTermService.class);
      boolean displayDueDate = moveInvoiceTermService.displayDueDate(move);

      response.setAttr("$dueDate", "hidden", !displayDueDate);

      if (displayDueDate) {
        if (request.getContext().get("dueDate") == null) {
          boolean isDateChange =
              request.getContext().containsKey("dateChange")
                  && (boolean) request.getContext().get("dateChange");

          response.setAttr(
              "$dueDate", "value", moveInvoiceTermService.computeDueDate(move, true, isDateChange));
          response.setAttr("$dateChange", "value", false);
        }
      } else {
        response.setAttr("$dueDate", "value", null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  private LocalDate extractDueDate(ActionRequest request) {
    if (!request.getContext().containsKey("dueDate")
        || request.getContext().get("dueDate") == null) {
      return null;
    }

    Object dueDateObj = request.getContext().get("dueDate");
    if (dueDateObj.getClass() == LocalDate.class) {
      return (LocalDate) dueDateObj;
    } else {
      return LocalDate.parse((String) dueDateObj);
    }
  }

  public void updateDueDate(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().containsKey("dueDate")
          && request.getContext().get("dueDate") != null) {
        Move move = request.getContext().asType(Move.class);
        move = Beans.get(MoveRepository.class).find(move.getId());

        Beans.get(MoveInvoiceTermService.class)
            .updateSingleInvoiceTermDueDate(move, this.extractDueDate(request));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
