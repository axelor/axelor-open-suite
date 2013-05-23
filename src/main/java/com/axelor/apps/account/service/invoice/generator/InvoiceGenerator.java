package com.axelor.apps.account.service.invoice.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoiceLineTaxHistory;
import com.axelor.apps.account.db.InvoiceLineVat;
import com.axelor.apps.account.service.invoice.generator.tax.TaxLine;
import com.axelor.apps.account.service.invoice.generator.tax.VatInvoiceLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public abstract class InvoiceGenerator {
	
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceGenerator.class);

	protected String exceptionMsg;
	protected SequenceService sequenceService;
	
	protected boolean months30days;
	protected int invoiceType;
	protected Partner partner;
	protected Company company;
	protected LocalDate date;
	
	protected InvoiceGenerator(Partner partner, int invoiceType, Company company) throws AxelorException {
		
		this.partner = partner;
		this.company = company;
		this.invoiceType = invoiceType;
		
		this.date = GeneralService.getTodayDate();
		this.exceptionMsg = GeneralService.getExceptionInvoiceMsg();
		this.sequenceService = new SequenceService(date);
		
	}
	
	
	protected InvoiceGenerator(int invoiceType) {
		
		this.invoiceType = invoiceType;
		
		this.date = GeneralService.getTodayDate();
		this.exceptionMsg = GeneralService.getExceptionInvoiceMsg();
		this.sequenceService = new SequenceService(date);
		
	}
	
	abstract public Invoice generate() throws AxelorException;
	
	protected Invoice createInvoice() throws AxelorException {
		
		Invoice invoice = new Invoice();

		invoice.setInvoiceDate(date);
		
		invoice.setStatus(Status.all().filter("code = 'dra'").fetchOne());
		
		updateInvoice(invoice, partner, company);
		
		initCollections(invoice);
		
		return invoice;
	}
	
	protected void updateInvoice(Invoice invoice, Partner partner, Company company) throws AxelorException{

		invoice.setPartner(partner);
		invoice.setAddress(partner.getMainInvoicingAddress());
		invoice.setCompany(company);
		invoice.setPartnerAccount(company.getCustomerAccount());
		if (invoice.getPartnerAccount() == null) {
			throw new AxelorException(String.format("%s :\nCompte comptable manquant pour la société %s", exceptionMsg, company.getName()), IException.MISSING_FIELD);			
		}
		invoice.setPaymentMode(partner.getPaymentMode());
//		if (partner.getPaymentTime() != null) {
//			invoice.setDueDate(date.plusDays(partner.getPaymentTime()));
//		}
		
		if (invoice.getPaymentCondition() != null && invoice.getPaymentCondition().getPaymentTime() != null) {
			invoice.setDueDate(date.plusDays(invoice.getPaymentCondition().getPaymentTime()));
		}
	}
	

	/**
	 * Peupler une facture.
	 * <p>
	 * Cette fonction permet de déterminer d'ajouter les lignes de taxes passées en paramètres et de déterminer les tva d'une facture à partir des lignes de factures et de taxes passées en paramètres. 
	 * Ces lignes de factures sont au préalable réparties entre les lignes de factures concernant uniquement les taxes et les lignes standards.
	 * </p>
	 * 
	 * @param invoice
	 * @param contractLine
	 * @param invoiceLines
	 * @param invoiceLineTaxes
	 * @param standard
	 * 
	 * @throws AxelorException
	 */
	protected void populate(Invoice invoice, List<InvoiceLine> invoiceLines) throws AxelorException {
		
		LOG.debug("Peupler une facture => lignes de factures: {} ", new Object[] { invoiceLines.size() });
		
		initCollections( invoice );
		dispatchInvoiceLine( invoice, invoiceLines );
		
		// Add Tax lines
		TaxLine taxGenerator = new TaxLine(invoice, invoiceLines);
		invoice.getInvoiceLineTaxList().addAll( taxGenerator.creates() );
		// Add Tax lines history		
		invoice.getInvoiceLineTaxHistoryList().addAll(taxGenerator.getInvoiceLineTaxHistories());
		// create Tva lines
		invoice.getInvoiceLineVatList().addAll((new VatInvoiceLine(invoice, invoice.getInvoiceLineList(), invoice.getInvoiceLineTaxList())).creates());
		
		computeInvoice(invoice);
		
	}
	
	/**
	 * Initialiser l'ensemble des Collections d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initCollections(Invoice invoice){

		initInvoiceLineVats(invoice);
		initInvoiceLineTaxes(invoice);
		initInvoiceLines(invoice);
		
	}
	
	/**
	 * Initialiser l'ensemble des listes de ligne de facture d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initInvoiceLines(Invoice invoice) {
		
		if (invoice.getInvoiceLineList() == null) { invoice.setInvoiceLineList(new ArrayList<InvoiceLine>()); }
		else { invoice.getInvoiceLineList().clear(); }
		
		if (invoice.getTaxInvoiceLineList() == null) { invoice.setTaxInvoiceLineList(new ArrayList<InvoiceLine>()); }
		else { invoice.getTaxInvoiceLineList().clear(); }
		
	}
	
	/**
	 * Initialiser l'ensemble des listes de ligne de taxes d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initInvoiceLineTaxes(Invoice invoice) {
		
		if (invoice.getInvoiceLineTaxHistoryList() == null) { invoice.setInvoiceLineTaxHistoryList(new ArrayList<InvoiceLineTaxHistory>()); }
		else { invoice.getInvoiceLineTaxHistoryList().clear(); }
		
		if (invoice.getInvoiceLineTaxList() == null) { invoice.setInvoiceLineTaxList(new ArrayList<InvoiceLineTax>()); }
		else { invoice.getInvoiceLineTaxList().clear(); }

		invoice.flush();
		
	}
	
	/**
	 * Initialiser l'ensemble des listes de ligne de tva d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initInvoiceLineVats(Invoice invoice) {
		
		if (invoice.getInvoiceLineVatList() == null) { invoice.setInvoiceLineVatList(new ArrayList<InvoiceLineVat>()); }
		else { invoice.getInvoiceLineVatList().clear(); }
		
	}

	/**
	 * Calculer le montant d'une facture.
	 * <p> 
	 * Le calcul est basé sur les lignes de TVA préalablement créées.
	 * </p>
	 * 
	 * @param invoice
	 * @param vatLines
	 * @throws AxelorException 
	 */
	public void computeInvoice(Invoice invoice) throws AxelorException {
		
		// Dans la devise de la comptabilité du tiers
		invoice.setExAllTaxTotal( BigDecimal.ZERO );
		invoice.setExTaxTotal( BigDecimal.ZERO );
		invoice.setVatTotal( BigDecimal.ZERO );
		invoice.setInTaxTotal( BigDecimal.ZERO );
		
		// Dans la devise de la facture
		invoice.setInvoiceExAllTaxTotal(BigDecimal.ZERO);
		invoice.setInvoiceExTaxTotal(BigDecimal.ZERO);
		invoice.setInvoiceVatTotal(BigDecimal.ZERO);
		invoice.setInvoiceInTaxTotal(BigDecimal.ZERO);
		
		for (InvoiceLineVat vatLine : invoice.getInvoiceLineVatList()) {
			
			// Dans la devise de la comptabilité du tiers
			invoice.setExAllTaxTotal(invoice.getExAllTaxTotal().add( vatLine.getAccountingExAllTaxBase() ));
			invoice.setExTaxTotal(invoice.getExTaxTotal().add( vatLine.getAccountingExTaxBase() ));
			invoice.setVatTotal(invoice.getVatTotal().add( vatLine.getAccountingVatTotal() ));
			invoice.setInTaxTotal(invoice.getInTaxTotal().add( vatLine.getAccountingInTaxTotal() ));
			
			// Dans la devise de la facture
			invoice.setInvoiceExAllTaxTotal(invoice.getInvoiceExAllTaxTotal().add( vatLine.getExAllTaxBase() ));
			invoice.setInvoiceExTaxTotal(invoice.getInvoiceExTaxTotal().add( vatLine.getExTaxBase() ));
			invoice.setInvoiceVatTotal(invoice.getInvoiceVatTotal().add( vatLine.getVatTotal() ));
			invoice.setInvoiceInTaxTotal(invoice.getInvoiceInTaxTotal().add( vatLine.getInTaxTotal() ));
			
		}
		
		LOG.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
			new Object[] { invoice.getExAllTaxTotal(), invoice.getExTaxTotal(), invoice.getVatTotal(), invoice.getInTaxTotal() });
		
	}

	/**
	 * Répartir les lignes de factures entre les deux listes de la factures si
	 * celle-ci sont liées.
	 * 
	 * @param invoice
	 * @param invoiceLines
	 */
	protected void dispatchInvoiceLine(Invoice invoice, List<InvoiceLine> invoiceLines) {

		for (InvoiceLine invoiceLine : invoiceLines) {

			if (invoiceLine.getInvoice() != null) {
				invoice.getInvoiceLineList().add(invoiceLine); 
			}
			if (invoiceLine.getTaxInvoice() != null) {
				invoice.getTaxInvoiceLineList().add(invoiceLine);
			}
		}

	}
	
}
