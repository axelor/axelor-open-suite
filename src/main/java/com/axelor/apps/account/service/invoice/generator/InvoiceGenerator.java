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
package com.axelor.apps.account.service.invoice.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.generator.tax.TaxInvoiceLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public abstract class InvoiceGenerator {
	
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceGenerator.class);

	protected String exceptionMsg;
	protected JournalService journalService;
	
	protected boolean months30days;
	protected int operationType;
	protected Company company;
	protected PaymentCondition paymentCondition;
	protected PaymentMode paymentMode;
	protected Address mainInvoicingAddress;
	protected Partner partner;
	protected Partner contactPartner;
	protected Currency currency;
	protected Project project;
	protected LocalDate today;
	protected PriceList priceList;
	
	protected InvoiceGenerator(int operationType, Company company,PaymentCondition paymentCondition, PaymentMode paymentMode, Address mainInvoicingAddress, 
			Partner partner, Partner contactPartner, Currency currency, Project project, PriceList priceList) throws AxelorException {
		
		this.operationType = operationType;
		this.company = company;
		this.paymentCondition = paymentCondition;
		this.paymentMode = paymentMode;
		this.mainInvoicingAddress = mainInvoicingAddress;
		this.partner = partner;
		this.contactPartner = contactPartner;
		this.project = project;
		this.currency = currency;
		this.priceList = priceList;
		
		this.today = GeneralService.getTodayDate();
		this.exceptionMsg = GeneralService.getExceptionInvoiceMsg();
		this.journalService = new JournalService();
		
	}
	
	
	/**
	 * PaymentCondition, Paymentmode, MainInvoicingAddress, Currency récupérés du tiers
	 * @param operationType
	 * @param company
	 * @param partner
	 * @param contactPartner
	 * @throws AxelorException
	 */
	protected InvoiceGenerator(int operationType, Company company, Partner partner, Partner contactPartner, Project project, PriceList priceList) throws AxelorException {
		
		this.operationType = operationType;
		this.company = company;
		this.partner = partner;
		this.contactPartner = contactPartner;
		this.priceList = priceList;
		this.project = project;
		
		this.today = GeneralService.getTodayDate();
		this.exceptionMsg = GeneralService.getExceptionInvoiceMsg();
		this.journalService = new JournalService();
		
	}
	
	
	protected InvoiceGenerator() {
		
		this.today = GeneralService.getTodayDate();
		this.exceptionMsg = GeneralService.getExceptionInvoiceMsg();
		
	}
	
	protected int inverseOperationType(int operationType) throws AxelorException  {

		switch(operationType)  {
		
			case IInvoice.SUPPLIER_PURCHASE:
				return IInvoice.SUPPLIER_REFUND;
			case IInvoice.SUPPLIER_REFUND:
				return IInvoice.SUPPLIER_PURCHASE;
			case IInvoice.CLIENT_SALE:
				return IInvoice.CLIENT_REFUND;
			case IInvoice.CLIENT_REFUND:
				return IInvoice.CLIENT_SALE;
			default:
				throw new AxelorException(String.format("%s :\nLe type de facture n'est pas rempli %s", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		
	}
	
	
	abstract public Invoice generate() throws AxelorException;
	
	
	protected Invoice createInvoiceHeader() throws AxelorException  {
		
		Invoice invoice = new Invoice();
		
		invoice.setOperationTypeSelect(operationType);
		
		invoice.setInvoiceDate(this.today);
		
		if(partner == null)  {
			throw new AxelorException(String.format("%s :\nAucun tiers selectionné", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		invoice.setPartner(partner);
		
		if(paymentCondition == null)  {
			paymentCondition = partner.getPaymentCondition();
		}
		if(paymentCondition == null)  {
			throw new AxelorException(String.format("%s :\nCondition de paiement absent", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		invoice.setPaymentCondition(paymentCondition);
		
		invoice.setDueDate(this.today.plusDays(paymentCondition.getPaymentTime()));
		
		if(paymentMode == null)  {
			paymentMode = partner.getPaymentMode();
		}
		if(paymentMode == null)  {
			throw new AxelorException(String.format("%s :\nMode de paiement absent", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		invoice.setPaymentMode(paymentMode);
		
		if(mainInvoicingAddress == null)  {
			mainInvoicingAddress = partner.getMainInvoicingAddress();
		}
		if(mainInvoicingAddress == null)  {
			throw new AxelorException(String.format("%s :\nAdresse de facturation absente", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		
		invoice.setAddress(mainInvoicingAddress);
		
		invoice.setContactPartner(contactPartner);
		
		if(currency == null)  {
			currency = partner.getCurrency();
		}
		if(currency == null)  {
			throw new AxelorException(String.format("%s :\nDevise absente", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		invoice.setCurrency(currency);
		
		invoice.setProject(project);
		
		invoice.setCompany(company);
		
		invoice.setPartnerAccount(this.getCustomerAccount(partner, company));
		
		invoice.setJournal(journalService.getJournal(invoice)); 
		
		invoice.setStatus(Status.all().filter("code = 'dra'").fetchOne());
		
		invoice.setPriceList(priceList);
		
		initCollections(invoice);
		
		return invoice;
	}
	

	public Account getCustomerAccount(Partner partner, Company company) throws AxelorException  {
			
		Account partnerAccount = null;
		
		for(AccountingSituation accountingSituation : partner.getAccountingSituationList())  {
			
			if(accountingSituation.getCompany().equals(company))  {
				
				partnerAccount = accountingSituation.getCustomerAccount();
				
			}
		}
		
		if(partnerAccount == null)  {
			
			AccountConfigService accountConfigService = new AccountConfigService();
			
			partnerAccount = accountConfigService.getCustomerAccount(accountConfigService.getAccountConfig(company));
			
		}
		
		if(partnerAccount == null)  {
			
			throw new AxelorException(String.format("%s :\nCompte comptable manquant pour la société %s", 
					GeneralService.getExceptionInvoiceMsg(), company.getName()), IException.MISSING_FIELD);			
			
		}
		
		return partnerAccount;
			
	}
	
	
	
	/**
	 * Peupler une facture.
	 * <p>
	 * Cette fonction permet de déterminer de déterminer les tva d'une facture à partir des lignes de factures  en paramètres. 
	 * </p>
	 * 
	 * @param invoice
	 * @param invoiceLines
	 * 
	 * @throws AxelorException
	 */
	public void populate(Invoice invoice, List<InvoiceLine> invoiceLines) throws AxelorException {
		
		LOG.debug("Peupler une facture => lignes de factures: {} ", new Object[] {  invoiceLines.size() });
		
		initCollections( invoice );
		
		invoice.getInvoiceLineList().addAll(invoiceLines);
		
		// create Tva lines
		invoice.getInvoiceLineTaxList().addAll((new TaxInvoiceLine(invoice, invoiceLines)).creates());
		
		computeInvoice(invoice);
		
	}
	
	
	/**
	 * Initialiser l'ensemble des Collections d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initCollections(Invoice invoice){

		initInvoiceLineTaxList(invoice);
		initInvoiceLineList(invoice);
		
	}
	
	/**
	 * Initialiser l'ensemble des listes de ligne de facture d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initInvoiceLineList(Invoice invoice) {
		
		if (invoice.getInvoiceLineList() == null) { invoice.setInvoiceLineList(new ArrayList<InvoiceLine>()); }
		else  {  invoice.getInvoiceLineList().clear();  }
		
	}
	
	
	/**
	 * Initialiser l'ensemble des listes de ligne de tva d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initInvoiceLineTaxList(Invoice invoice) {
		
		if (invoice.getInvoiceLineTaxList() == null) { invoice.setInvoiceLineTaxList(new ArrayList<InvoiceLineTax>()); }
		else { invoice.getInvoiceLineTaxList().clear(); }
		
	}

	/**
	 * Calculer le montant d'une facture.
	 * <p> 
	 * Le calcul est basé sur les lignes de TVA préalablement créées.
	 * </p>
	 * 
	 * @param invoice
	 * @throws AxelorException 
	 */
	public void computeInvoice(Invoice invoice) throws AxelorException {
		
		// Dans la devise de la comptabilité du tiers
		invoice.setExTaxTotal( BigDecimal.ZERO );
		invoice.setTaxTotal( BigDecimal.ZERO );
		invoice.setInTaxTotal( BigDecimal.ZERO );
		
		// Dans la devise de la facture
		invoice.setInvoiceExTaxTotal(BigDecimal.ZERO);
		invoice.setInvoiceTaxTotal(BigDecimal.ZERO);
		invoice.setInvoiceInTaxTotal(BigDecimal.ZERO);
		
		for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
			
			// Dans la devise de la comptabilité du tiers
			invoice.setExTaxTotal(invoice.getExTaxTotal().add( invoiceLineTax.getAccountingExTaxBase() ));
			invoice.setTaxTotal(invoice.getTaxTotal().add( invoiceLineTax.getAccountingTaxTotal() ));
			invoice.setInTaxTotal(invoice.getInTaxTotal().add( invoiceLineTax.getAccountingInTaxTotal() ));
			
			// Dans la devise de la facture
			invoice.setInvoiceExTaxTotal(invoice.getInvoiceExTaxTotal().add( invoiceLineTax.getExTaxBase() ));
			invoice.setInvoiceTaxTotal(invoice.getInvoiceTaxTotal().add( invoiceLineTax.getTaxTotal() ));
			invoice.setInvoiceInTaxTotal(invoice.getInvoiceInTaxTotal().add( invoiceLineTax.getInTaxTotal() ));
			
		}
		
		LOG.debug("Montant de la facture: HT = {}, TVA = {}, TTC = {}",
			new Object[] { invoice.getExTaxTotal(), invoice.getTaxTotal(), invoice.getInTaxTotal() });
		
	}

}
