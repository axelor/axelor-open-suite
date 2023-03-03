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
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class MassEntryServiceImpl implements MassEntryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MassEntryToolService massEntryToolService;
  protected MoveToolService moveToolService;
  protected MoveLineTaxService moveLineTaxService;
  protected MoveCounterPartService moveCounterPartService;

  @Inject
  public MassEntryServiceImpl(
      MassEntryToolService massEntryToolService,
      MoveToolService moveToolService,
      MoveLineTaxService moveLineTaxService,
      MoveCounterPartService moveCounterPartService) {
    this.massEntryToolService = massEntryToolService;
    this.moveToolService = moveToolService;
    this.moveLineTaxService = moveLineTaxService;
    this.moveCounterPartService = moveCounterPartService;
  }

  public void fillMoveLineListWithMoveLineMassEntryList(Move move, Integer temporaryMoveNumber) {
    List<MoveLine> moveLineList = new ArrayList<>();
    boolean firstLine = true;

    for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
      if (Objects.equals(moveLineMassEntry.getTemporaryMoveNumber(), temporaryMoveNumber)
          && moveLineMassEntry.getInputAction() == 1) {
        if (firstLine) {
          move.setPaymentMode(moveLineMassEntry.getMovePaymentMode());
          move.setPaymentCondition(moveLineMassEntry.getMovePaymentCondition());
          move.setDate(moveLineMassEntry.getDate());

          // TODO Need to be seen, to enable multiPartners in a Move and remove this line
          move.setPartner(moveLineMassEntry.getPartner());
          firstLine = false;
        }
        moveLineMassEntry.setMove(move);
        moveLineList.add(moveLineMassEntry);
      }
    }
    move.setMoveLineList(moveLineList);
    massEntryToolService.clearMoveLineMassEntryListAndAddNewLines(move, temporaryMoveNumber);
  }

  public void generateTaxLineAndCounterpart(
      Move move, LocalDate dueDate, Integer temporaryMoveNumber) throws AxelorException {
    if (ObjectUtils.notEmpty(move.getMoveLineList())) {
      moveToolService.exceptionOnGenerateCounterpart(move.getJournal(), move.getPaymentMode());
      if (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
          || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
        moveLineTaxService.autoTaxLineGenerate(move);
        moveCounterPartService.generateCounterpartMoveLine(move, dueDate);
      }
      massEntryToolService.clearMoveLineMassEntryListAndAddNewLines(move, temporaryMoveNumber);
    }
  }

  public MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineMassEntryList, MoveLineMassEntry moveLineMassEntry) {
    for (MoveLineMassEntry moveLine : moveLineMassEntryList) {
      if (moveLine.getTemporaryMoveNumber().equals(moveLineMassEntry.getTemporaryMoveNumber())) {
        moveLineMassEntry.setPartner(moveLine.getPartner());
        moveLineMassEntry.setDate(moveLine.getDate());
        moveLineMassEntry.setDueDate(moveLine.getDueDate());
        moveLineMassEntry.setOriginDate(moveLine.getOriginDate());
        moveLineMassEntry.setOrigin(moveLine.getOrigin());
        moveLineMassEntry.setMoveStatusSelect(moveLine.getMoveStatusSelect());
        moveLineMassEntry.setInterbankCodeLine(moveLine.getInterbankCodeLine());
        moveLineMassEntry.setMoveDescription(moveLine.getMoveDescription());
        moveLineMassEntry.setDescription(moveLine.getDescription());
        moveLineMassEntry.setExportedDirectDebitOk(moveLine.getExportedDirectDebitOk());
        moveLineMassEntry.setMovePaymentCondition(moveLine.getMovePaymentCondition());
        moveLineMassEntry.setMovePaymentMode(moveLine.getMovePaymentMode());
        break;
      }
    }
    return moveLineMassEntry;
  }

  public void resetMoveLineMassEntry(MoveLineMassEntry moveLineMassEntry) {
    moveLineMassEntry.setOrigin(null);
    moveLineMassEntry.setOriginDate(null);
    moveLineMassEntry.setPartner(null);
    moveLineMassEntry.setMoveDescription(null);
    moveLineMassEntry.setMovePaymentCondition(null);
    moveLineMassEntry.setMovePaymentMode(null);
    moveLineMassEntry.setAccount(null);
    moveLineMassEntry.setTaxLine(null);
    moveLineMassEntry.setDescription(null);
    moveLineMassEntry.setDebit(BigDecimal.ZERO);
    moveLineMassEntry.setCredit(BigDecimal.ZERO);
    moveLineMassEntry.setCurrencyRate(BigDecimal.ONE);
    moveLineMassEntry.setCurrencyAmount(BigDecimal.ZERO);
    moveLineMassEntry.setMoveStatusSelect(null);
    moveLineMassEntry.setVatSystemSelect(0);
  }
}
