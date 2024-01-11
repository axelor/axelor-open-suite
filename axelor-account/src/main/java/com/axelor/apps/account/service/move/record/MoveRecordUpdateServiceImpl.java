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
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCurrencyService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;

public class MoveRecordUpdateServiceImpl implements MoveRecordUpdateService {

  protected AppBaseService appBaseService;
  protected MoveLineService moveLineService;
  protected MoveRepository moveRepository;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveValidateService moveValidateService;
  protected MoveLineCurrencyService moveLineCurrencyService;

  @Inject
  public MoveRecordUpdateServiceImpl(
      AppBaseService appBaseService,
      MoveLineService moveLineService,
      MoveRepository moveRepository,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveValidateService moveValidateService,
      MoveLineCurrencyService moveLineCurrencyService) {
    this.appBaseService = appBaseService;
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
  public String updateInvoiceTerms(Move move, boolean paymentConditionChange, boolean headerChange)
      throws AxelorException {
    String flashMessage = null;

    if (paymentConditionChange) {
      moveInvoiceTermService.recreateInvoiceTerms(move);

      if (moveInvoiceTermService.displayDueDate(move)) {
        LocalDate dueDate = moveInvoiceTermService.computeDueDate(move, true, false);
        move.setDueDate(dueDate);
      }
    } else if (headerChange) {
      boolean isAllUpdated = moveInvoiceTermService.updateInvoiceTerms(move);

      if (!isAllUpdated) {
        flashMessage = I18n.get(AccountExceptionMessage.MOVE_INVOICE_TERM_CANNOT_UPDATE);
      }
    }

    return flashMessage;
  }

  @Override
  public void updateInvoiceTermDueDate(Move move, LocalDate dueDate) {
    if (dueDate != null) {
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
  public void updateMoveLinesCurrencyRate(Move move) throws AxelorException {
    if (move != null
        && ObjectUtils.notEmpty(move.getMoveLineList())
        && move.getCurrency() != null
        && move.getCompanyCurrency() != null) {
      moveLineCurrencyService.computeNewCurrencyRateOnMoveLineList(move, move.getDueDate());
    }
  }

  @Override
  public void updateDueDate(Move move, boolean paymentConditionChange, boolean dateChange) {
    if (moveInvoiceTermService.displayDueDate(move)) {
      if (move.getDueDate() == null || paymentConditionChange) {
        boolean isDateChange = dateChange || paymentConditionChange;
        LocalDate dueDate = moveInvoiceTermService.computeDueDate(move, true, isDateChange);

        move.setDueDate(dueDate);
      }
    } else {
      move.setDueDate(null);
    }
  }

  @Override
  public LocalDate getDateOfReversion(LocalDate moveDate, int dateOfReversionSelect) {
    switch (dateOfReversionSelect) {
      case MoveRepository.DATE_OF_REVERSION_TODAY:
        return appBaseService.getTodayDate(null);
      case MoveRepository.DATE_OF_REVERSION_ORIGINAL_MOVE_DATE:
        return moveDate;
      case MoveRepository.DATE_OF_REVERSION_TOMORROW:
        return appBaseService.getTodayDate(null).plusDays(1);
      default:
        return null;
    }
  }

  @Override
  public void resetDueDate(Move move) {
    if (move.getPaymentCondition() == null || !moveInvoiceTermService.displayDueDate(move)) {
      move.setDueDate(null);
    }
  }
}
