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
package com.axelor.apps.account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.PaymentVoucherService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InterbankPaymentOrderImportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(InterbankPaymentOrderImportService.class); 
	
	@Inject
	private PaymentVoucherService pvs;
	
	@Inject
	private CfonbService cs;
	
	@Inject
	private RejectImportService ris;
	
	@Inject
	private BankDetailsService bds;
	
	private DateTime dateTime;

	@Inject
	public InterbankPaymentOrderImportService() {

		this.dateTime = GeneralService.getTodayDateTime();
		
	}
	
	public void runInterbankPaymentOrderImport(Company company) throws AxelorException, IOException  {
		
		this.testCompanyField(company);
		
		String dest = ris.getDestCFONBFile(company.getInterbankPaymentOrderImportPathCFONB(), company.getTempInterbankPaymentOrderImportPathCFONB());
		
		// Récupération des enregistrements
		List<String[]> file = cs.importCFONB(dest, company, 3, 4);	
		for(String[] payment : file)  {
			
			this.runInterbankPaymentOrder(payment, company);
		}
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentVoucher runInterbankPaymentOrder(String[] payment, Company company) throws AxelorException  {
		Invoice invoice = this.getInvoice(payment[1], company);
		
		PaymentMode paymentMode = cs.getPaymentMode(invoice.getCompany(), payment[0]);
		LOG.debug("Mode de paiement récupéré depuis l'enregistrement CFONB : {}", new Object[]{paymentMode.getName()});
		
		BigDecimal amount = new BigDecimal(payment[5]);
		
		if(this.bankDetailsMustBeUpdate(payment[4]))  {
			this.updateBankDetails(payment, invoice, paymentMode);
		}
		
		return pvs.createPaymentVoucherIPO(invoice, this.dateTime, amount, paymentMode);
	}
	
	
	public void updateBankDetails(String[] payment, Invoice invoice, PaymentMode paymentMode)  {
		LOG.debug("Mise à jour des coordonnées bancaire du payeur : Payeur = {} , Facture = {}, Mode de paiement = {}", 
				new Object[]{invoice.getPartner().getName(),invoice.getInvoiceId(),paymentMode.getName()});
		
		Partner partner = invoice.getPartner();
		
		BankDetails bankDetails = bds.createBankDetails( //TODO
				this.getAccountNbr(payment[2]),
				"",
				this.getBankCode(payment[2]),
				payment[3],
				"",
				"",
				"",
				partner,
				this.getSortCode(payment[2]));
		
		partner.getBankDetailsList().add(bankDetails);
		
		partner.setPaymentMode(paymentMode);
		partner.save();
		
	}
	
	
	public Invoice getInvoice(String ref, Company company) throws AxelorException  {
		Invoice invoice = Invoice.all().filter("UPPER(self.invoiceId) = ?1", ref).fetchOne();
		if(invoice == null)  {
			throw new AxelorException(String.format("%s :\n La facture n°%s n'a pas été trouvée pour la société %s",
					GeneralService.getExceptionAccountingMsg(), ref, company.getName()), IException.INCONSISTENCY);
		}
		return invoice;
	}
	
	
	/**
	 * Fonction vérifiant si les coordonnées bancaire du payeur doivent être mise à jour
	 * @param val
	 * @return
	 * 			Les coordonnées bancaire du payeur doivent-elles être mise à jour ?
	 */
	public boolean bankDetailsMustBeUpdate(String val)  {
		LOG.debug("Doit-on mettre à jour les coordonnées bancaires du payeur ? {}", !val.equals("1"));

		return  !val.equals("1");
	}
	
	
	/**
	 * Procédure permettant de vérifier les champs d'une société
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
		if(company.getInterbankPaymentOrderImportPathCFONB() == null || company.getInterbankPaymentOrderImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin d'import des paiements par TIP et TIP + chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getTempInterbankPaymentOrderImportPathCFONB() == null || company.getTempInterbankPaymentOrderImportPathCFONB().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un chemin d'import temporaire des paiements par TIP et TIP + chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	

	/**
	 * Méthode permettant de récupérer le code établissement
	 * @param bankDetails
	 * @return
	 */
	public String getBankCode(String bankDetails)  {
		if(bankDetails != null && bankDetails.length() > 5)  {
			return bankDetails.substring(0, 5);
		}
		else  {
			return "";
		}
	}
	
	
	/**
	 * Méthode permettant de récupérer le code guichet
	 * @param bankDetails
	 * @return
	 */
	public String getSortCode(String bankDetails)  {
		if(bankDetails != null && bankDetails.length() > 5)  {
			return bankDetails.substring(5, 10);
		}
		else  {
			return "";
		}
	}
	
	
	/**
	 * Methode permettant de récupérer le numéro de compte 
	 * @param bankDetails
	 * @return
	 */
	public String getAccountNbr(String bankDetails)  {
		if(bankDetails != null && bankDetails.length() > 5)  {
			return bankDetails.substring(10, 21);
		}
		else  {
			return "";
		}
	}
}
