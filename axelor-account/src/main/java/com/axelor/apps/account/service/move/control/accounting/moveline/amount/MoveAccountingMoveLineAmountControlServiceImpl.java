package com.axelor.apps.account.service.move.control.accounting.moveline.amount;

import java.math.BigDecimal;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class MoveAccountingMoveLineAmountControlServiceImpl implements MoveAccountingMoveLineAmountControlService{

	@Override
	public void checkNotEmpty(MoveLine moveLine) throws AxelorException {
	    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0
	            && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0
	            && moveLine.getCurrencyAmount().compareTo(BigDecimal.ZERO) == 0) {
	          throw new AxelorException(
	              moveLine,
	              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
	              I18n.get(IExceptionMessage.MOVE_LINE_7),
	              moveLine.getAccount().getCode());
	        }
	}

}
