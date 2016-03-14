/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.tax.TaxInvoiceLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public abstract class InvoiceGenerator  {
	

	private final Logger logger = LoggerFactory.getLogger(getClass());

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
	protected LocalDate today;
	protected PriceList priceList;
	protected String internalReference;
	protected String externalReference;
	protected Boolean inAti;
	protected BankDetails companyBankDetails;

	protected InvoiceGenerator(int operationType, Company company, PaymentCondition paymentCondition, PaymentMode paymentMode, Address mainInvoicingAddress,
			Partner partner, Partner contactPartner, Currency currency, PriceList priceList, String internalReference, String externalReference, Boolean inAti, 
			BankDetails companyBankDetails) throws AxelorException {

		this.operationType = operationType;
		this.company = company;
		this.paymentCondition = paymentCondition;
		this.paymentMode = paymentMode;
		this.mainInvoicingAddress = mainInvoicingAddress;
		this.partner = partner;
		this.contactPartner = contactPartner;
		this.currency = currency;
		this.priceList = priceList;
		this.internalReference = internalReference;
		this.externalReference = externalReference;
		this.inAti = inAti;
		this.companyBankDetails = companyBankDetails;
		this.today = Beans.get(GeneralService.class).getTodayDate();
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
	protected InvoiceGenerator(int operationType, Company company, Partner partner, Partner contactPartner, PriceList priceList,
			String internalReference, String externalReference, Boolean inAti) throws AxelorException {

		this.operationType = operationType;
		this.company = company;
		this.partner = partner;
		this.contactPartner = contactPartner;
		this.priceList = priceList;
		this.internalReference = internalReference;
		this.externalReference = externalReference;
		this.inAti = inAti;
		this.today = Beans.get(GeneralService.class).getTodayDate();
		this.journalService = new JournalService();

	}


	protected InvoiceGenerator() {
		this.today = Beans.get(GeneralService.class).getTodayDate();
		this.journalService = new JournalService();

	}

	protected int inverseOperationType(int operationType) throws AxelorException  {

		switch(operationType)  {

			case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
				return InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
			case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
				return InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
			case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
				return InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
			case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
				return InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICE_GENERATOR_1), GeneralServiceImpl.EXCEPTION), IException.MISSING_FIELD);
		}

	}

	
	abstract public Invoice generate() throws AxelorException;


	protected Invoice createInvoiceHeader() throws AxelorException  {

		Invoice invoice = new Invoice();
		
		invoice.setCompany(company);

		invoice.setOperationTypeSelect(operationType);

		if(partner == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICE_GENERATOR_2), GeneralServiceImpl.EXCEPTION), IException.MISSING_FIELD);
		}
		invoice.setPartner(partner);

		if(paymentCondition == null)  {
			paymentCondition = partner.getPaymentCondition();
		}
		if(paymentCondition == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICE_GENERATOR_3), GeneralServiceImpl.EXCEPTION), IException.MISSING_FIELD);
		}
		invoice.setPaymentCondition(paymentCondition);

		if(paymentMode == null)  {
			paymentMode = partner.getPaymentMode();
		}
		if(paymentMode == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICE_GENERATOR_4), GeneralServiceImpl.EXCEPTION), IException.MISSING_FIELD);
		}
		invoice.setPaymentMode(paymentMode);

		if(mainInvoicingAddress == null)  {
			mainInvoicingAddress = Beans.get(PartnerService.class).getInvoicingAddress(partner);
		}
		if(mainInvoicingAddress == null && partner.getIsCustomer())  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICE_GENERATOR_5), GeneralServiceImpl.EXCEPTION), IException.MISSING_FIELD);
		}

		invoice.setAddress(mainInvoicingAddress);

		invoice.setContactPartner(contactPartner);

		if(currency == null)  {
			currency = partner.getCurrency();
		}
		if(currency == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICE_GENERATOR_6), GeneralServiceImpl.EXCEPTION), IException.MISSING_FIELD);
		}
		invoice.setCurrency(currency);

		invoice.setPartnerAccount(Beans.get(AccountCustomerService.class).getPartnerAccount(partner, company, operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE || operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND));

		invoice.setJournal(journalService.getJournal(invoice));

		invoice.setStatusSelect(InvoiceRepository.STATUS_DRAFT);

		if(priceList == null)  {
			if(InvoiceToolService.isPurchase(invoice))  {
				priceList = partner.getPurchasePriceList();
			}
			else  {
				priceList = partner.getSalePriceList();
			}
		}

		invoice.setPriceList(priceList);

		invoice.setInternalReference(internalReference);

		invoice.setExternalReference(externalReference);

		// Set ATI mode on invoice
		AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		int atiChoice = accountConfig.getInvoiceInAtiSelect();
		
		if(inAti == null)  {  
			invoice.setInAti(accountConfigService.getInvoiceInAti(accountConfig));
		}
		else if(atiChoice == AccountConfigRepository.INVOICE_ATI_DEFAULT || atiChoice == AccountConfigRepository.INVOICE_WT_DEFAULT)  {
			invoice.setInAti(inAti);
		}
		else if(atiChoice == AccountConfigRepository.INVOICE_ATI_ALWAYS)  {
			invoice.setInAti(true);
		}
		else {
			invoice.setInAti(false);
		}
		
		// Set Company bank details
		if(companyBankDetails == null)  {
			AccountingSituation accountingSituation = Beans.get(AccountingSituationService.class).getAccountingSituation(partner, company);
			if(accountingSituation != null)  {
				companyBankDetails = accountingSituation.getCompanyBankDetails();
			}
			else  {
				companyBankDetails = company.getDefaultBankDetails();
			}
		}
		invoice.setCompanyBankDetails(companyBankDetails);
		
		initCollections(invoice);

		return invoice;
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

		logger.debug("Peupler une facture => lignes de factures: {} ", new Object[] {  invoiceLines.size() });

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
		invoice.setCompanyExTaxTotal(BigDecimal.ZERO);
		invoice.setCompanyTaxTotal(BigDecimal.ZERO);
		invoice.setCompanyInTaxTotal(BigDecimal.ZERO);

		for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {

			// Dans la devise de la comptabilité du tiers
			invoice.setExTaxTotal(invoice.getExTaxTotal().add( invoiceLineTax.getExTaxBase() ));
			invoice.setTaxTotal(invoice.getTaxTotal().add( invoiceLineTax.getTaxTotal() ));
			invoice.setInTaxTotal(invoice.getInTaxTotal().add( invoiceLineTax.getInTaxTotal() ));

			// Dans la devise de la facture
			invoice.setCompanyExTaxTotal(invoice.getCompanyExTaxTotal().add( invoiceLineTax.getCompanyExTaxBase() ));
			invoice.setCompanyTaxTotal(invoice.getCompanyTaxTotal().add( invoiceLineTax.getCompanyTaxTotal() ));
			invoice.setCompanyInTaxTotal(invoice.getCompanyInTaxTotal().add( invoiceLineTax.getCompanyInTaxTotal() ));

		}

		logger.debug("Montant de la facture: HT = {}, TVA = {}, TTC = {}",
			new Object[] { invoice.getExTaxTotal(), invoice.getTaxTotal(), invoice.getInTaxTotal() });

	}

}
