package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class MoveLineMassEntryRecordServiceImpl implements MoveLineMassEntryRecordService {

  protected MoveLineMassEntryService moveLineMassEntryService;

  @Inject
  public MoveLineMassEntryRecordServiceImpl(MoveLineMassEntryService moveLineMassEntryService) {
    this.moveLineMassEntryService = moveLineMassEntryService;
  }

  @Override
  public void setCurrencyRate(Move move, MoveLineMassEntry moveLine) throws AxelorException {
    BigDecimal currencyRate = BigDecimal.ONE;

    currencyRate =
        moveLineMassEntryService.computeCurrentRate(
            currencyRate,
            move.getMoveLineMassEntryList(),
            move.getCurrency(),
            move.getCompanyCurrency(),
            moveLine.getTemporaryMoveNumber(),
            moveLine.getOriginDate());

    moveLine.setCurrencyRate(currencyRate);
  }

  @Override
  public void resetDebit(MoveLineMassEntry moveLine) {
    if (moveLine.getCredit().signum() != 0 && moveLine.getDebit().signum() != 0) {
      moveLine.setDebit(BigDecimal.ZERO);
    }
  }

  @Override
  public void setMovePfpValidatorUser(MoveLineMassEntry moveLine, Company company) {
    moveLine.setMovePfpValidatorUser(
        Beans.get(MoveLineMassEntryService.class)
            .getPfpValidatorUserForInTaxAccount(
                moveLine.getAccount(), company, moveLine.getPartner()));
  }
}
