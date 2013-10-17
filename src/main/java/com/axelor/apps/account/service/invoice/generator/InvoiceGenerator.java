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
import com.axelor.apps.account.db.InvoiceLineVat;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.invoice.generator.tax.VatInvoiceLine;
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
	protected Partner clientPartner;
	protected Partner contactPartner;
	protected Currency currency;
	protected Project affairProject;
	protected LocalDate today;
	protected PriceList priceList;
	
	protected InvoiceGenerator(int operationType, Company company,PaymentCondition paymentCondition, PaymentMode paymentMode, Address mainInvoicingAddress, 
			Partner clientPartner, Partner contactPartner, Currency currency, Project affairProject, PriceList priceList) throws AxelorException {
		
		this.operationType = operationType;
		this.company = company;
		this.paymentCondition = paymentCondition;
		this.paymentMode = paymentMode;
		this.mainInvoicingAddress = mainInvoicingAddress;
		this.clientPartner = clientPartner;
		this.contactPartner = contactPartner;
		this.affairProject = affairProject;
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
	 * @param clientPartner
	 * @param contactPartner
	 * @throws AxelorException
	 */
	protected InvoiceGenerator(int operationType, Company company, Partner clientPartner, Partner contactPartner, PriceList priceList) throws AxelorException {
		
		this.operationType = operationType;
		this.company = company;
		this.clientPartner = clientPartner;
		this.contactPartner = contactPartner;
		this.priceList = priceList;
		
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
		
		if(clientPartner == null)  {
			throw new AxelorException(String.format("%s :\nAucun tiers selectionné %s", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		invoice.setClientPartner(clientPartner);
		
		if(paymentCondition == null)  {
			paymentCondition = clientPartner.getPaymentCondition();
		}
//		if(paymentCondition == null)  {
//			throw new AxelorException(String.format("%s :\nCondition de paiement absent", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
//		}
		invoice.setPaymentCondition(paymentCondition);
		
		invoice.setDueDate(this.today.plusDays(paymentCondition.getPaymentTime()));
		
		if(paymentMode == null)  {
			paymentMode = clientPartner.getPaymentMode();
		}
		if(paymentMode == null)  {
			throw new AxelorException(String.format("%s :\nMode de paiement absent", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		invoice.setPaymentMode(paymentMode);
		
		if(mainInvoicingAddress == null)  {
			mainInvoicingAddress = clientPartner.getMainInvoicingAddress();
		}
		if(mainInvoicingAddress == null)  {
			throw new AxelorException(String.format("%s :\nAdresse de facturation absente", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		
		invoice.setAddress(mainInvoicingAddress);
		
		invoice.setContactPartner(contactPartner);
		
		if(currency == null)  {
			currency = clientPartner.getCurrency();
		}
		if(currency == null)  {
			throw new AxelorException(String.format("%s :\nDevise absente", GeneralService.getExceptionInvoiceMsg()), IException.MISSING_FIELD);	
		}
		invoice.setCurrency(currency);
		
		invoice.setProject(affairProject);
		
		invoice.setCompany(company);
		
		invoice.setPartnerAccount(this.getCustomerAccount(clientPartner, company));
		
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
			
			partnerAccount = company.getCustomerAccount();
			
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
	 * Cette fonction permet de déterminer de déterminer les tva d'une facture. 
	 * </p>
	 * 
	 * @param invoice
	 * 
	 * @throws AxelorException
	 */
	public void populate(Invoice invoice) throws AxelorException {
		
		this.populate(invoice, invoice.getInvoiceLineList());
		
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
		
		// create Tva lines
		invoice.getInvoiceLineVatList().addAll((new VatInvoiceLine(invoice, invoiceLines)).creates());
		
		computeInvoice(invoice);
		
	}
	
	
	/**
	 * Initialiser l'ensemble des Collections d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initCollections(Invoice invoice){

		initInvoiceLineVats(invoice);
		initInvoiceLines(invoice);
		
	}
	
	/**
	 * Initialiser l'ensemble des listes de ligne de facture d'une facture 
	 * 
	 * @param invoice
	 */
	protected void initInvoiceLines(Invoice invoice) {
		
		if (invoice.getInvoiceLineList() == null) { invoice.setInvoiceLineList(new ArrayList<InvoiceLine>()); }
		
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
		invoice.setExTaxTotal( BigDecimal.ZERO );
		invoice.setVatTotal( BigDecimal.ZERO );
		invoice.setInTaxTotal( BigDecimal.ZERO );
		
		// Dans la devise de la facture
		invoice.setInvoiceExTaxTotal(BigDecimal.ZERO);
		invoice.setInvoiceVatTotal(BigDecimal.ZERO);
		invoice.setInvoiceInTaxTotal(BigDecimal.ZERO);
		
		for (InvoiceLineVat vatLine : invoice.getInvoiceLineVatList()) {
			
			// Dans la devise de la comptabilité du tiers
			invoice.setExTaxTotal(invoice.getExTaxTotal().add( vatLine.getAccountingExTaxBase() ));
			invoice.setVatTotal(invoice.getVatTotal().add( vatLine.getAccountingVatTotal() ));
			invoice.setInTaxTotal(invoice.getInTaxTotal().add( vatLine.getAccountingInTaxTotal() ));
			
			// Dans la devise de la facture
			invoice.setInvoiceExTaxTotal(invoice.getInvoiceExTaxTotal().add( vatLine.getExTaxBase() ));
			invoice.setInvoiceVatTotal(invoice.getInvoiceVatTotal().add( vatLine.getVatTotal() ));
			invoice.setInvoiceInTaxTotal(invoice.getInvoiceInTaxTotal().add( vatLine.getInTaxTotal() ));
			
		}
		
		LOG.debug("Montant de la facture: HT = {}, TVA = {}, TTC = {}",
			new Object[] { invoice.getExTaxTotal(), invoice.getVatTotal(), invoice.getInTaxTotal() });
		
	}

}
