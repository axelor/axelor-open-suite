package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;
import java.util.Optional;

public class MoveRecordServiceImpl implements MoveRecordService {

  protected MoveDefaultService moveDefaultService;
  protected MoveAttrsService moveAttrsService;
  protected PeriodServiceAccount periodAccountService;
  protected MoveCheckService moveCheckService;
  protected MoveComputeService moveComputeService;
  protected MoveRecordUpdateService moveRecordUpdateService;
  protected MoveRecordSetService moveRecordSetService;
  protected MoveRepository moveRepository;
  protected AppAccountService appAccountService;

  @Inject
  public MoveRecordServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodServiceAccount periodAccountService,
      MoveCheckService moveCheckService,
      MoveComputeService moveComputeService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveRepository moveRepository,
      AppAccountService appAccountService) {
    this.moveDefaultService = moveDefaultService;
    this.moveAttrsService = moveAttrsService;
    this.periodAccountService = periodAccountService;
    this.moveCheckService = moveCheckService;
    this.moveComputeService = moveComputeService;
    this.moveRecordUpdateService = moveRecordUpdateService;
    this.moveRepository = moveRepository;
    this.moveRecordSetService = moveRecordSetService;
    this.appAccountService = appAccountService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveBefore(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    boolean paymentConditionChange =
        Optional.ofNullable(context.get("paymentConditionChange"))
            .map(value -> (Boolean) value)
            .orElse(false);
    boolean headerChange =
        Optional.ofNullable(context.get("headerChange"))
            .map(value -> (Boolean) value)
            .orElse(false);

    moveRecordUpdateService.updatePartner(move);
    result.merge(
        moveRecordUpdateService.updateInvoiceTerms(move, paymentConditionChange, headerChange));
    result.merge(moveRecordUpdateService.updateInvoiceTermDueDate(move, move.getDueDate()));

    return result;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveCheck(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    moveCheckService.checkDates(move);
    moveCheckService.checkPeriodPermission(move);
    moveCheckService.checkRemovedLines(move);
    moveCheckService.checkAnalyticAccount(move);

    return result;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveAfter(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    move = moveRepository.find(move.getId());
    MoveContext result = new MoveContext();

    // No need to merge result
    moveRecordUpdateService.updateRoundInvoiceTermPercentages(move);

    // Move will be saved again in this method
    moveRecordUpdateService.updateInDayBookMode(move);

    return result;
  }

  @Override
  public MoveContext onNew(Move move, User user) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveDefaultService.setDefaultMoveValues(move));
    result.putInValues(moveDefaultService.setDefaultCurrency(move));
    result.putInValues(moveRecordSetService.setJournal(move));
    moveRecordSetService.setPeriod(move);
    result.putInValues("period", move.getPeriod());
    if (move.getJournal() != null && move.getJournal().getIsFillOriginDate()) {
      result.putInValues(moveRecordSetService.setOriginDate(move));
    }
    result.putInAttrs(moveAttrsService.getHiddenAttributeValues(move));
    result.putInAttrs(
        "$reconcileTags", "hidden", moveAttrsService.isHiddenMoveLineListViewer(move));
    result.putInValues(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
    result.putInValues(moveCheckService.checkPeriodAndStatus(move));
    result.putInAttrs(moveAttrsService.getFunctionalOriginSelectDomain(move));
    result.putInValues(moveRecordSetService.setFunctionalOriginSelect(move));
    moveCheckService.checkPeriodPermission(move);
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));

    if (appAccountService.getAppAccount().getActivatePassedForPayment()) {
      result.putInAttrs(moveAttrsService.getPfpAttrs(move, user));
      result.putInValues(moveRecordSetService.setPfpStatus(move));
    }

    return result;
  }

  @Override
  public MoveContext onLoad(Move move, Context context, User user) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    result.putInAttrs(moveAttrsService.getHiddenAttributeValues(move));
    result.putInValues(moveComputeService.computeTotals(move));
    result.putInAttrs(
        "$reconcileTags", "hidden", moveAttrsService.isHiddenMoveLineListViewer(move));
    result.putInValues(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
    result.putInValues(
        "$isThereRelatedCutOffMoves", moveCheckService.checkRelatedCutoffMoves(move));
    result.putInValues(moveCheckService.checkPeriodAndStatus(move));
    result.putInAttrs(moveAttrsService.getFunctionalOriginSelectDomain(move));
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));
    result.putInAttrs("dueDate", "hidden", moveAttrsService.isHiddenDueDate(move));

    if (appAccountService.getAppAccount().getActivatePassedForPayment()) {
      result.putInAttrs(moveAttrsService.getPfpAttrs(move, user));
    }

    return result;
  }

  @Override
  public MoveContext onChangeDate(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    boolean paymentConditionChange =
        Optional.ofNullable(context.get("paymentConditionChange"))
            .map(value -> (Boolean) value)
            .orElse(false);
    boolean dateChange =
        Optional.ofNullable(context.get("dateChange")).map(value -> (Boolean) value).orElse(false);

    result.putInValues(moveRecordSetService.setPeriod(move));
    result.putInValues(
        "$validatePeriod",
        !periodAccountService.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser()));
    moveCheckService.checkPeriodPermission(move);
    result.putInValues(moveRecordSetService.setMoveLineDates(move));
    if (move.getJournal() != null && move.getJournal().getIsFillOriginDate()) {
      result.putInValues(moveRecordSetService.setOriginDate(move));
      onChangeOriginDate(move, context);
    }
    result.merge(moveRecordUpdateService.updateMoveLinesCurrencyRate(move, move.getDueDate()));
    result.putInValues(moveComputeService.computeTotals(move));
    updateDummiesDateConText(move, context);
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));

    return result;
  }

  protected void updateDummiesDateConText(Move move, Context context) {
    move.setDueDate(null);
    context.put("$dateChange", true);
  }

  @Override
  public MoveContext onChangeJournal(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    result.putInAttrs(moveAttrsService.getFunctionalOriginSelectDomain(move));
    result.putInValues(moveRecordSetService.setFunctionalOriginSelect(move));
    checkPartnerCompatible(move, result);
    result.putInValues(moveRecordSetService.setPaymentMode(move));
    result.putInValues(moveRecordSetService.setPaymentCondition(move));
    result.putInValues(moveRecordSetService.setPartnerBankDetails(move));

    if (move.getJournal() != null && move.getJournal().getIsFillOriginDate()) {
      result.putInValues(moveRecordSetService.setOriginDate(move));
    }

    if (appAccountService.getAppAccount().getActivatePassedForPayment()) {
      result.putInValues(moveRecordSetService.setPfpStatus(move));
    }

    return result;
  }

  protected void checkPartnerCompatible(Move move, MoveContext result) {
    try {
      moveCheckService.checkPartnerCompatible(move);
    } catch (AxelorException e) {
      result.putInValues("partner", null);
      result.putInNotify(e.getMessage());
    }
  }

  @Override
  public MoveContext onChangePartner(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    boolean paymentConditionChange =
        Optional.ofNullable(context.get("paymentConditionChange"))
            .map(value -> (Boolean) value)
            .orElse(false);
    boolean dateChange =
        Optional.ofNullable(context.get("dateChange")).map(value -> (Boolean) value).orElse(false);

    result.putInValues(moveRecordSetService.setCurrencyByPartner(move));
    result.putInValues(moveRecordSetService.setPaymentMode(move));
    result.putInValues(moveRecordSetService.setPaymentCondition(move));
    result.putInValues(moveRecordSetService.setPartnerBankDetails(move));
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));
    result.putInAttrs(moveAttrsService.getHiddenAttributeValues(move));
    result.putInValues(moveRecordSetService.setCompanyBankDetails(move));

    if (appAccountService.getAppAccount().getActivatePassedForPayment()
        && move.getPfpValidateStatusSelect() > MoveRepository.PFP_NONE) {
      result.putInValues(moveRecordSetService.setPfpValidatorUser(move));
    }

    return result;
  }

  @Override
  public MoveContext onChangeMoveLineList(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    boolean paymentConditionChange =
        Optional.ofNullable(context.get("paymentConditionChange"))
            .map(value -> (Boolean) value)
            .orElse(false);
    boolean dateChange =
        Optional.ofNullable(context.get("dateChange")).map(value -> (Boolean) value).orElse(false);

    result.putInValues(moveComputeService.computeTotals(move));
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));

    return result;
  }

  @Override
  public MoveContext onChangeOriginDate(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    boolean paymentConditionChange =
        Optional.ofNullable(context.get("paymentConditionChange"))
            .map(value -> (Boolean) value)
            .orElse(false);
    boolean dateChange =
        Optional.ofNullable(context.get("dateChange")).map(value -> (Boolean) value).orElse(false);

    checkDuplicateOriginMove(move, result);
    result.putInValues(moveRecordSetService.setMoveLineOriginDates(move));
    updateDummiesDateConText(move, context);
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));
    result.putInAttrs("$paymentConditionChange", "value", true);

    return result;
  }

  protected void checkDuplicateOriginMove(Move move, MoveContext result) {
    try {
      moveCheckService.checkDuplicatedMoveOrigin(move);
    } catch (AxelorException e) {
      result.putInAlert(e.getMessage());
    }
  }

  @Override
  public MoveContext onChangeOrigin(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    checkOrigin(move, result);
    checkDuplicateOriginMove(move, result);
    result.putInValues(moveRecordSetService.setOriginOnMoveLineList(move));

    return result;
  }

  protected void checkOrigin(Move move, MoveContext result) {
    try {
      moveCheckService.checkOrigin(move);
    } catch (AxelorException e) {
      result.putInAlert(e.getMessage());
    }
  }

  @Override
  public MoveContext onChangePaymentCondition(Move move, Context context) throws AxelorException {

    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    boolean paymentConditionChange =
        Optional.ofNullable(context.get("paymentConditionChange"))
            .map(value -> (Boolean) value)
            .orElse(false);
    boolean headerChange =
        Optional.ofNullable(context.get("headerChange"))
            .map(value -> (Boolean) value)
            .orElse(false);
    boolean dateChange =
        Optional.ofNullable(context.get("dateChange")).map(value -> (Boolean) value).orElse(false);

    MoveContext result = new MoveContext();

    result.merge(moveCheckService.checkTermsInPayment(move));
    if (!result.getError().isEmpty()) {
      return result;
    }
    result.putInAttrs("$paymentConditionChange", "value", true);
    paymentConditionChange = true;
    result.merge(
        moveRecordUpdateService.updateInvoiceTerms(move, paymentConditionChange, headerChange));
    result.merge(moveRecordUpdateService.updateInvoiceTermDueDate(move, move.getDueDate()));
    result.merge(moveRecordUpdateService.updateDueDate(move, paymentConditionChange, dateChange));

    return result;
  }
}
