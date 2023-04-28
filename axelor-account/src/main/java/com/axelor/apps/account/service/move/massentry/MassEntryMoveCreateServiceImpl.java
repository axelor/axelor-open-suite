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
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryToolService;
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
import java.util.Comparator;
import java.util.List;

public class MassEntryMoveCreateServiceImpl implements MassEntryMoveCreateService {

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PeriodServiceAccount periodServiceAccount;
  protected MoveValidateService moveValidateService;
  protected PeriodService periodService;
  protected MassEntryToolService massEntryToolService;
  protected MoveLineMassEntryToolService moveLineMassEntryToolService;
  protected MoveLineMassEntryRepository moveLineMassEntryRepository;

  @Inject
  public MassEntryMoveCreateServiceImpl(
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      PeriodServiceAccount periodServiceAccount,
      MoveValidateService moveValidateService,
      PeriodService periodService,
      MassEntryToolService massEntryToolService,
      MoveLineMassEntryToolService moveLineMassEntryToolService,
      MoveLineMassEntryRepository moveLineMassEntryRepository) {
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.periodServiceAccount = periodServiceAccount;
    this.moveValidateService = moveValidateService;
    this.periodService = periodService;
    this.massEntryToolService = massEntryToolService;
    this.moveLineMassEntryToolService = moveLineMassEntryToolService;
    this.moveLineMassEntryRepository = moveLineMassEntryRepository;
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

        moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);
        moveLineMassEntryToolService.setAnalyticsFields(moveLine, moveLineElement);

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
    List<Move> moveList = new ArrayList();
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

    moveResult.setJournal(parentMove.getJournal());
    moveResult.setFunctionalOriginSelect(parentMove.getFunctionalOriginSelect());
    moveResult.setCompany(parentMove.getCompany());
    moveResult.setCurrency(parentMove.getCurrency());
    moveResult.setCompanyBankDetails(parentMove.getCompanyBankDetails());

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
        massEntryLine.setFieldsErrorList(null);
        MoveLineMassEntry copy = moveLineMassEntryRepository.copy(massEntryLine, false);
        move.addMoveLineListItem(copy);
        move.addMoveLineMassEntryListItem(copy);
      }
    }
  }

  @Override
  public Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineList) {
    return moveLineList.stream()
        .map(MoveLineMassEntry::getTemporaryMoveNumber)
        .max(Comparator.naturalOrder())
        .get();
  }
}
