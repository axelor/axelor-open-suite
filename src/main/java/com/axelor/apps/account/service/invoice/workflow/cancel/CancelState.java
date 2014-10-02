/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.workflow.cancel;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.inject.Beans;

public class CancelState extends WorkflowInvoice {
	
	public CancelState(Invoice invoice){ super(invoice); }
	
	@Override
	public void process() throws AxelorException {
		
		if(invoice.getStatusSelect() == STATUS_VENTILATED && invoice.getCompany().getAccountConfig().getAllowCancelVentilatedInvoice())  {
			cancelMove();
		}
		
		setStatus();
		
	}
	
	protected void setStatus(){
		
		invoice.setStatusSelect(STATUS_CANCELED);
		
	}
	
	protected void cancelMove() throws AxelorException{
		
		if(invoice.getMove() == null)   {  return;  }
			
		if(invoice.getInTaxTotalRemaining().compareTo(invoice.getInTaxTotal()) != 0)  {
			
			throw new AxelorException(String.format("%s :\n Move should be unReconcile before to cancel the invoice",
					GeneralServiceAccount.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		
		if(invoice.getOldMove() != null)  {
			
			throw new AxelorException(String.format("%s :\n Invoice is passed in doubfult debit, and can't be canceled",
					GeneralServiceAccount.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		
		try{
			
			Move move = invoice.getMove();
			
			invoice.setMove(null);

			if(invoice.getCompany().getAccountConfig().getAllowRemovalValidatedMove())  {
				Beans.get(MoveRepository.class).remove(move);
			}
			else  {
				move.setStatusSelect(MoveRepository.STATUS_CANCELED);
			}
			
		}
		catch(Exception e)  {
			
			throw new AxelorException(String.format("%s :\n Too many accounting operations are used on this invoice, so invoice can't be canceled",
					GeneralServiceAccount.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
			
		}
		
		
	}
	
}