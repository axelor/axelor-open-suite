package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MassEntryMoveCreateServiceImpl implements MassEntryMoveCreateService {

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PeriodServiceAccount periodServiceAccount;
  protected MoveValidateService moveValidateService;
  protected PeriodService periodService;

  @Inject
  public MassEntryMoveCreateServiceImpl(
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      PeriodServiceAccount periodServiceAccount,
      MoveValidateService moveValidateService,
      PeriodService periodService) {
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.periodServiceAccount = periodServiceAccount;
    this.moveValidateService = moveValidateService;
    this.periodService = periodService;
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
  public List<Move> createMoveListFromMassEntryList(Move parentMove) {
    List<Move> moveList = new ArrayList<>();
    Move moveToAdd;

    for (int i = 1;
        i <= this.getMaxTemporaryMoveNumber(parentMove.getMoveLineMassEntryList());
        i++) {
      moveToAdd = this.createMoveFromMassEntryList(parentMove, i);
      moveList.add(moveToAdd);
    }

    return moveList;
  }

  @Override
  public Move createMoveFromMassEntryList(Move parentMove, int temporaryMoveNumber) {
    Move moveResult = new Move();
    boolean firstMoveLine = true;

    moveResult.setJournal(parentMove.getJournal());
    moveResult.setFunctionalOriginSelect(parentMove.getFunctionalOriginSelect());
    moveResult.setCompany(parentMove.getCompany());
    moveResult.setCurrency(parentMove.getCurrency());
    moveResult.setCompanyBankDetails(parentMove.getCompanyBankDetails());

    for (MoveLineMassEntry massEntryLine : parentMove.getMoveLineMassEntryList()) {
      if (massEntryLine.getTemporaryMoveNumber() == temporaryMoveNumber
          && massEntryLine.getInputAction()
              == MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE) {
        if (firstMoveLine) {
          if (massEntryLine.getDate() != null && moveResult.getCompany() != null) {
            moveResult.setPeriod(
                periodService.getPeriod(
                    massEntryLine.getDate(), moveResult.getCompany(), YearRepository.TYPE_FISCAL));
          }
          moveResult.setReference(massEntryLine.getTemporaryMoveNumber().toString());
          moveResult.setDate(massEntryLine.getDate());
          moveResult.setPartner(massEntryLine.getPartner());
          moveResult.setOrigin(massEntryLine.getOrigin());
          moveResult.setStatusSelect(massEntryLine.getMoveStatusSelect());
          moveResult.setOriginDate(massEntryLine.getOriginDate());
          moveResult.setDescription(massEntryLine.getMoveDescription());
          moveResult.setPaymentMode(massEntryLine.getMovePaymentMode());
          moveResult.setPaymentCondition(massEntryLine.getMovePaymentCondition());
          moveResult.setPartnerBankDetails(massEntryLine.getMovePartnerBankDetails());
          firstMoveLine = false;
        }
        massEntryLine.setMove(moveResult);
        massEntryLine.setFieldsErrorList(null);
        moveResult.addMoveLineListItem(massEntryLine);
        moveResult.addMoveLineMassEntryListItem(massEntryLine);
      }
    }

    return moveResult;
  }

  @Override
  public Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineList) {
    int max = 0;

    for (MoveLineMassEntry moveLine : moveLineList) {
      if (moveLine.getTemporaryMoveNumber() > max) {
        max = moveLine.getTemporaryMoveNumber();
      }
    }

    return max;
  }
}
