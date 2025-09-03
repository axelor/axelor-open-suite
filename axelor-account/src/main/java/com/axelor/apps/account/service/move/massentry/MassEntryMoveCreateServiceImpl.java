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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.account.service.period.PeriodCheckService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MassEntryMoveCreateServiceImpl implements MassEntryMoveCreateService {

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PeriodCheckService periodCheckService;
  protected MoveValidateService moveValidateService;
  protected PeriodService periodService;
  protected MoveLineMassEntryRepository moveLineMassEntryRepository;
  protected MoveSimulateService moveSimulateService;
  protected MoveRepository moveRepository;
  protected MoveLineMassEntryRecordService moveLineMassEntryRecordService;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected MoveRecordSetService moveRecordSetService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MassEntryMoveCreateServiceImpl(
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      PeriodCheckService periodCheckService,
      MoveValidateService moveValidateService,
      PeriodService periodService,
      MoveLineMassEntryRepository moveLineMassEntryRepository,
      MoveSimulateService moveSimulateService,
      MoveRepository moveRepository,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      MoveRecordSetService moveRecordSetService,
      MoveLineToolService moveLineToolService) {
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.periodCheckService = periodCheckService;
    this.moveValidateService = moveValidateService;
    this.periodService = periodService;
    this.moveLineMassEntryRepository = moveLineMassEntryRepository;
    this.moveSimulateService = moveSimulateService;
    this.moveRepository = moveRepository;
    this.moveLineMassEntryRecordService = moveLineMassEntryRecordService;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.moveRecordSetService = moveRecordSetService;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move generateMassEntryMove(Move move) throws AxelorException {
    Move newMove = new Move();
    User user = AuthUtils.getUser();
    boolean authorizeSimulatedMove = move.getJournal().getAuthorizeSimulatedMove();

    if (move.getJournal().getCompany() != null) {
      newMove =
          moveCreateService.createMove(
              move.getJournal(),
              move.getCompany(),
              move.getCurrency(),
              move.getPartner(),
              move.getDate(),
              move.getOriginDate(),
              move.getPaymentMode(),
              move.getPartner() != null ? move.getPartner().getFiscalPosition() : null,
              move.getPartnerBankDetails(),
              MoveRepository.TECHNICAL_ORIGIN_MASS_ENTRY,
              move.getFunctionalOriginSelect(),
              false,
              false,
              false,
              move.getOrigin(),
              move.getDescription(),
              move.getCompanyBankDetails());
      newMove.setPaymentCondition(move.getPaymentCondition());
      newMove.setPfpValidatorUser(move.getPfpValidatorUser());
      newMove.setPfpValidateStatusSelect(move.getPfpValidateStatusSelect());

      moveRecordSetService.setThirdPartyPayerPartner(newMove);

      int counter = 1;

      for (MoveLine moveLine : move.getMoveLineList()) {
        BigDecimal amount = moveLine.getDebit().add(moveLine.getCredit());

        MoveLine newMoveLine =
            moveLineCreateService.createMoveLine(
                newMove,
                moveLine.getPartner(),
                moveLine.getAccount(),
                amount,
                moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0,
                moveLine.getDate(),
                moveLine.getOriginDate(),
                counter,
                moveLine.getOrigin(),
                moveLine.getName());
        if (StringUtils.notEmpty(moveLine.getDescription())) {
          newMoveLine.setDescription(moveLine.getDescription());
        }
        newMoveLine.setVatSystemSelect(moveLine.getVatSystemSelect());
        newMoveLine.setCutOffStartDate(moveLine.getCutOffStartDate());
        newMoveLine.setCutOffEndDate(moveLine.getCutOffEndDate());
        newMove.getMoveLineList().add(newMoveLine);

        newMoveLine.setTaxLineSet(moveLine.getTaxLineSet());

        moveLineComputeAnalyticService.generateAnalyticMoveLines(newMoveLine);
        moveLineMassEntryRecordService.setAnalytics(newMoveLine, moveLine);
        if (ObjectUtils.notEmpty(moveLine.getAnalyticMoveLineList())) {
          newMoveLine.clearAnalyticMoveLineList();
          for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
            newMoveLine.addAnalyticMoveLineListItem(
                analyticMoveLineRepository.copy(analyticMoveLine, false));
          }
        }

        counter++;
      }

      if (!periodCheckService.isAuthorizedToAccountOnPeriod(newMove, user)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(AccountExceptionMessage.ACCOUNT_PERIOD_TEMPORARILY_CLOSED),
                newMove.getReference()));
      }

      if (move.getStatusSelect() != MoveRepository.STATUS_NEW) {
        if (authorizeSimulatedMove) {
          moveSimulateService.simulate(newMove);
        } else {
          moveValidateService.accounting(newMove);
        }
      } else {
        moveRepository.save(newMove);
      }
    }

    return newMove;
  }

  @Override
  public List<Move> createMoveListFromMassEntryList(Move parentMove) {
    List<Move> moveList = new ArrayList<>();
    Move moveToAdd;

    List<Integer> uniqueIdList =
        parentMove.getMoveLineMassEntryList().stream()
            .map(MoveLineMassEntry::getTemporaryMoveNumber)
            .distinct()
            .collect(Collectors.toList());
    for (Integer i : uniqueIdList) {
      moveToAdd = this.createMoveFromMassEntryList(parentMove, i);
      moveList.add(moveToAdd);
    }

    return moveList;
  }

  @Override
  public Move createMoveFromMassEntryList(Move parentMove, int temporaryMoveNumber) {
    if (parentMove == null) {
      return null;
    }

    Move moveResult = new Move();

    if (parentMove.getId() != null) {
      parentMove = moveRepository.find(parentMove.getId());
    }

    moveResult.setJournal(parentMove.getJournal());
    moveResult.setFunctionalOriginSelect(parentMove.getFunctionalOriginSelect());
    moveResult.setCompany(parentMove.getCompany());
    moveResult.setCurrency(parentMove.getCurrency());
    moveResult.setCompanyBankDetails(parentMove.getCompanyBankDetails());
    moveResult.setPfpValidateStatusSelect(MoveRepository.PFP_STATUS_AWAITING);

    if (parentMove.getCompany() != null) {
      moveResult.setCompanyCurrency(parentMove.getCompany().getCurrency());
    }

    fillMoveWithMoveLineMassEntry(
        moveResult, parentMove.getMoveLineMassEntryList(), temporaryMoveNumber);

    return moveResult;
  }

  private void fillMoveWithMoveLineMassEntry(
      Move move, List<MoveLineMassEntry> moveLineMassEntryList, int temporaryMoveNumber) {
    boolean firstMoveLine = true;

    for (MoveLineMassEntry massEntryLine : moveLineMassEntryList) {
      if (massEntryLine.getTemporaryMoveNumber() == temporaryMoveNumber
          && massEntryLine.getInputAction()
              == MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE) {
        if (firstMoveLine) {
          if (massEntryLine.getDate() != null && move.getCompany() != null) {

            move.setPeriod(
                periodService.getPeriod(
                    massEntryLine.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL));
          }
          move.setReference(massEntryLine.getTemporaryMoveNumber().toString());
          move.setDate(massEntryLine.getDate());
          move.setPartner(massEntryLine.getPartner());
          move.setOrigin(massEntryLine.getOrigin());
          move.setStatusSelect(massEntryLine.getMoveStatusSelect());
          move.setOriginDate(massEntryLine.getOriginDate());
          move.setDescription(massEntryLine.getMoveDescription());
          move.setPaymentMode(massEntryLine.getMovePaymentMode());
          move.setPaymentCondition(massEntryLine.getMovePaymentCondition());
          move.setPartnerBankDetails(massEntryLine.getMovePartnerBankDetails());
          firstMoveLine = false;
        }

        if (ObjectUtils.notEmpty(massEntryLine.getMovePfpValidatorUser())) {
          move.setPfpValidatorUser(massEntryLine.getMovePfpValidatorUser());
        }
        massEntryLine.setFieldsErrorList(null);
        MoveLineMassEntry copy = moveLineMassEntryRepository.copy(massEntryLine, false);
        copy.setSourceTaxLineSet(massEntryLine.getSourceTaxLineSet());
        moveLineToolService.setDecimals(copy, move);
        moveLineMassEntryRecordService.fillAnalyticMoveLineList(massEntryLine, copy);

        move.addMoveLineListItem(copy);
        move.addMoveLineMassEntryListItem(copy);
      }
    }
  }
}
