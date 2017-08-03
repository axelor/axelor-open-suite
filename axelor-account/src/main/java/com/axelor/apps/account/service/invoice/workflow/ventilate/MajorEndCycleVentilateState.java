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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import javax.inject.Inject;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;

public class MajorEndCycleVentilateState extends VentilateState {

	@Inject
	private PaymentScheduleService paymentScheduleService;

	@Inject
	private ReimbursementExportService reimbursementExportService;


	public MajorEndCycleVentilateState(SequenceService sequenceService,
									   MoveService moveService,
									   AccountConfigService accountConfigService,
									   AppAccountService appAccountService,
									   InvoiceRepository invoiceRepo,
									   WorkflowVentilationService workflowService,
									   PaymentScheduleService paymentScheduleService,
									   ReimbursementExportService reimbursementExportService) {
		super(sequenceService, moveService, accountConfigService, appAccountService, invoiceRepo, workflowService);
		this.paymentScheduleService = paymentScheduleService;
		this.reimbursementExportService = reimbursementExportService;
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