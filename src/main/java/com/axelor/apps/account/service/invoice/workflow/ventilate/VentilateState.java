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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.common.base.Preconditions;

public class VentilateState extends WorkflowInvoice {

	protected boolean useExcessPayment;
	protected SequenceService SequenceService;
	protected MoveService moveService;
	
	private boolean updateInvoiceDate;
	
	public VentilateState(SequenceService SequenceService, MoveService moveService, Invoice invoice) {
		
		super(invoice);
		this.SequenceService = SequenceService;
		this.moveService = moveService;
		this.updateInvoiceDate = true;
		this.useExcessPayment = true;
		
	}
	
	public void setUpdateInvoiceDate( boolean updateInvoiceDate ){
		this.updateInvoiceDate = updateInvoiceDate;
	}
	
	public void setUseExcessPayment( boolean useExcessPayment ){
		this.useExcessPayment = useExcessPayment;
	}
	
	@Override
	public void process( ) throws AxelorException {
		
		Preconditions.checkNotNull(invoice.getClientPartner());
		if (updateInvoiceDate) { setInvoiceDate( ); }
		
		setInvoiceId( );
		updatePaymentSchedule( );
		setMove( );
		setStatus( );
		
	}
	
	protected void updatePaymentSchedule( ){
		
		if ( invoice.getPaymentSchedule() != null ) { invoice.getPaymentSchedule().addInvoiceSetItem( invoice ); }
		
	}
	
	protected void setInvoiceDate( ){
		
		LocalDate date = GeneralService.getTodayDate();
		
		invoice.setInvoiceDate(date);
		
		if(invoice.getPaymentCondition() != null)  {
			date = date.plusDays( invoice.getPaymentCondition().getPaymentTime() );
		}
		
		invoice.setDueDate(date);
		
	}
	
	protected void setMove( ) throws AxelorException {
		
		Move move = null;
		
		// Création de l'écriture comptable
		move = moveService.createMove(invoice);
		
		if (move != null){
			
			invoice.setMove(move);
			
//			if ( invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == 1 ) {
//				// Emploie du trop perçu
//				moveService.createMoveUseExcessPayment(invoice);
//			} 
//			else if ()
			
		}
		
	}
	
	
	/**
	 * Détermine le numéro de facture
	 * 
	 * @param invoice
	 * @param company
	 * @throws AxelorException 
	 */
	protected void setStatus( ) {
		invoice.setStatus(Status.all().filter("self.code = ?1", "dis").fetchOne());
	}
	
	/**
	 * Détermine le numéro de facture
	 * 
	 * @param invoice
	 * @param company
	 * @throws AxelorException 
	 */
	protected void setInvoiceId( ) throws AxelorException{
		
		if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1) {
			invoice.setInvoiceId(SequenceService.getSequence(IAdministration.CUSTOMER_REFUND, invoice.getCompany(), false));
		}
		else { 
			invoice.setInvoiceId(SequenceService.getSequence(IAdministration.CUSTOMER_INVOICE, invoice.getCompany(), false));
		}
		
		if (invoice.getInvoiceId() == null) {
			throw new AxelorException(String.format("La société %s n'a pas de séquence de facture ou d'avoir", invoice.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}

}