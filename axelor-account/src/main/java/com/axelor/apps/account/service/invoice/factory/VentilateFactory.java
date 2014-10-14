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
package com.axelor.apps.account.service.invoice.factory;

import javax.inject.Inject;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.invoice.workflow.ventilate.MajorEndCycleVentilateState;
import com.axelor.apps.account.service.invoice.workflow.ventilate.VentilateState;
import com.axelor.apps.base.service.administration.SequenceService;

public class VentilateFactory {
	
	@Inject
	private SequenceService SequenceService;
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private PaymentScheduleService paymentScheduleService;
	
	@Inject
	private ReimbursementExportService reimbursementExportService;

	public VentilateState getVentilator(Invoice invoice){
		
		return ventilatorByType(invoice);
		
	}
	
	protected VentilateState ventilatorByType(Invoice invoice){
		
		if(invoice.getEndOfCycleOk())  {
			return new MajorEndCycleVentilateState(SequenceService, moveService, paymentScheduleService, reimbursementExportService, invoice);
		}
		else  {
			return new VentilateState(SequenceService, moveService, invoice);
		}
		
	}
	
}
