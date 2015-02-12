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
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class VentilateState extends WorkflowInvoice {

	protected SequenceService sequenceService;
	protected MoveService moveService;
	
	public VentilateState(SequenceService sequenceService, MoveService moveService, Invoice invoice) {
		
		super(invoice);
		this.sequenceService = sequenceService;
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
				InvoiceRepository.STATUS_VENTILATED, invoice.getInvoiceDate(), invoice.getOperationTypeSelect()).count() > 0)  {
			throw new AxelorException(I18n.get(IExceptionMessage.VENTILATE_STATE_1), IException.CONFIGURATION_ERROR);
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
		
		if (move != null && Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany()).getAutoReconcileOnInvoice())  {
			
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
		invoice.setStatusSelect(InvoiceRepository.STATUS_VENTILATED);
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
		
		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
			
			invoice.setInvoiceId(sequenceService.getSequenceNumber(IAdministration.SUPPLIER_INVOICE, invoice.getCompany()));
			break;
			
		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
			
			invoice.setInvoiceId(sequenceService.getSequenceNumber(IAdministration.SUPPLIER_REFUND, invoice.getCompany()));
			break;

		case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
			
			invoice.setInvoiceId(sequenceService.getSequenceNumber(IAdministration.CUSTOMER_INVOICE, invoice.getCompany()));
			break;
			
		case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
				
			invoice.setInvoiceId(sequenceService.getSequenceNumber(IAdministration.CUSTOMER_REFUND, invoice.getCompany()));
			break;
			
		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.JOURNAL_1),invoice.getInvoiceId()), IException.MISSING_FIELD);
		}
		
		if (invoice.getInvoiceId() == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.VENTILATE_STATE_2), invoice.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
	}

}