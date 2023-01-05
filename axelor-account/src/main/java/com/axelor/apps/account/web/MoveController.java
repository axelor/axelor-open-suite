/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.journal.JournalCheckPartnerTypeService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class MoveController {

  public void validate(ActionRequest request, ActionResponse response) {

    Move move = request.getContext().asType(Move.class);
    move = Beans.get(MoveRepository.class).find(move.getId());
    try {
      Beans.get(MoveValidateService.class).validate(move);
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

    Period period = null;
    try {
      if (move.getDate() != null && move.getCompany() != null) {
        period =
            Beans.get(PeriodService.class)
                .getActivePeriod(move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setValue("period", period);
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
  public void validateMultipleMoves(ActionRequest request, ActionResponse response) {
    List<Integer> moveIds = (List<Integer>) request.getContext().get("_ids");
    try {
      if (moveIds != null && !moveIds.isEmpty()) {
        boolean error = Beans.get(MoveValidateService.class).validateMultiple(moveIds);
        if (error) {
          response.setFlash(I18n.get(IExceptionMessage.MOVE_VALIDATION_NOT_OK));
        } else {
          response.setFlash(I18n.get(IExceptionMessage.MOVE_VALIDATION_OK));
          response.setReload(true);
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

      if (!move.getStatusSelect().equals(MoveRepository.STATUS_VALIDATED)) {
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
      response.setFlash(I18n.get(IExceptionMessage.MOVE_REMOVED_OK));
    } else if (move.getStatusSelect().equals(MoveRepository.STATUS_ACCOUNTED)) {
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
                    MoveRepository.STATUS_ACCOUNTED,
                    MoveRepository.STATUS_CANCELED,
                    MoveRepository.STATUS_SIMULATED)
                .fetch();

        if (!moveList.isEmpty()) {
          int errorNB = Beans.get(MoveRemoveService.class).deleteMultiple(moveList);

          flashMessage =
              errorNB > 0
                  ? String.format(
                      I18n.get(IExceptionMessage.MOVE_ARCHIVE_OR_REMOVE_NOT_OK_NB), errorNB)
                  : I18n.get(IExceptionMessage.MOVE_ARCHIVE_OR_REMOVE_OK);
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
      if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
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
        response.setReload(true);
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
          && move.getStatusSelect() < MoveRepository.STATUS_VALIDATED) {
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
      Beans.get(MoveCounterPartService.class).generateCounterpartMoveLine(move);
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

  public void setOriginOnLines(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Beans.get(MoveToolService.class).setOriginOnMoveLineList(move);
      response.setValue("moveLineList", move.getMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
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

  public void validateOrigin(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move.getOrigin() == null) {
        response.setAlert(I18n.get(IExceptionMessage.MOVE_CHECK_ORIGIN));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateDescription(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move.getDescription() == null) {
        response.setAlert(I18n.get(IExceptionMessage.MOVE_CHECK_DESCRIPTION));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
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

  public void onChangeJournal(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move.getPartner() != null) {
        boolean isPartnerCompatible =
            Beans.get(JournalCheckPartnerTypeService.class)
                .isPartnerCompatible(move.getJournal(), move.getPartner());
        if (!isPartnerCompatible) {
          response.setValue("partner", null);
          response.setNotify(
              I18n.get(IExceptionMessage.MOVE_PARTNER_IS_NOT_COMPATIBLE_WITH_SELECTED_JOURNAL));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
