package com.axelor.apps.account.service.move.control.accounting.balance;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class MoveAccountingBalanceControlServiceImpl implements MoveAccountingBalanceControlService{
	
	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void checkWellBalanced(Move move) throws AxelorException {
	    log.debug("Well-balanced validation on account move {}", move.getReference());

	    if (move.getMoveLineList() != null) {

	      BigDecimal totalDebit = BigDecimal.ZERO;
	      BigDecimal totalCredit = BigDecimal.ZERO;

	      for (MoveLine moveLine : move.getMoveLineList()) {

	        if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
	            && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
	          throw new AxelorException(
	              move,
	              TraceBackRepository.CATEGORY_INCONSISTENCY,
	              I18n.get(IExceptionMessage.MOVE_6),
	              moveLine.getName());
	        }

	        totalDebit = totalDebit.add(moveLine.getDebit());
	        totalCredit = totalCredit.add(moveLine.getCredit());
	      }

	      if (totalDebit.compareTo(totalCredit) != 0) {
	        throw new AxelorException(
	            move,
	            TraceBackRepository.CATEGORY_INCONSISTENCY,
	            I18n.get(IExceptionMessage.MOVE_7),
	            move.getReference(),
	            totalDebit,
	            totalCredit);
	      }
	    }
		
	}

}
