/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;

public class MajorEndCycleVentilateState extends VentilateState {
	
	private PaymentScheduleService paymentScheduleService;
	private ReimbursementExportService reimbursementExportService;
	
	public MajorEndCycleVentilateState(SequenceService SequenceService, MoveService moveService,
			PaymentScheduleService paymentScheduleService, 
			ReimbursementExportService reimbursementExportService, Invoice invoice) {
		
		super(SequenceService, moveService, invoice);
		this.reimbursementExportService = reimbursementExportService;
		this.paymentScheduleService = paymentScheduleService;
		
	}
	
	@Override
	protected void setMove( ) throws AxelorException {
		
		if( invoice.getPaymentSchedule() != null && invoice.getEndOfCycleOk())  {
			
			paymentScheduleService.closePaymentSchedule(invoice.getPaymentSchedule());
			
		}
		
		super.setMove( );
		
		Move move = invoice.getMove();
		if (move != null && invoice.getPaymentSchedule() != null && invoice.getEndOfCycleOk()) {
			
			reimbursementExportService.createReimbursementInvoice(invoice);
			
		}
		
	}

}