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
package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryToolService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class MassEntryServiceImpl implements MassEntryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MassEntryToolService massEntryToolService;
  protected MassEntryVerificationService massEntryVerificationService;
  protected MoveToolService moveToolService;
  protected MoveLineMassEntryService moveLineMassEntryService;
  protected MassEntryMoveCreateService massEntryMoveCreateService;
  protected AppAccountService appAccountService;
  protected MoveRepository moveRepository;
  protected MoveLineMassEntryToolService moveLineMassEntryToolService;

  @Inject
  public MassEntryServiceImpl(
      MassEntryToolService massEntryToolService,
      MassEntryVerificationService massEntryVerificationService,
      MoveToolService moveToolService,
      MoveLineMassEntryService moveLineMassEntryService,
      MassEntryMoveCreateService massEntryMoveCreateService,
      AppAccountService appAccountService,
      MoveRepository moveRepository,
      MoveLineMassEntryToolService moveLineMassEntryToolService) {
    this.massEntryToolService = massEntryToolService;
    this.massEntryVerificationService = massEntryVerificationService;
    this.moveToolService = moveToolService;
    this.moveLineMassEntryService = moveLineMassEntryService;
    this.massEntryMoveCreateService = massEntryMoveCreateService;
    this.appAccountService = appAccountService;
    this.moveRepository = moveRepository;
    this.moveLineMassEntryToolService = moveLineMassEntryToolService;
  }

  @Override
  public MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineList, MoveLineMassEntry inputLine) {
    if (ObjectUtils.notEmpty(moveLineList)) {
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
          break;
        }
      }
    } else {
      resetMoveLineMassEntry(inputLine);
    }
    return inputLine;
  }

  @Override
  public void resetMoveLineMassEntry(MoveLineMassEntry moveLine) {
    moveLine.setDate(LocalDate.now());
    moveLine.setOrigin(null);
    moveLine.setOriginDate(LocalDate.now());
    moveLine.setPartner(null);
    moveLine.setPartnerId(null);
    moveLine.setPartnerSeq(null);
    moveLine.setPartnerFullName(null);
    moveLine.setMoveDescription(null);
    moveLine.setMovePaymentCondition(null);
    moveLine.setMovePaymentMode(null);
    moveLine.setMovePartnerBankDetails(null);
    moveLine.setAccount(null);
    moveLine.setTaxLine(null);
    moveLine.setDescription(null);
    moveLine.setDebit(BigDecimal.ZERO);
    moveLine.setCredit(BigDecimal.ZERO);
    moveLine.setCurrencyRate(BigDecimal.ONE);
    moveLine.setCurrencyAmount(BigDecimal.ZERO);
    moveLine.setMoveStatusSelect(null);
    moveLine.setVatSystemSelect(0);
    moveLine.setPfpValidatorUser(null);
    moveLine.setDeliveryDate(LocalDate.now());
    moveLine.setCutOffStartDate(LocalDate.now());
    moveLine.setCutOffEndDate(LocalDate.now());
    moveLine.setIsEdited(MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_NULL);
    moveLine.setFieldsErrorList(null);
    moveLine.setAnalyticDistributionTemplate(null);
    moveLine.setAxis1AnalyticAccount(null);
    moveLine.setAxis2AnalyticAccount(null);
    moveLine.setAxis3AnalyticAccount(null);
    moveLine.setAxis4AnalyticAccount(null);
    moveLine.setAxis5AnalyticAccount(null);
    moveLine.setAnalyticMoveLineList(null);

    if (!appAccountService.getAppAccount().getManageCutOffPeriod()) {
      moveLine.setCutOffStartDate(null);
      moveLine.setCutOffEndDate(null);
      moveLine.setDeliveryDate(null);
    }
  }

  @Override
  public void verifyFieldsAndGenerateTaxLineAndCounterpart(Move parentMove, LocalDate dueDate)
      throws AxelorException {
    this.verifyFieldsChangeOnMoveLineMassEntry(parentMove, parentMove.getMassEntryManageCutOff());

    MoveLineMassEntry lastLine =
        parentMove.getMoveLineMassEntryList().get(parentMove.getMoveLineMassEntryList().size() - 1);
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

      int categoryError =
          this.generatedTaxeAndCounterPart(parentMove, workingMove, dueDate, temporaryMoveNumber);
      if (categoryError == 0) {
        massEntryToolService.sortMoveLinesMassEntryByTemporaryNumber(parentMove);
      } else {
        massEntryVerificationService.setErrorMassEntryMoveLines(
            parentMove,
            temporaryMoveNumber,
            "paymentMode",
            I18n.get(AccountExceptionMessage.EXCEPTION_GENERATE_COUNTERPART));
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
  public void checkMassEntryMoveGeneration(Move move) {
    List<Move> moveList;

    moveList = massEntryMoveCreateService.createMoveListFromMassEntryList(move);
    move.setMassEntryErrors("");

    for (Move element : moveList) {
      int newMoveStatus = MoveRepository.STATUS_DAYBOOK;
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
        }
        massEntryToolService.fillMassEntryLinesFields(move, element, newMoveStatus);
      }
    }
  }

  @Override
  public Map<List<Long>, String> validateMassEntryMove(Move move) {
    Map<List<Long>, String> resultMap = new HashMap<>();
    String errors = "";
    List<Move> moveList;
    List<Long> moveIdList = new ArrayList<>();
    List<Integer> temporaryErrorIdList = new ArrayList<>();
    int i = 0;

    if (massEntryToolService.verifyJournalAuthorizeNewMove(
            move.getMoveLineMassEntryList(), move.getJournal())
        && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
      moveList = massEntryMoveCreateService.createMoveListFromMassEntryList(move);

      for (Move element : moveList) {
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
          if (++i % AbstractBatch.FETCH_LIMIT == 0) {
            JPA.clear();
          }
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

    resultMap.put(moveIdList, errors);

    return resultMap;
  }

  @Override
  public int generatedTaxeAndCounterPart(
      Move parentMove, Move workingMove, LocalDate dueDate, int temporaryMoveNumber) {
    try {
      moveToolService.exceptionOnGenerateCounterpart(workingMove);
      moveLineMassEntryService.generateTaxLineAndCounterpart(
          parentMove, workingMove, dueDate, temporaryMoveNumber);
    } catch (AxelorException e) {
      TraceBackService.trace(e);
      return e.getCategory();
    }
    return 0;
  }
}
