package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.account.service.moveline.MoveLineCurrencyService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;

public class MoveRecordUpdateServiceImpl implements MoveRecordUpdateService {

  protected MoveLineService moveLineService;
  protected MoveRepository moveRepository;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveValidateService moveValidateService;
  protected MoveLineCurrencyService moveLineCurrencyService;

  @Inject
  public MoveRecordUpdateServiceImpl(
      MoveLineService moveLineService,
      MoveRepository moveRepository,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveValidateService moveValidateService,
      MoveLineCurrencyService moveLineCurrencyService) {
    this.moveLineService = moveLineService;
    this.moveRepository = moveRepository;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.moveValidateService = moveValidateService;
    this.moveLineCurrencyService = moveLineCurrencyService;
  }

  @Override
  public void updatePartner(Move move) {
    if (move.getId() != null) {
      Move previousMove = moveRepository.find(move.getId());

      if (previousMove != null && !Objects.equals(move.getPartner(), previousMove.getPartner())) {
        moveLineService.updatePartner(
            move.getMoveLineList(), move.getPartner(), previousMove.getPartner());
      }
    }
  }

  @Override
  public MoveContext updateInvoiceTerms(
      Move move, boolean paymentConditionChange, boolean headerChange) throws AxelorException {
    Objects.requireNonNull(move);
    MoveContext result = new MoveContext();

    if (paymentConditionChange) {

      moveInvoiceTermService.recreateInvoiceTerms(move);

      if (moveInvoiceTermService.displayDueDate(move)) {
        LocalDate dueDate = moveInvoiceTermService.computeDueDate(move, true, false);
        result.putInAttrs("dueDate", "value", dueDate);
        move.setDueDate(dueDate);
      }
    } else if (headerChange) {

      boolean isAllUpdated = moveInvoiceTermService.updateInvoiceTerms(move);

      if (!isAllUpdated) {
        result.putInFlash(I18n.get(AccountExceptionMessage.MOVE_INVOICE_TERM_CANNOT_UPDATE));
      }
    }

    result.putInAttrs("$paymentConditionChange", "value", false);
    result.putInAttrs("$headerChange", "value", false);
    result.putInValues("moveLineList", move.getMoveLineList());
    return result;
  }

  @Override
  public MoveContext updateRoundInvoiceTermPercentages(Move move) {

    MoveContext result = new MoveContext();
    moveInvoiceTermService.roundInvoiceTermPercentages(move);
    result.putInValues("moveLineList", move.getMoveLineList());

    return result;
  }

  @Override
  public MoveContext updateInvoiceTermDueDate(Move move, LocalDate dueDate) {
    MoveContext result = new MoveContext();
    if (dueDate != null) {

      moveInvoiceTermService.updateSingleInvoiceTermDueDate(move, dueDate);
      result.putInValues("moveLineList", move.getMoveLineList());
    }

    return result;
  }

  @Override
  public void updateInDayBookMode(Move move) throws AxelorException {
    if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
        || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED) {
      moveValidateService.updateInDayBookMode(move);
    }
  }

  @Override
  public MoveContext updateMoveLinesCurrencyRate(Move move, LocalDate dueDate)
      throws AxelorException {

    MoveContext moveContext = new MoveContext();
    if (move != null
        && ObjectUtils.notEmpty(move.getMoveLineList())
        && move.getCurrency() != null
        && move.getCompanyCurrency() != null) {
      moveLineCurrencyService.computeNewCurrencyRateOnMoveLineList(move, dueDate);
      moveContext.putInValues("moveLineList", move.getMoveLineList());
    }
    return moveContext;
  }

  @Override
  public MoveContext updateDueDate(Move move, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException {
    Objects.requireNonNull(move);
    MoveContext moveContext = new MoveContext();

    boolean displayDueDate = moveInvoiceTermService.displayDueDate(move);

    moveContext.putInAttrs("dueDate", "hidden", !displayDueDate);

    if (displayDueDate) {

      if (move.getDueDate() == null || paymentConditionChange) {
        boolean isDateChange = dateChange || paymentConditionChange;

        LocalDate dueDate = moveInvoiceTermService.computeDueDate(move, true, isDateChange);
        move.setDueDate(dueDate);
        moveContext.putInValues("dueDate", dueDate);
        moveContext.putInAttrs("$dateChange", "value", false);
      }
    } else {
      move.setDueDate(null);
      moveContext.putInValues("dueDate", null);
    }
    return moveContext;
  }
}
