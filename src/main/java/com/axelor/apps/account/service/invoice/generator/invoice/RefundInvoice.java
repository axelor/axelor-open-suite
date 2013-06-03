package com.axelor.apps.account.service.invoice.generator.invoice;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;

public class RefundInvoice extends InvoiceGenerator implements InvoiceStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(RefundInvoice.class);
	private Invoice invoice;
	
	public RefundInvoice(Invoice invoice) {
		
//		super( invoice.getInvoiceTypeSelect() ); // mettre un type (client/fournisseur, achat, vente)
		super( 0 ); // mettre un type (client/fournisseur, achat, vente)

		this.invoice = invoice;
		
	}

	@Override
	public Invoice generate() throws AxelorException {
		
		LOG.debug("Créer un avoir pour la facture {}", new Object[] { invoice.getInvoiceId() });
		
		Invoice refund = JPA.copy(invoice, true);
		
		List<InvoiceLine> refundLines = new ArrayList<InvoiceLine>();
		refundLines.addAll( refund.getInvoiceLineList() );
		refundLines.addAll( refund.getTaxInvoiceLineList() );
		
		refundInvoiceLines(refundLines);
		
		populate( refund, refundLines );
		refund.setInvoiceId( sequenceService.getSequence(IAdministration.CUSTOMER_REFUND_DRAFT, refund.getCompany(), false) );
		refund.setMove(null);
		
		return refund;
		
	}
	
	@Override
	protected void populate(Invoice invoice, List<InvoiceLine> invoiceLines) throws AxelorException {
		
		super.populate(invoice, invoiceLines);
	}
	
	
	/**
	 * Mets à jour les lignes de facture en appliquant la négation aux prix unitaires et
	 * au total hors taxe.
	 * 
	 * @param invoiceLines
	 */
	protected void refundInvoiceLines(List<InvoiceLine> invoiceLines){
		
		for (InvoiceLine invoiceLine : invoiceLines){
			
			invoiceLine.setQty(invoiceLine.getQty().negate());
			invoiceLine.setExTaxTotal(invoiceLine.getExTaxTotal().negate());
			
		}
		
	}

}
