package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class MoveRecordServiceImpl implements MoveRecordService {

  protected MoveDefaultService moveDefaultService;
  protected MoveAttrsService moveAttrsService;
  protected PeriodServiceAccount periodAccountService;
  protected MoveCheckService moveCheckService;
  protected MoveComputeService moveComputeService;
  protected MoveRecordUpdateService moveRecordUpdateService;
  protected MoveRecordSetService moveRecordSetService;
  protected MoveRepository moveRepository;

  @Inject
  public MoveRecordServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodServiceAccount periodAccountService,
      MoveCheckService moveCheckService,
      MoveComputeService moveComputeService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveRepository moveRepository) {
    this.moveDefaultService = moveDefaultService;
    this.moveAttrsService = moveAttrsService;
    this.periodAccountService = periodAccountService;
    this.moveCheckService = moveCheckService;
    this.moveComputeService = moveComputeService;
    this.moveRecordUpdateService = moveRecordUpdateService;
    this.moveRepository = moveRepository;
    this.moveRecordSetService = moveRecordSetService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveBefore(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    moveCheckService.checkDates(move);
    moveCheckService.checkPeriodPermission(move);
    moveCheckService.checkRemovedLines(move);
    moveCheckService.checkAnalyticAccount(move);
    moveRecordUpdateService.updatePartner(move);

    return result;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveAfter(Move move, Context context) throws AxelorException {
    Objects.requireNonNull(move);
    Objects.requireNonNull(context);

    MoveContext result = new MoveContext();

    result.merge(moveRecordUpdateService.updateInvoiceTerms(move, context));
    moveRecordUpdateService.updateRoundInvoiceTermPercentages(move);
    moveRecordUpdateService.updateDueDate(move, context);
    moveRecordUpdateService.updateInDayBookMode(move);

    return result;
  }

  @Override
  public MoveContext onNew(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    MoveContext result = new MoveContext();

    result.putInValues(moveDefaultService.setDefaultMoveValues(move));
    result.putInValues(moveDefaultService.setDefaultCurrency(move));
    result.putInValues(moveRecordSetService.setJournal(move));
    moveRecordSetService.setPeriod(move);
    result.putInValues("period", move.getPeriod());
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

    return result;
  }

  @Override
  public MoveContext onLoad(Move move, Context context) throws AxelorException {
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
    // result.putInAttrs(moveAttrsService.computeAndGetDueDate(move, context));
    result.putInAttrs(moveAttrsService.getMoveLineAnalyticAttrs(move));

    return result;
  }
}
