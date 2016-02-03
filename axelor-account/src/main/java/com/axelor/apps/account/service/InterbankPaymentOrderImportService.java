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
package com.axelor.apps.account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.cfonb.CfonbImportService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherCreateService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InterbankPaymentOrderImportService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected PaymentVoucherCreateService paymentVoucherCreateService;
	protected CfonbImportService cfonbImportService;
	protected RejectImportService rejectImportService;
	protected BankDetailsService bankDetailsService;
	protected AccountConfigService accountConfigService;
	protected PartnerRepository partnerRepo;
	protected InvoiceRepository invoiceRepo;

	protected DateTime dateTime;

	@Inject
	public InterbankPaymentOrderImportService(GeneralService generalService, PaymentVoucherCreateService paymentVoucherCreateService, CfonbImportService cfonbImportService,
			RejectImportService rejectImportService, BankDetailsService bankDetailsService, AccountConfigService accountConfigService, PartnerRepository partnerRepo,
			InvoiceRepository invoiceRepo) {

		this.paymentVoucherCreateService = paymentVoucherCreateService;
		this.cfonbImportService = cfonbImportService;
		this.rejectImportService = rejectImportService;
		this.bankDetailsService = bankDetailsService;
		this.accountConfigService = accountConfigService;
		this.partnerRepo = partnerRepo;
		this.invoiceRepo = invoiceRepo;
		this.dateTime = generalService.getTodayDateTime();

	}

	public void runInterbankPaymentOrderImport(Company company) throws AxelorException, IOException  {

		this.testCompanyField(company);

		AccountConfig accountConfig = company.getAccountConfig();

		String dest = rejectImportService.getDestCFONBFile(accountConfig.getInterbankPaymentOrderImportPathCFONB(), accountConfig.getTempInterbankPaymentOrderImportPathCFONB());

		// Récupération des enregistrements
		List<String[]> file = cfonbImportService.importCFONB(dest, company, 3, 4);
		for(String[] payment : file)  {

			this.runInterbankPaymentOrder(payment, company);
		}
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentVoucher runInterbankPaymentOrder(String[] payment, Company company) throws AxelorException  {
		Invoice invoice = this.getInvoice(payment[1], company);

		PaymentMode paymentMode = cfonbImportService.getPaymentMode(invoice.getCompany(), payment[0]);
		log.debug("Mode de paiement récupéré depuis l'enregistrement CFONB : {}", new Object[]{paymentMode.getName()});

		BigDecimal amount = new BigDecimal(payment[5]);

		if(this.bankDetailsMustBeUpdate(payment[4]))  {
			this.updateBankDetails(payment, invoice, paymentMode);
		}

		return paymentVoucherCreateService.createPaymentVoucherIPO(invoice, this.dateTime, amount, paymentMode);
	}


	public void updateBankDetails(String[] payment, Invoice invoice, PaymentMode paymentMode)  {
		log.debug("Mise à jour des coordonnées bancaire du payeur : Payeur = {} , Facture = {}, Mode de paiement = {}",
				new Object[]{invoice.getPartner().getName(),invoice.getInvoiceId(),paymentMode.getName()});

		Partner partner = invoice.getPartner();

		BankDetails bankDetails = bankDetailsService.createBankDetails( //TODO
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
		partnerRepo.save(partner);

	}


	public Invoice getInvoice(String ref, Company company) throws AxelorException  {
		Invoice invoice = invoiceRepo.all().filter("UPPER(self.invoiceId) = ?1", ref).fetchOne();
		if(invoice == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INTER_BANK_PO_IMPORT_1),
					GeneralServiceImpl.EXCEPTION, ref, company.getName()), IException.INCONSISTENCY);
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
		log.debug("Doit-on mettre à jour les coordonnées bancaires du payeur ? {}", !val.equals("1"));

		return  !val.equals("1");
	}


	/**
	 * Procédure permettant de vérifier les configurations comptables
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		accountConfigService.getInterbankPaymentOrderImportPathCFONB(accountConfig);
		accountConfigService.getTempInterbankPaymentOrderImportPathCFONB(accountConfig);

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
