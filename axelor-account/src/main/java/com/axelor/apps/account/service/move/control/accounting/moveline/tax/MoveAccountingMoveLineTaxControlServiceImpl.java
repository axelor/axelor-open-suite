package com.axelor.apps.account.service.move.control.accounting.moveline.tax;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class MoveAccountingMoveLineTaxControlServiceImpl
    implements MoveAccountingMoveLineTaxControlService {

  @Override
  public void checkMandatoryTax(MoveLine moveLine) throws AxelorException {

    Account account = moveLine.getAccount();
    if (account.getIsTaxAuthorizedOnMoveLine()
        && account.getIsTaxRequiredOnMoveLine()
        && moveLine.getTaxLine() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(I18n.get(IExceptionMessage.MOVE_9), account.getName(), moveLine.getName()));
    }
  }
}
