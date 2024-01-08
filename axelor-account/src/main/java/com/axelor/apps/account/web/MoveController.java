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
package com.axelor.apps.account.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.record.MoveGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
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

  public void onNew(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      response.setValues(moveGroupService.getOnNewValuesMap(move));
      response.setAttrs(moveGroupService.getOnNewAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onLoad(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      response.setValues(moveGroupService.getOnLoadValuesMap(move));
      response.setAttrs(moveGroupService.getOnLoadAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onSaveCheck(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      Beans.get(MoveGroupService.class).checkBeforeSave(move);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onSave(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);
      move = Beans.get(MoveRepository.class).find(move.getId());

      boolean paymentConditionChange =
          this.getChangeDummyBoolean(context, "paymentConditionChange");
      boolean dateChange = this.getChangeDummyBoolean(context, "dateChange");
      boolean headerChange = this.getChangeDummyBoolean(context, "headerChange");

      Beans.get(MoveGroupService.class)
          .onSave(move, paymentConditionChange, dateChange, headerChange);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeDate(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      boolean paymentConditionChange =
          this.getChangeDummyBoolean(context, "paymentConditionChange");

      response.setValues(moveGroupService.getDateOnChangeValuesMap(move));
      response.setAttrs(moveGroupService.getDateOnChangeAttrsMap(move, paymentConditionChange));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeJournal(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      response.setValues(moveGroupService.getJournalOnChangeValuesMap(move));
      response.setAttrs(moveGroupService.getJournalOnChangeAttrsMap(move));

      if (!Beans.get(MoveCheckService.class).isPartnerCompatible(move)) {
        response.setNotify(
            I18n.get(AccountExceptionMessage.MOVE_PARTNER_IS_NOT_COMPATIBLE_WITH_SELECTED_JOURNAL));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangePartner(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      boolean paymentConditionChange =
          this.getChangeDummyBoolean(context, "paymentConditionChange");
      boolean dateChange = this.getChangeDummyBoolean(context, "dateChange");

      response.setValues(
          moveGroupService.getPartnerOnChangeValuesMap(move, paymentConditionChange, dateChange));
      response.setAttrs(moveGroupService.getPartnerOnChangeAttrsMap(move, paymentConditionChange));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeMoveLineList(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      boolean paymentConditionChange =
          this.getChangeDummyBoolean(context, "paymentConditionChange");
      boolean dateChange = this.getChangeDummyBoolean(context, "dateChange");

      response.setValues(
          moveGroupService.getMoveLineListOnChangeValuesMap(
              move, paymentConditionChange, dateChange));
      response.setAttrs(
          moveGroupService.getMoveLineListOnChangeAttrsMap(move, paymentConditionChange));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeOriginDate(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      boolean paymentConditionChange =
          this.getChangeDummyBoolean(context, "paymentConditionChange");

      response.setValues(
          moveGroupService.getOriginDateOnChangeValuesMap(move, paymentConditionChange));
      response.setAttrs(
          moveGroupService.getOriginDateOnChangeAttrsMap(move, paymentConditionChange));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeOrigin(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setValues(Beans.get(MoveGroupService.class).getOriginOnChangeValuesMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangePaymentCondition(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Move move = context.asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      boolean dateChange = this.getChangeDummyBoolean(context, "dateChange");
      boolean headerChange = this.getChangeDummyBoolean(context, "headerChange");

      Map<String, Object> valuesMap =
          moveGroupService.getPaymentConditionOnChangeValuesMap(move, dateChange, headerChange);

      response.setValues(valuesMap);

      if (valuesMap.containsKey("info")) {
        response.setInfo((String) valuesMap.get("info"));
      } else {
        response.setAttrs(moveGroupService.getPaymentConditionOnChangeAttrsMap(move));

        if (valuesMap.containsKey("flash") && valuesMap.get("flash") != null) {
          response.setNotify((String) valuesMap.get("flash"));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onClickGenerateCounterpart(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      LocalDate dueDate = this.extractDueDate(request);

      response.setValues(
          Beans.get(MoveGroupService.class).getGenerateCounterpartOnClickValuesMap(move, dueDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onClickGenerateTaxLines(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setValues(
          Beans.get(MoveGroupService.class).getGenerateTaxLinesOnClickValuesMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeDescription(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setValues(Beans.get(MoveGroupService.class).getDescriptionOnChangeValuesMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onChangeCompany(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      response.setValues(moveGroupService.getCompanyOnChangeValuesMap(move));
      response.setAttrs(moveGroupService.getCompanyOnChangeAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onChangePaymentMode(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MoveGroupService moveGroupService = Beans.get(MoveGroupService.class);

      response.setValues(moveGroupService.getPaymentModeOnChangeValuesMap(move));
      response.setAttrs(moveGroupService.getHeaderChangeAttrsMap());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onSelectPartner(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setAttrs(Beans.get(MoveGroupService.class).getPartnerOnSelectAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onClickApplyCutOffDates(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      LocalDate cutOffStartDate =
          LocalDate.parse((String) request.getContext().get("cutOffStartDate"));
      LocalDate cutOffEndDate = LocalDate.parse((String) request.getContext().get("cutOffEndDate"));

      response.setValues(
          Beans.get(MoveGroupService.class)
              .getApplyCutOffDatesOnClickValuesMap(move, cutOffStartDate, cutOffEndDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onChangeCurrency(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setValues(Beans.get(MoveGroupService.class).getCurrencyOnChangeValuesMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onChangeFiscalPosition(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setValues(
          Beans.get(MoveGroupService.class).getFiscalPositionOnChangeValuesMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onChangeDateOfReversionSelect(ActionRequest request, ActionResponse response) {
    try {
      LocalDate moveDate = LocalDate.parse((String) request.getContext().get("_moveDate"));
      int dateOfReversionSelect = (int) request.getContext().get("dateOfReversionSelect");

      response.setValues(
          Beans.get(MoveGroupService.class)
              .getDateOfReversionSelectOnChangeValuesMap(moveDate, dateOfReversionSelect));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onSelectPaymentMode(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setAttrs(Beans.get(MoveGroupService.class).getPaymentModeOnSelectAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onSelectPartnerBankDetails(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setAttrs(
          Beans.get(MoveGroupService.class).getPartnerBankDetailsOnSelectAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onSelectTradingName(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      response.setAttrs(Beans.get(MoveGroupService.class).getTradingNameOnSelectAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onChangePartnerBankDetails(ActionRequest request, ActionResponse response) {
    try {
      response.setAttrs(Beans.get(MoveGroupService.class).getHeaderChangeAttrsMap());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void wizardDefault(ActionRequest request, ActionResponse response) {
    try {
      LocalDate moveDate = LocalDate.parse((String) request.getContext().get("_moveDate"));

      response.setAttrs(Beans.get(MoveGroupService.class).getWizardDefaultAttrsMap(moveDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkDuplicateOriginMove(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      String alert = Beans.get(MoveCheckService.class).getDuplicatedMoveOriginAlert(move);

      if (StringUtils.notEmpty(alert)) {
        response.setAlert(alert);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkOrigin(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      String alert = Beans.get(MoveCheckService.class).getOriginAlert(move);

      if (StringUtils.notEmpty(alert)) {
        response.setAlert(alert);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkDescription(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      String alert = Beans.get(MoveCheckService.class).getDescriptionAlert(move);

      if (StringUtils.notEmpty(alert)) {
        response.setAlert(alert);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkAccounting(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      String alert = Beans.get(MoveCheckService.class).getAccountingAlert(move);

      if (StringUtils.notEmpty(alert)) {
        response.setAlert(alert);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkPeriod(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      String alert = Beans.get(MoveCheckService.class).getPeriodAlert(move);

      if (StringUtils.notEmpty(alert)) {
        response.setAlert(alert);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected boolean getChangeDummyBoolean(Context context, String name) {
    return Optional.ofNullable(context.get(name)).map(value -> (Boolean) value).orElse(false);
  }

  public void checkPeriodPermission(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      Beans.get(MoveCheckService.class).checkPeriodPermission(move);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
