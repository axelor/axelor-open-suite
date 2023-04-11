package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.account.service.moveline.MoveLineCurrencyService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
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
      move = moveRepository.find(move.getId());

      moveInvoiceTermService.recreateInvoiceTerms(move);

      if (moveInvoiceTermService.displayDueDate(move)) {
        result.putInAttrs(
            "dueDate", "value", moveInvoiceTermService.computeDueDate(move, true, false));
      }
    } else if (headerChange) {
      move = moveRepository.find(move.getId());

      boolean isAllUpdated = moveInvoiceTermService.updateInvoiceTerms(move);

      if (!isAllUpdated) {
        result.putInFlash(I18n.get(AccountExceptionMessage.MOVE_INVOICE_TERM_CANNOT_UPDATE));
      }
    }

    result.putInValues("$paymentConditionChange", false);
    result.putInValues("$headerChange", false);
    return result;
  }

  @Override
  public void updateRoundInvoiceTermPercentages(Move move) {
    moveInvoiceTermService.roundInvoiceTermPercentages(move);
  }

  @Override
  public void updateDueDate(Move move, LocalDate dueDate) {
    if (dueDate != null) {
      move = moveRepository.find(move.getId());

      moveInvoiceTermService.updateSingleInvoiceTermDueDate(move, dueDate);
    }
  }

  @Override
  public void updateInDayBookMode(Move move) throws AxelorException {
    if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
        || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED) {
      moveValidateService.updateInDayBookMode(move);
    }
  }

  @Override
  public void updateMoveLinesCurrencyRate(Move move, LocalDate dueDate) throws AxelorException {

    if (move != null
        && ObjectUtils.notEmpty(move.getMoveLineList())
        && move.getCurrency() != null
        && move.getCompanyCurrency() != null) {
      moveLineCurrencyService.computeNewCurrencyRateOnMoveLineList(move, dueDate);
    }
  }
}
