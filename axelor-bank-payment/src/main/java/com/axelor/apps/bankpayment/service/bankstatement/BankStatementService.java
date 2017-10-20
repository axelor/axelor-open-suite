/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatement;

import java.io.IOException;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.file.afb120.BankStatementFileAFB120Service;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BankStatementService {

	protected BankStatementRepository bankStatementRepository;
	
	@Inject
	public BankStatementService(BankStatementRepository bankStatementRepository)  {
		this.bankStatementRepository = bankStatementRepository;
	}

	public void runImport(BankStatement bankStatement) throws IOException, AxelorException  {

		if (bankStatement.getBankStatementFile() == null || bankStatement.getBankStatementFileFormat() == null) {
			return;
		}

		BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

		switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
	    	case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
		    case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
		    case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM_0BY:
		    case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM_EUR:
    			new BankStatementFileAFB120Service(bankStatement, bankStatementRepository).process();
	    		updateStatus(bankStatement);
    			break;

		    default:
    			throw new AxelorException(I18n.get(IExceptionMessage.BANK_STATEMENT_FILE_UNKNOWN_FORMAT), IException.INCONSISTENCY);
		}

	}
	
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void updateStatus(BankStatement bankStatement)  {
		
		bankStatement.setStatusSelect(BankStatementRepository.STATUS_IMPORTED);
		bankStatementRepository.save(bankStatement);
	}
	

}
