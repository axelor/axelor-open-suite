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
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequestScoped
public class MassEntryServiceImpl implements MassEntryService {

  protected MassEntryToolService massEntryToolService;
  protected MassEntryVerificationService massEntryVerificationService;
  protected MoveToolService moveToolService;
  protected MoveLineMassEntryService moveLineMassEntryService;
  protected MassEntryMoveCreateService massEntryMoveCreateService;
  protected MoveRepository moveRepository;
  protected MoveLineMassEntryRecordService moveLineMassEntryRecordService;

  @Inject
  public MassEntryServiceImpl(
      MassEntryToolService massEntryToolService,
      MassEntryVerificationService massEntryVerificationService,
      MoveToolService moveToolService,
      MoveLineMassEntryService moveLineMassEntryService,
      MassEntryMoveCreateService massEntryMoveCreateService,
      MoveRepository moveRepository,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService) {
    this.massEntryToolService = massEntryToolService;
    this.massEntryVerificationService = massEntryVerificationService;
    this.moveToolService = moveToolService;
    this.moveLineMassEntryService = moveLineMassEntryService;
    this.massEntryMoveCreateService = massEntryMoveCreateService;
    this.moveRepository = moveRepository;
    this.moveLineMassEntryRecordService = moveLineMassEntryRecordService;
  }

  @Override
  public MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineList, MoveLineMassEntry inputLine, Company company) {
    if (ObjectUtils.notEmpty(moveLineList)) {
      inputLine.setInputAction(MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE);
      if (inputLine.getTemporaryMoveNumber() <= 0) {
        inputLine.setTemporaryMoveNumber(
            massEntryMoveCreateService.getMaxTemporaryMoveNumber(moveLineList));
        inputLine.setCounter(moveLineList.size() + 1);
      }

      for (MoveLineMassEntry moveLine : moveLineList) {
        if (moveLine.getTemporaryMoveNumber().equals(inputLine.getTemporaryMoveNumber())) {
          inputLine.setPartner(moveLine.getPartner());
          inputLine.setPartnerId(moveLine.getPartnerId());
          inputLine.setPartnerSeq(moveLine.getPartnerSeq());
          inputLine.setPartnerFullName(moveLine.getPartnerFullName());
          inputLine.setDate(moveLine.getDate());
          inputLine.setDueDate(moveLine.getDueDate());
          inputLine.setOriginDate(moveLine.getOriginDate());
          inputLine.setOrigin(moveLine.getOrigin());
          inputLine.setMoveStatusSelect(moveLine.getMoveStatusSelect());
          inputLine.setInterbankCodeLine(moveLine.getInterbankCodeLine());
          inputLine.setMoveDescription(moveLine.getMoveDescription());
          inputLine.setDescription(moveLine.getMoveDescription());
          inputLine.setExportedDirectDebitOk(moveLine.getExportedDirectDebitOk());
          inputLine.setMovePaymentCondition(moveLine.getMovePaymentCondition());
          inputLine.setMovePaymentMode(moveLine.getMovePaymentMode());
          inputLine.setMovePartnerBankDetails(moveLine.getMovePartnerBankDetails());
          inputLine.setCutOffStartDate(moveLine.getCutOffStartDate());
          inputLine.setCutOffEndDate(moveLine.getCutOffEndDate());
          inputLine.setDeliveryDate(moveLine.getDeliveryDate());
          inputLine.setVatSystemSelect(moveLine.getVatSystemSelect());
          inputLine.setIsEdited(MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_NULL);
          moveLineMassEntryRecordService.setAnalytics(inputLine, moveLine);
          moveLineMassEntryRecordService.fillAnalyticMoveLineList(inputLine, moveLine);
          break;
        }
      }
    } else {
      inputLine = moveLineMassEntryService.createMoveLineMassEntry(company);
    }
    return inputLine;
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
      String technicalTypeSelect =
          lastLine.getAccount() != null
              ? lastLine.getAccount().getAccountType().getTechnicalTypeSelect()
              : null;
      int journalTechnicalTypeSelect =
          parentMove.getJournal() != null
              ? parentMove.getJournal().getJournalType().getTechnicalTypeSelect()
              : 0;
      int temporaryMoveNumber = lastLine.getTemporaryMoveNumber();
      int[] technicalTypeSelectArray = {
        JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
        JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE,
        JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE
      };

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

  @Override
  @Transactional
  public Map<List<Long>, String> validateMassEntryMove(Move move) {
    Map<List<Long>, String> resultMap = new HashMap<>();
    String errors = "";
    List<Move> moveList;
    List<Long> moveIdList = new ArrayList<>();
    List<Integer> temporaryErrorIdList = new ArrayList<>();
    int i = 0;
    move = moveRepository.find(move.getId());

    if (massEntryToolService.verifyJournalAuthorizeNewMove(
            move.getMoveLineMassEntryList(), move.getJournal())
        && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
      List<Integer> uniqueIdList =
          move.getMoveLineMassEntryList().stream()
              .map(MoveLineMassEntry::getTemporaryMoveNumber)
              .distinct()
              .collect(Collectors.toList());
      for (Integer x : uniqueIdList) {
        Move element = massEntryMoveCreateService.createMoveFromMassEntryList(move, x);
        String moveTemporaryMoveNumber = element.getReference();
        try {
          element.setReference(null);

          Move generatedMove = massEntryMoveCreateService.generateMassEntryMove(element);
          moveIdList.add(generatedMove.getId());

          for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
            if (Objects.equals(
                Integer.parseInt(moveTemporaryMoveNumber), moveLine.getTemporaryMoveNumber())) {
              moveLine.setMoveStatusSelect(generatedMove.getStatusSelect());
              moveLine.setTemporaryMoveNumber(Math.toIntExact(generatedMove.getId()));
            }
          }

        } catch (Exception e) {
          TraceBackService.trace(e);
          if (errors.length() > 0) {
            errors = errors.concat(", ");
          }
          errors = errors.concat(moveTemporaryMoveNumber);
          temporaryErrorIdList.add(Integer.parseInt(moveTemporaryMoveNumber));
        } finally {
          JPA.clear();
          move = moveRepository.find(move.getId());
        }
      }

      for (int tempMoveNumber : temporaryErrorIdList) {
        for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
          if (Objects.equals(tempMoveNumber, moveLine.getTemporaryMoveNumber())) {
            moveLine.setMoveStatusSelect(MoveRepository.STATUS_NEW);
          }
        }
      }
      if (errors.length() == 0) {
        move.setMassEntryStatusSelect(MoveRepository.MASS_ENTRY_STATUS_VALIDATED);
      }
    }

    moveRepository.save(move);
    resultMap.put(moveIdList, errors);

    return resultMap;
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
