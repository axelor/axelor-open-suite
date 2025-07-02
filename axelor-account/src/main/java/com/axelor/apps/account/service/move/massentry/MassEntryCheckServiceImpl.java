/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RequestScoped
public class MassEntryCheckServiceImpl implements MassEntryCheckService {

  protected MassEntryToolService massEntryToolService;
  protected MassEntryVerificationService massEntryVerificationService;
  protected MoveToolService moveToolService;
  protected MoveLineMassEntryService moveLineMassEntryService;
  protected MassEntryMoveCreateService massEntryMoveCreateService;

  @Inject
  public MassEntryCheckServiceImpl(
      MassEntryToolService massEntryToolService,
      MassEntryVerificationService massEntryVerificationService,
      MoveToolService moveToolService,
      MoveLineMassEntryService moveLineMassEntryService,
      MassEntryMoveCreateService massEntryMoveCreateService) {
    this.massEntryToolService = massEntryToolService;
    this.massEntryVerificationService = massEntryVerificationService;
    this.moveToolService = moveToolService;
    this.moveLineMassEntryService = moveLineMassEntryService;
    this.massEntryMoveCreateService = massEntryMoveCreateService;
  }

  @Override
  public void verifyFieldsAndGenerateTaxLineAndCounterpart(Move parentMove, LocalDate dueDate)
      throws AxelorException {
    if (ObjectUtils.notEmpty(parentMove.getMoveLineMassEntryList())) {
      this.verifyFieldsChangeOnMoveLineMassEntry(parentMove, parentMove.getMassEntryManageCutOff());

      MoveLineMassEntry lastLine =
          parentMove
              .getMoveLineMassEntryList()
              .get(parentMove.getMoveLineMassEntryList().size() - 1);
      int inputAction = lastLine.getInputAction();
      int temporaryMoveNumber = lastLine.getTemporaryMoveNumber();

      if ((ObjectUtils.notEmpty(inputAction)
          && inputAction == MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_COUNTERPART)) {
        parentMove
            .getMoveLineMassEntryList()
            .remove(parentMove.getMoveLineMassEntryList().size() - 1);
        Move workingMove =
            massEntryMoveCreateService.createMoveFromMassEntryList(parentMove, temporaryMoveNumber);

        generatedTaxAndCounterPart(parentMove, workingMove, dueDate, temporaryMoveNumber);
      }
    }
  }

  @Override
  public void verifyFieldsChangeOnMoveLineMassEntry(Move parentMove, boolean manageCutOff)
      throws AxelorException {
    List<MoveLineMassEntry> moveLineList =
        massEntryToolService.getEditedMoveLineMassEntry(parentMove.getMoveLineMassEntryList());

    parentMove.setMassEntryErrors(null);
    if (ObjectUtils.notEmpty(moveLineList)) {
      for (MoveLineMassEntry moveLineEdited : moveLineList) {
        for (MoveLineMassEntry moveLine : parentMove.getMoveLineMassEntryList()) {
          moveLine.setFieldsErrorList(null);
          if (Objects.equals(
              moveLine.getTemporaryMoveNumber(), moveLineEdited.getTemporaryMoveNumber())) {
            moveLine.setMoveStatusSelect(MoveRepository.STATUS_NEW);
            massEntryVerificationService.checkChangesMassEntryMoveLine(
                moveLine, parentMove, moveLineEdited, manageCutOff);
            moveLine.setIsEdited(MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_NULL);
          }
        }
      }
    }
  }

  @Override
  public void checkMassEntryMoveGeneration(Move move) throws AxelorException {
    List<Move> moveList;
    boolean authorizeSimulatedMove = move.getJournal().getAuthorizeSimulatedMove();
    boolean allowAccountingDaybook = move.getJournal().getAllowAccountingDaybook();

    moveList = massEntryMoveCreateService.createMoveListFromMassEntryList(move);
    move.setMassEntryErrors("");

    for (Move element : moveList) {
      int newMoveStatus = MoveRepository.STATUS_ACCOUNTED;
      if (ObjectUtils.notEmpty(element.getMoveLineMassEntryList())
          && ObjectUtils.notEmpty(element.getMoveLineList())) {
        element.setMassEntryErrors("");
        int temporaryMoveNumber =
            element.getMoveLineMassEntryList().get(0).getTemporaryMoveNumber();
        massEntryVerificationService.checkPreconditionsMassEntry(
            element, temporaryMoveNumber, moveList, move.getMassEntryManageCutOff());
        if (ObjectUtils.notEmpty(element.getMassEntryErrors())) {
          move.setMassEntryErrors(move.getMassEntryErrors() + element.getMassEntryErrors() + '\n');
          newMoveStatus = MoveRepository.STATUS_NEW;
        } else {
          if (allowAccountingDaybook) {
            newMoveStatus = MoveRepository.STATUS_DAYBOOK;
          }
          if (authorizeSimulatedMove) {
            newMoveStatus = MoveRepository.STATUS_SIMULATED;
          }
        }
        massEntryToolService.fillMassEntryLinesFields(move, element, newMoveStatus);
      }
    }
  }

  protected void generatedTaxAndCounterPart(
      Move parentMove, Move workingMove, LocalDate dueDate, int temporaryMoveNumber) {
    try {
      moveToolService.exceptionOnGenerateCounterpart(workingMove);
      moveLineMassEntryService.generateTaxLineAndCounterpart(
          parentMove, workingMove, dueDate, temporaryMoveNumber);
      moveLineMassEntryService.setPfpValidatorUserForInTaxAccount(
          parentMove.getMoveLineMassEntryList(), parentMove.getCompany(), temporaryMoveNumber);
      massEntryToolService.sortMoveLinesMassEntryByTemporaryNumber(parentMove);
    } catch (AxelorException e) {
      TraceBackService.trace(e);
      massEntryVerificationService.setErrorMassEntryMoveLines(
          parentMove, temporaryMoveNumber, "paymentMode", e.getMessage());
    }
  }
}
