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
		
		invoice.setDueDate(date.plusDays( invoice.getPaymentCondition().getPaymentTime() ) );

		
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