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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
		
		if(all().filter("self.statusSelect = ?1 AND self.invoiceDate > ?2 AND self.operationTypeSelect = ?3", 
				InvoiceService.STATUS_VENTILATED, invoice.getInvoiceDate(), invoice.getOperationTypeSelect()).count() > 0)  {
			throw new AxelorException(String.format("La date de facture ou d'avoir ne peut être antérieure à la date de la dernière facture ventilée"), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	protected LocalDate getDueDate()  {
		
		PaymentCondition paymentCondition = invoice.getPaymentCondition();
		
		switch (paymentCondition.getTypeSelect()) {
		case PaymentConditionRepository.TYPE_NET:
			
			return invoice.getInvoiceDate().plusDays(paymentCondition.getPaymentTime());
			
		case PaymentConditionRepository.TYPE_END_OF_MONTH_N_DAYS:
					
			return invoice.getInvoiceDate().dayOfMonth().withMaximumValue().plusDays(paymentCondition.getPaymentTime());
					
		case PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH:
			
			return invoice.getInvoiceDate().plusDays(paymentCondition.getPaymentTime()).dayOfMonth().withMaximumValue();
			
		case PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH_AT:
			
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
		invoice.setStatusSelect(InvoiceService.STATUS_VENTILATED);
	}
	
	/**
	 * Détermine le numéro de facture
	 * 
	 * @param invoice
	 * @param company
	 * @throws AxelorException 
	 */
	protected void setInvoiceId( ) throws AxelorException{
		
		if(!Strings.isNullOrEmpty(invoice.getInvoiceId()))  {  return;  }
		
		switch (invoice.getOperationTypeSelect()) {
		
		case InvoiceService.OPERATION_TYPE_SUPPLIER_PURCHASE:
			
			invoice.setInvoiceId(SequenceService.getSequenceNumber(IAdministration.SUPPLIER_INVOICE, invoice.getCompany()));
			break;
			
		case InvoiceService.OPERATION_TYPE_SUPPLIER_REFUND:
			
			invoice.setInvoiceId(SequenceService.getSequenceNumber(IAdministration.SUPPLIER_REFUND, invoice.getCompany()));
			break;

		case InvoiceService.OPERATION_TYPE_CLIENT_SALE:
			
			invoice.setInvoiceId(SequenceService.getSequenceNumber(IAdministration.CUSTOMER_INVOICE, invoice.getCompany()));
			break;
			
		case InvoiceService.OPERATION_TYPE_CLIENT_REFUND:
				
			invoice.setInvoiceId(SequenceService.getSequenceNumber(IAdministration.CUSTOMER_REFUND, invoice.getCompany()));
			break;
			
		default:
			break;
		}
		
		if (invoice.getInvoiceId() == null) {
			throw new AxelorException(String.format("La société %s n'a pas de séquence de facture ou d'avoir", invoice.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}

}