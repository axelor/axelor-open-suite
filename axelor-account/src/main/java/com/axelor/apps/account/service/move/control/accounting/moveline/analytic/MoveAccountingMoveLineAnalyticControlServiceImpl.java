package com.axelor.apps.account.service.move.control.accounting.moveline.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class MoveAccountingMoveLineAnalyticControlServiceImpl
    implements MoveAccountingMoveLineAnalyticControlService {

  @Override
  public void checkMandatoryAnalyticDistributionTemplate(MoveLine moveLine) throws AxelorException {
    Account account = moveLine.getAccount();
    if (moveLine.getAnalyticDistributionTemplate() == null
        && ObjectUtils.isEmpty(moveLine.getAnalyticMoveLineList())
        && account.getAnalyticDistributionAuthorized()
        && account.getAnalyticDistributionRequiredOnMoveLines()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.MOVE_10),
          account.getName(),
          moveLine.getName());
    }
  }

  @Override
  public void checkAuthorizedAnalyticDistributionTemplate(MoveLine moveLine)
      throws AxelorException {
    Account account = moveLine.getAccount();
    if (account != null
        && !account.getAnalyticDistributionAuthorized()
        && (moveLine.getAnalyticDistributionTemplate() != null
            || (moveLine.getAnalyticMoveLineList() != null
                && !moveLine.getAnalyticMoveLineList().isEmpty()))) {
      throw new AxelorException(
          moveLine.getMove(),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_11),
          moveLine.getName());
    }
  }
}
