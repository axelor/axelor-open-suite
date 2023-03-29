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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PeriodServiceAccount periodServiceAccount;
  protected MoveValidateService moveValidateService;
  protected int jpaLimit = 20;

  @Inject
  public MassEntryServiceImpl(
      MassEntryToolService massEntryToolService,
      MassEntryVerificationService massEntryVerificationService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      PeriodServiceAccount periodServiceAccount,
      MoveValidateService moveValidateService) {
    this.massEntryToolService = massEntryToolService;
    this.massEntryVerificationService = massEntryVerificationService;
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.periodServiceAccount = periodServiceAccount;
    this.moveValidateService = moveValidateService;
  }

  @Override
  public MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineList, MoveLineMassEntry inputLine, boolean manageCutOff) {
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
      resetMoveLineMassEntry(inputLine, manageCutOff);
    }
    return inputLine;
  }

  @Override
  public void resetMoveLineMassEntry(MoveLineMassEntry moveLine, boolean manageCutOff) {
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

    if (!manageCutOff) {
      moveLine.setCutOffStartDate(null);
      moveLine.setCutOffEndDate(null);
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
            massEntryVerificationService.checkAndReplaceFieldsInMoveLineMassEntry(
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
    int newMoveStatus = MoveRepository.STATUS_DAYBOOK;

    moveList = massEntryToolService.createMoveListFromMassEntryList(move);
    move.setMassEntryErrors("");

    for (Move element : moveList) {
      if (ObjectUtils.notEmpty(element.getMoveLineMassEntryList())
          && ObjectUtils.notEmpty(element.getMoveLineList())) {
        element.setMassEntryErrors("");
        int temporaryMoveNumber =
            element.getMoveLineMassEntryList().get(0).getTemporaryMoveNumber();

        massEntryVerificationService.checkDateInAllMoveLineMassEntry(element, temporaryMoveNumber);
        massEntryVerificationService.checkOriginDateInAllMoveLineMassEntry(
            element, temporaryMoveNumber);
        massEntryVerificationService.checkOriginInAllMoveLineMassEntry(
            element, temporaryMoveNumber);
        massEntryVerificationService.checkCurrencyRateInAllMoveLineMassEntry(
            element, temporaryMoveNumber);
        massEntryVerificationService.checkPartnerInAllMoveLineMassEntry(
            element, temporaryMoveNumber);
        massEntryVerificationService.checkWellBalancedMove(element, temporaryMoveNumber);
        massEntryVerificationService.checkAccountAnalytic(element, temporaryMoveNumber);

        if (ObjectUtils.notEmpty(element.getMassEntryErrors())) {
          move.setMassEntryErrors(move.getMassEntryErrors() + element.getMassEntryErrors() + '\n');
          newMoveStatus = MoveRepository.STATUS_NEW;
        }
        massEntryToolService.setNewStatusSelectOnMassEntryLines(element, newMoveStatus);
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
      moveList = massEntryToolService.createMoveListFromMassEntryList(move);

      for (Move element : moveList) {
        String moveTemporaryMoveNumber = element.getReference();
        try {
          element.setReference(null);

          Move generatedMove = this.generateMassEntryMove(element);
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
          if (++i % jpaLimit == 0) {
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
        move.setMassEntryStatusSelect(MoveRepository.MASS_ENTRY_STATUS_CLOSED);
      }
    }

    resultMap.put(moveIdList, errors);

    return resultMap;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move generateMassEntryMove(Move move) throws AxelorException {
    Move newMove = new Move();
    User user = AuthUtils.getUser();

    if (move.getJournal().getCompany() != null) {
      int[] functionalOriginTab = new int[0];
      if (!ObjectUtils.isEmpty(move.getJournal().getAuthorizedFunctionalOriginSelect())) {
        functionalOriginTab =
            Arrays.stream(
                    move.getJournal()
                        .getAuthorizedFunctionalOriginSelect()
                        .replace(" ", "")
                        .split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
      }

      newMove =
          moveCreateService.createMove(
              move.getJournal(),
              move.getCompany(),
              move.getCurrency(),
              move.getPartner(),
              move.getDate(),
              move.getOriginDate(),
              move.getPaymentMode(),
              move.getPartner().getFiscalPosition(),
              move.getPartnerBankDetails(),
              MoveRepository.TECHNICAL_ORIGIN_TEMPLATE,
              !ObjectUtils.isEmpty(functionalOriginTab) ? functionalOriginTab[0] : 0,
              false,
              false,
              false,
              move.getOrigin(),
              move.getDescription(),
              move.getCompanyBankDetails());
      newMove.setPaymentCondition(move.getPaymentCondition());

      int counter = 1;

      for (MoveLine moveLineElement : move.getMoveLineList()) {
        BigDecimal amount = moveLineElement.getDebit().add(moveLineElement.getCredit());

        MoveLine moveLine =
            moveLineCreateService.createMoveLine(
                newMove,
                moveLineElement.getPartner(),
                moveLineElement.getAccount(),
                amount,
                moveLineElement.getDebit().compareTo(BigDecimal.ZERO) > 0,
                moveLineElement.getDate(),
                moveLineElement.getOriginDate(),
                counter,
                moveLineElement.getOrigin(),
                moveLineElement.getName());
        moveLine.setVatSystemSelect(moveLineElement.getVatSystemSelect());
        moveLine.setCutOffStartDate(moveLineElement.getCutOffStartDate());
        moveLine.setCutOffEndDate(moveLineElement.getCutOffEndDate());
        newMove.getMoveLineList().add(moveLine);

        moveLine.setTaxLine(moveLineElement.getTaxLine());

        moveLine.setAnalyticDistributionTemplate(moveLineElement.getAnalyticDistributionTemplate());
        moveLine.setAxis1AnalyticAccount(moveLineElement.getAxis1AnalyticAccount());
        moveLine.setAxis2AnalyticAccount(moveLineElement.getAxis2AnalyticAccount());
        moveLine.setAxis3AnalyticAccount(moveLineElement.getAxis3AnalyticAccount());
        moveLine.setAxis4AnalyticAccount(moveLineElement.getAxis4AnalyticAccount());
        moveLine.setAxis5AnalyticAccount(moveLineElement.getAxis5AnalyticAccount());
        moveLine.setAnalyticMoveLineList(moveLineElement.getAnalyticMoveLineList());
        moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);

        counter++;
      }

      if (!periodServiceAccount.isAuthorizedToAccountOnPeriod(newMove, user)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(AccountExceptionMessage.ACCOUNT_PERIOD_TEMPORARILY_CLOSED),
                newMove.getReference()));
      }

      // Pass the move in STATUS_DAYBOOK
      if (move.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK)
          && newMove.getStatusSelect().equals(MoveRepository.STATUS_NEW)) {
        moveValidateService.accounting(newMove);
      }

      // Pass the move in STATUS_ACCOUNTED
      if (newMove.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK)) {
        moveValidateService.accounting(newMove);
      }
    }

    return newMove;
  }

  @Override
  public int generatedTaxeAndCounterPart(
      Move parentMove, Move workingMove, LocalDate dueDate, int temporaryMoveNumber) {
    try {
      Beans.get(MoveToolService.class).exceptionOnGenerateCounterpart(workingMove);
      Beans.get(MoveLineMassEntryService.class)
          .generateTaxLineAndCounterpart(parentMove, workingMove, dueDate, temporaryMoveNumber);
    } catch (AxelorException e) {
      TraceBackService.trace(e);
      return e.getCategory();
    }
    return 0;
  }
}
