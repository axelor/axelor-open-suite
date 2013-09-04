/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
