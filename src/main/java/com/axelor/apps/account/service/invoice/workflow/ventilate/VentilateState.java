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

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.IPaymentCondition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.common.base.Preconditions;

public class VentilateState extends WorkflowInvoice {

	protected SequenceService SequenceService;
	protected MoveService moveService;
	
	
	public VentilateState(SequenceService SequenceService, MoveService moveService, Invoice invoice) {
		
		super(invoice);
		this.SequenceService = SequenceService;
		this.moveService = moveService;
		
	}
	
	
	@Override
	public void process( ) throws AxelorException {
		
		Preconditions.checkNotNull(invoice.getPartner());
		setDueDate( );
		
		setInvoiceId( );
		updatePaymentSchedule( );
		setMove( );
		setStatus( );
		
	}
	
	protected void updatePaymentSchedule( ){
		
		if ( invoice.getPaymentSchedule() != null ) { invoice.getPaymentSchedule().addInvoiceSetItem( invoice ); }
		
	}
	
	protected void setDueDate( ) throws AxelorException{
		
		this.checkInvoiceDate();
		
		if(!invoice.getPaymentCondition().getIsFree())  {
			invoice.setDueDate(this.getDueDate());
		}
		
	}
	
	
	protected void checkInvoiceDate() throws AxelorException  {
		
		if(Invoice.filter("self.status.code = 'dis' AND self.invoiceDate > ?1 AND self.operationTypeSelect = ?2", invoice.getInvoiceDate(),invoice.getOperationTypeSelect()).count() > 0)  {
			throw new AxelorException(String.format("La date de facture ou d'avoir ne peut être antérieure à la date de la dernière facture ventilée"), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	protected LocalDate getDueDate()  {
		
		PaymentCondition paymentCondition = invoice.getPaymentCondition();
		
		switch (paymentCondition.getTypeSelect()) {
		case IPaymentCondition.TYPE_NET:
			
			return invoice.getInvoiceDate().plusDays(paymentCondition.getPaymentTime());
			
		case IPaymentCondition.TYPE_END_OF_MONTH_N_DAYS:
					
			return invoice.getInvoiceDate().dayOfMonth().withMaximumValue().plusDays(paymentCondition.getPaymentTime());
					
		case IPaymentCondition.TYPE_N_DAYS_END_OF_MONTH:
			
			return invoice.getInvoiceDate().plusDays(paymentCondition.getPaymentTime()).dayOfMonth().withMaximumValue();
			
		case IPaymentCondition.TYPE_N_DAYS_END_OF_MONTH_AT:
			
			return invoice.getInvoiceDate().plusDays(paymentCondition.getPaymentTime()).dayOfMonth().withMaximumValue().plusDays(paymentCondition.getDaySelect());

		default:
			return invoice.getInvoiceDate();
		}
		
	}
	
	protected void setMove( ) throws AxelorException {
		
		// Création de l'écriture comptable
		Move move = moveService.createMove(invoice);
		
		if (move != null)  {
			
			moveService.createMoveUseExcessPaymentOrDue(invoice);
			
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
		
		switch (invoice.getOperationTypeSelect()) {
		
				
		case IInvoice.SUPPLIER_PURCHASE:
			
			invoice.setInvoiceId(SequenceService.getSequence(IAdministration.SUPPLIER_INVOICE, invoice.getCompany(), false));
			break;
			
		case IInvoice.SUPPLIER_REFUND:
			
			invoice.setInvoiceId(SequenceService.getSequence(IAdministration.SUPPLIER_REFUND, invoice.getCompany(), false));
			break;

		case IInvoice.CLIENT_SALE:
			
			invoice.setInvoiceId(SequenceService.getSequence(IAdministration.CUSTOMER_INVOICE, invoice.getCompany(), false));
			break;
			
		case IInvoice.CLIENT_REFUND:
				
			invoice.setInvoiceId(SequenceService.getSequence(IAdministration.CUSTOMER_REFUND, invoice.getCompany(), false));
			break;
			
		default:
			break;
		}
		
		if (invoice.getInvoiceId() == null) {
			throw new AxelorException(String.format("La société %s n'a pas de séquence de facture ou d'avoir", invoice.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}

}