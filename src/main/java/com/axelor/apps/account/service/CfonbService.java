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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.DirectDebitManagement;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.StringTool;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class CfonbService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CfonbService.class);
	
	private List<String> importFile;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	/****************************************  Export CFONB  *****************************************************/
	
	
	/**
	 * Méthode permettant d'exporter les remboursements au format CFONB
	 * @param reimbursementExport
	 * @param dateTime
	 * @param reimbursementList
	 * @throws AxelorException
	 */
	public void exportCFONB(Company company, DateTime dateTime, List<Reimbursement> reimbursementList, BankDetails bankDetails) throws AxelorException  {
		
		this.testCompanyExportCFONBField(company);
		
		// paramètre obligatoire : au minimum 
		//		un enregistrement emetteur par date de règlement (code 03)
		// 		un enregistrement destinataire (code 06)
		// 		un enregistrement total (code 08)
		
		String senderCFONB = this.createSenderReimbursementCFONB(company, dateTime, bankDetails);
		List<String> multiRecipientCFONB = new ArrayList<String>();
		for(Reimbursement reimbursement : reimbursementList)  {
			reimbursement = Reimbursement.find(reimbursement.getId());

			multiRecipientCFONB.add(this.createRecipientCFONB(company, reimbursement));
		}
		String totalCFONB = this.createReimbursementTotalCFONB(company,this.getTotalAmountReimbursementExport(reimbursementList));
		
		this.testLength(senderCFONB, totalCFONB, multiRecipientCFONB, company);
		
		List<String> cFONB = this.createCFONBExport(senderCFONB, multiRecipientCFONB, totalCFONB);
		
		// Mise en majuscule des enregistrement
		cFONB = this.toUpperCase(cFONB);
		
		this.createCFONBFile(cFONB, dateTime, company.getAccountConfig().getReimbursementExportFolderPathCFONB(), "virement");
	}
	
	/**
	 * Méthode permettant d'exporter les prélèvements d'échéance de mensu au format CFONB
	 * @param paymentScheduleExport
	 * @param paymentScheduleLineList
	 * @param company
	 * @throws AxelorException
	 */
	public void exportPaymentScheduleCFONB(DateTime processingDateTime, LocalDate scheduleDate, List<PaymentScheduleLine> paymentScheduleLineList, Company company, BankDetails bankDetails) throws AxelorException  {
		this.testCompanyExportCFONBField(company);
		
		// paramètre obligatoire : au minimum 
		//		un enregistrement emetteur par date de règlement (code 03)
		// 		un enregistrement destinataire (code 06)
		// 		un enregistrement total (code 08)
		
		String senderCFONB = this.createSenderMonthlyExportCFONB(company, scheduleDate, bankDetails);
		List<String> multiRecipientCFONB = new ArrayList<String>();
		
		List<DirectDebitManagement> directDebitManagementList = new ArrayList<DirectDebitManagement>();
		for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
			paymentScheduleLine = PaymentScheduleLine.find(paymentScheduleLine.getId());
			if(paymentScheduleLine.getDirectDebitManagement() == null)  {
				multiRecipientCFONB.add(this.createRecipientCFONB(company, paymentScheduleLine, true));
			}
			else  {
				if(!directDebitManagementList.contains(paymentScheduleLine.getDirectDebitManagement()))  {
					directDebitManagementList.add(paymentScheduleLine.getDirectDebitManagement());
				}
			}
		}
		
		for(DirectDebitManagement directDebitManagement : directDebitManagementList)  {
			multiRecipientCFONB.add(this.createRecipientCFONB(company, directDebitManagement, true, false));
		}
		
		String totalCFONB = this.createPaymentScheduleTotalCFONB(company,this.getTotalAmountPaymentSchedule(paymentScheduleLineList, true));
		
		this.testLength(senderCFONB, totalCFONB, multiRecipientCFONB, company);
		
		List<String> cFONB = this.createCFONBExport(senderCFONB, multiRecipientCFONB, totalCFONB);
		
		// Mise en majuscule des enregistrement
		cFONB = this.toUpperCase(cFONB);
		
		this.createCFONBFile(cFONB, processingDateTime, company.getAccountConfig().getPaymentScheduleExportFolderPathCFONB(), "prelevement");
	}
	
	
	/**
	 * Méthode permettant d'exporter les prélèvements de facture au format CFONB
	 * @param paymentScheduleExport
	 * @param paymentScheduleLineList
	 * @param invoiceList
	 * @param company
	 * @throws AxelorException
	 */
	public void exportInvoiceCFONB(DateTime processingDateTime, LocalDate scheduleDate, List<Invoice> invoiceList, Company company, BankDetails bankDetails) throws AxelorException  {
		this.testCompanyExportCFONBField(company);
		
		// paramètre obligatoire : au minimum 
		//		un enregistrement emetteur par date de règlement (code 03)
		// 		un enregistrement destinataire (code 06)
		// 		un enregistrement total (code 08)
		
		String senderCFONB = this.createSenderMonthlyExportCFONB(company, scheduleDate, bankDetails);
		List<String> multiRecipientCFONB = new ArrayList<String>();
		
		List<DirectDebitManagement> directDebitManagementList = new ArrayList<DirectDebitManagement>();
		
		for(Invoice invoice : invoiceList)  {
			invoice = Invoice.find(invoice.getId());
			if(invoice.getDirectDebitManagement() == null)  {
				multiRecipientCFONB.add(this.createRecipientCFONB(company, invoice));
			}
			else  {
				if(!directDebitManagementList.contains(invoice.getDirectDebitManagement()))  {
					directDebitManagementList.add(invoice.getDirectDebitManagement());
				}
			}
		}
		
		for(DirectDebitManagement directDebitManagement : directDebitManagementList)  {
			multiRecipientCFONB.add(this.createRecipientCFONB(company, directDebitManagement, true, true));
		}

		
		BigDecimal amount = this.getTotalAmountInvoice(invoiceList);
		
		String totalCFONB = this.createPaymentScheduleTotalCFONB(company,amount);
		
		this.testLength(senderCFONB, totalCFONB, multiRecipientCFONB, company);
		
		List<String> cFONB = this.createCFONBExport(senderCFONB, multiRecipientCFONB, totalCFONB);
		
		// Mise en majuscule des enregistrement
		cFONB = this.toUpperCase(cFONB);
		
		this.createCFONBFile(cFONB, processingDateTime, company.getAccountConfig().getPaymentScheduleExportFolderPathCFONB(), "prelevement");
	}
	
	
	
	/**
	 * Méthode permettant d'exporter les prélèvements de facture et d'échéance de paiement au format CFONB
	 * @param paymentScheduleExport
	 * @param paymentScheduleLineList
	 * @param invoiceList
	 * @param company
	 * @throws AxelorException
	 */
	public void exportCFONB(DateTime processingDateTime, LocalDate scheduleDate, List<PaymentScheduleLine> paymentScheduleLineList, List<Invoice> invoiceList, Company company, BankDetails bankDetails) throws AxelorException  {
		this.testCompanyExportCFONBField(company);
		
		// paramètre obligatoire : au minimum 
		//		un enregistrement emetteur par date de règlement (code 03)
		// 		un enregistrement destinataire (code 06)
		// 		un enregistrement total (code 08)
		
		String senderCFONB = this.createSenderMonthlyExportCFONB(company, scheduleDate, bankDetails);
		List<String> multiRecipientCFONB = new ArrayList<String>();
		
		// Echéanciers
		List<DirectDebitManagement> directDebitManagementList = new ArrayList<DirectDebitManagement>();
		for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
			paymentScheduleLine = PaymentScheduleLine.find(paymentScheduleLine.getId());
			if(paymentScheduleLine.getDirectDebitManagement() == null)  {
				multiRecipientCFONB.add(this.createRecipientCFONB(company, paymentScheduleLine, false));
			}
			else  {
				if(!directDebitManagementList.contains(paymentScheduleLine.getDirectDebitManagement()))  {
					directDebitManagementList.add(paymentScheduleLine.getDirectDebitManagement());
				}
			}
		}
		
		for(DirectDebitManagement directDebitManagement : directDebitManagementList)  {
			multiRecipientCFONB.add(this.createRecipientCFONB(company, directDebitManagement, false, false));
		}
		
		
		// Factures
		directDebitManagementList = new ArrayList<DirectDebitManagement>();
		for(Invoice invoice : invoiceList)  {
			invoice = Invoice.find(invoice.getId());
			if(invoice.getDirectDebitManagement() == null)  {
				multiRecipientCFONB.add(this.createRecipientCFONB(company, invoice));
			}
			else  {
				if(!directDebitManagementList.contains(invoice.getDirectDebitManagement()))  {
					directDebitManagementList.add(invoice.getDirectDebitManagement());
				}
			}
		}
		
		for(DirectDebitManagement directDebitManagement : directDebitManagementList)  {
			multiRecipientCFONB.add(this.createRecipientCFONB(company, directDebitManagement, true, true));
		}

		
		BigDecimal amount = this.getTotalAmountPaymentSchedule(paymentScheduleLineList,false).add(this.getTotalAmountInvoice(invoiceList));
		
		String totalCFONB = this.createPaymentScheduleTotalCFONB(company,amount);
		
		this.testLength(senderCFONB, totalCFONB, multiRecipientCFONB, company);
		
		List<String> cFONB = this.createCFONBExport(senderCFONB, multiRecipientCFONB, totalCFONB);
		
		// Mise en majuscule des enregistrement
		cFONB = this.toUpperCase(cFONB);
		
		this.createCFONBFile(cFONB, processingDateTime, company.getAccountConfig().getPaymentScheduleExportFolderPathCFONB(), "prelevement");
	}
	
	
	
	/**
	 * Méthode permettant de mettre en majuscule et sans accent un CFONB
	 * @param cFONB
	 * @return
	 * 		Le CFONB nettoyé
	 */
	public List<String> toUpperCase(List<String> cFONB)  {
		List<String> upperCase = new ArrayList<String>();
		for(String s : cFONB)  {
			upperCase.add(StringTool.deleteAccent(s.toUpperCase()));
		}
		return upperCase;
	}

	
	/**
	 * Méthode permettant de récupérer la facture depuis une ligne d'écriture de facture ou une ligne d'écriture de rejet de facture
	 * @param moveLine
	 * 			Une ligne d'écriture de facture ou une ligne d'écriture de rejet de facture
	 * @return
	 * 			La facture trouvée
	 */
	public Invoice getInvoice(MoveLine moveLine)  {
		Invoice invoice = null;
		if(moveLine.getMove().getRejectOk())  {
			invoice = moveLine.getInvoiceReject();
		}
		else  {
			invoice = moveLine.getInvoice();
		}
		return invoice;
	}
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'émetteur' pour un virement des remboursements
	 * @param company
	 * 				Une société
	 * @param dateTime
	 * 				Une heure
	 * @return
	 * 				Un enregistrement 'emetteur'
	 * @throws AxelorException
	 */
	public String createSenderReimbursementCFONB(Company company, DateTime dateTime, BankDetails bankDetails) throws AxelorException  {
		
		DateFormat ddmmFormat = new SimpleDateFormat("ddMM"); 
		String date = ddmmFormat.format(dateTime.toDate());
		date += String.format("%s", StringTool.truncLeft(String.format("%s",dateTime.getYear()), 1));
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		// Récupération des valeurs
		String a = accountConfig.getSenderRecordCodeExportCFONB();  		// Code enregistrement
		String b1 = accountConfig.getTransferOperationCodeExportCFONB();	// Code opération
		String b2 = "";												// Zone réservée
		String b3 = accountConfig.getSenderNumExportCFONB();				// Numéro d'émetteur
		String c1One = "";											// Code CCD
		String c1Two = "";											// Zone réservée	
		String c1Three = date;										// Date d'échéance
		String c2 = accountConfig.getSenderNameCodeExportCFONB();			// Nom/Raison sociale du donneur d'ordre
		String d1One = "";											// Référence de la remise
		String d1Two = "";											// Zone réservée
		String d2One = "";											// Zone réservée
		String d2Two = "E";											// Code monnaie
		String d2Three = "";										// Zone réservée
		String d3 = bankDetails.getSortCode();  					// Code guichet de la banque du donneur d'ordre
		String d4 = bankDetails.getAccountNbr();  					// Numéro de compte du donneur d’ordre
		String e = "";												// Identifiant du donneur d'ordre
		String f = ""; 												// Zone réservée
		String g1 = bankDetails.getBankCode();						// Code établissement de la banque du donneur d'ordre
		String g2 = "";												// Zone réservée
		
		// Tronquage / remplissage à droite (chaine de caractère)
		b2 = StringTool.fillStringRight(b2, ' ', 8);
		b3 = StringTool.fillStringRight(b3, ' ', 6);
		c1One = StringTool.fillStringRight(c1One, ' ', 1);
		c1Two = StringTool.fillStringRight(c1Two, ' ', 6);
		c2 = StringTool.fillStringRight(c2, ' ', 24);
		d1One = StringTool.fillStringRight(d1One, ' ', 7);
		d1Two = StringTool.fillStringRight(d1Two, ' ', 17);
		d2One = StringTool.fillStringRight(d2One, ' ', 2);
		d2Three = StringTool.fillStringRight(d2Three, ' ', 5);
		d4 = StringTool.fillStringRight(d4, ' ', 11);
		e = StringTool.fillStringRight(e, ' ', 16);
		f = StringTool.fillStringRight(f, ' ', 31);
		g2 = StringTool.fillStringRight(g2, ' ', 6);
		
		// Tronquage / remplissage à gauche (nombre)
		a = StringTool.fillStringLeft(a, '0', 2);
		b1 = StringTool.fillStringLeft(b1, '0', 2);
		c1Three = StringTool.fillStringLeft(c1Three,'0', 5);
		d3 = StringTool.fillStringLeft(d3, '0', 5);
		g1 = StringTool.fillStringLeft(g1, '0', 5);

		// Vérification AN / N / A
		this.testDigital(a, company, 0);
		this.testDigital(b1, company, 0);
		this.testDigital(d3, company, 0);
		this.testDigital(g1, company, 0);
		
		// création de l'enregistrement
		return a+b1+b2+b3+c1One+c1Two+c1Three+c2+d1One+d1Two+d2One+d2Two+d2Three+d3+d4+e+f+g1+g2;
	}
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'émetteur' pour un export de prélèvement de mensu
	 * @param company
	 * 				Une société
	 * @param localDate
	 * 				Une date
	 * @return
	 * 				Un enregistrement 'emetteur'
	 * @throws AxelorException
	 */
	public String createSenderMonthlyExportCFONB(Company company, LocalDate localDate, BankDetails bankDetails) throws AxelorException  {
		
		DateFormat ddmmFormat = new SimpleDateFormat("ddMM"); 
		String date = ddmmFormat.format(localDate.toDateTimeAtCurrentTime().toDate());
		date += String.format("%s", StringTool.truncLeft(String.format("%s",localDate.getYear()), 1));
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		// Récupération des valeurs
		String a = accountConfig.getSenderRecordCodeExportCFONB();  			// Code enregistrement
		String b1 = accountConfig.getDirectDebitOperationCodeExportCFONB();	// Code opération
		String b2 = "";													// Zone réservée
		String b3 = accountConfig.getSenderNumExportCFONB();					// Numéro d'émetteur
		String c1One = "";												// Zone réservée
		String c1Two = date;											// Date d'échéance	
		String c2 = accountConfig.getSenderNameCodeExportCFONB();				// Nom/Raison sociale du donneur d'ordre
		String d1One = "";												// Référence de la remise
		String d1Two = "";												// Zone réservée
		String d2 = "";													// Zone réservée
		String d3 = bankDetails.getSortCode();  						// Code guichet de la banque du donneur d'ordre
		String d4 = bankDetails.getAccountNbr();  						// Numéro de compte du donneur d’ordre
		String e = "";													// Zone réservée
		String f = ""; 													// Zone réservée
		String g1 = bankDetails.getBankCode();							// Code établissement de la banque du donneur d'ordre
		String g2 = "";													// Zone réservée
		
		// Tronquage / remplissage à droite (chaine de caractère)
		b2 = StringTool.fillStringRight(b2, ' ', 8);
		b3 = StringTool.fillStringRight(b3, ' ', 6);
		c1One = StringTool.fillStringRight(c1One, ' ', 7);
		c2 = StringTool.fillStringRight(c2, ' ', 24);
		d1One = StringTool.fillStringRight(d1One, ' ', 7);
		d1Two = StringTool.fillStringRight(d1Two, ' ', 17);
		d2 = StringTool.fillStringRight(d2, ' ', 8);
		d4 = StringTool.fillStringRight(d4, ' ', 11);
		e = StringTool.fillStringRight(e, ' ', 16);
		f = StringTool.fillStringRight(f, ' ', 31);
		g2 = StringTool.fillStringRight(g2, ' ', 6);
		
		// Tronquage / remplissage à gauche (nombre)
		a = StringTool.fillStringLeft(a, '0', 2);
		b1 = StringTool.fillStringLeft(b1, '0', 2);
		c1Two = StringTool.fillStringLeft(c1Two, '0', 5);
		d3 = StringTool.fillStringLeft(d3, '0', 5);
		g1 = StringTool.fillStringLeft(g1, '0', 5);

		// Vérification AN / N / A
		this.testDigital(a, company, 0);
		this.testDigital(b1, company, 0);
		this.testDigital(d3, company, 0);
		this.testDigital(g1, company, 0);
		
		// création de l'enregistrement
		return a+b1+b2+b3+c1One+c1Two+c2+d1One+d1Two+d2+d3+d4+e+f+g1+g2;
	}
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'destinataire' pour un virement de remboursement
	 * @param company
	 * 				Une société
	 * @param reimbursement
	 * 				Un remboursement
	 * @return
	 * 			Un enregistrement 'destinataire'
	 * @throws AxelorException
	 */
	public String createRecipientCFONB(Company company, Reimbursement reimbursement) throws AxelorException  {
		BankDetails bankDetails = reimbursement.getBankDetails();
 
		if(bankDetails == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le remboursement %s",
					GeneralService.getExceptionAccountingMsg(),reimbursement.getRef()), IException.CONFIGURATION_ERROR);
		}
		
		BigDecimal amount = reimbursement.getAmountReimbursed();
		
		String ref = reimbursement.getRef();									// Référence
		String partner = this.getPayeurPartnerName(reimbursement.getPartner());	// Nom/Raison sociale du bénéficiaire
		String operationCode = company.getAccountConfig().getTransferOperationCodeExportCFONB();	// Code opération

		return this.createRecipientCFONB(company, amount, ref, partner, bankDetails, operationCode);
	}
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'destinataire' pour un export de prélèvement d'une échéance
	 * @param company
	 * 				Une société
	 * @param paymentScheduleLine
	 * 				Une échéance
	 * @return
	 * 				Un enregistrement 'destinataire'
	 * @throws AxelorException
	 */
	public String createRecipientCFONB(Company company, PaymentScheduleLine paymentScheduleLine, boolean mensu) throws AxelorException  {
		Partner partner = paymentScheduleLine.getPaymentSchedule().getPartner();
		BankDetails bankDetails = paymentScheduleLine.getPaymentSchedule().getBankDetails();
		if(bankDetails == null)  {
			bankDetails = partner.getBankDetails();
		}
		
		if(bankDetails == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le tiers %s",
					GeneralService.getExceptionAccountingMsg(),partner.getName()), IException.CONFIGURATION_ERROR);
		}
		
		BigDecimal amount = null;
 
		// prise en compte le montant restant a payer si ech paiement
		amount = this.getAmountRemainingFromPaymentMove(paymentScheduleLine);
			
		String ref = paymentScheduleLine.getDebitNumber();							// Référence
		String partnerName = this.getPayeurPartnerName(partner);					// Nom/Raison sociale du débiteur
		String operationCode = company.getAccountConfig().getDirectDebitOperationCodeExportCFONB();	// Code opération

		return this.createRecipientCFONB(company, amount, ref, partnerName, bankDetails, operationCode);
	}
	
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'destinataire' pour un export de prélèvement de plusieurs échéances par le biais d'un objet de gestion de prélèvement
	 * @param company
	 * 				Une société
	 * @param paymentScheduleLine
	 * 				Une échéance
	 * @return
	 * 				Un enregistrement 'destinataire'
	 * @throws AxelorException
	 */
	public String createRecipientCFONB(Company company, DirectDebitManagement directDebitManagement, boolean mensu, boolean isForInvoice) throws AxelorException  {
		BankDetails bankDetails = null;
		String partnerName = "";
		if(isForInvoice)  {
			Invoice invoice = (Invoice) directDebitManagement.getInvoiceSet().toArray()[0];
			Partner partner = invoice.getPartner();
			bankDetails = partner.getBankDetails();
			partnerName = this.getPayeurPartnerName(invoice.getPartner());			// Nom/Raison sociale du débiteur
			if(bankDetails == null) {
				throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le tiers %s",
						GeneralService.getExceptionAccountingMsg(), partner.getName()), IException.CONFIGURATION_ERROR);
			}
		}
		else  {
			PaymentSchedule paymentSchedule = directDebitManagement.getPaymentScheduleLineList().get(0).getPaymentSchedule();
			Partner partner = paymentSchedule.getPartner();
			partnerName = this.getPayeurPartnerName(partner);						// Nom/Raison sociale du débiteur
			
			bankDetails = paymentSchedule.getBankDetails();
			if(bankDetails == null)  {
				bankDetails = partner.getBankDetails();
			}
			if(bankDetails == null) {
				throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le tiers %s",
						GeneralService.getExceptionAccountingMsg(), partner.getName()), IException.CONFIGURATION_ERROR);
			}
		}
		
		BigDecimal amount = this.getAmount(directDebitManagement, mensu, isForInvoice);
 
		String ref = directDebitManagement.getDebitNumber();						// Référence

		String operationCode = company.getAccountConfig().getDirectDebitOperationCodeExportCFONB();	// Code opération

		return this.createRecipientCFONB(company, amount, ref, partnerName, bankDetails, operationCode);
	}
	
	
	public BigDecimal getAmount(DirectDebitManagement directDebitManagement, boolean mensu, boolean isForInvoice)  {
		BigDecimal amount = BigDecimal.ZERO;
		
		if(isForInvoice)  {
			for(Invoice invoice : directDebitManagement.getInvoiceSet())  {
				amount = amount.add(this.getAmountRemainingFromPaymentMove(invoice));
			}
		}
		else  {
			for(PaymentScheduleLine paymentScheduleLine : directDebitManagement.getPaymentScheduleLineList())  {
				if(mensu)  {
					// prise en compte le montant restant a payer si ech paiement
					amount = amount.add(paymentScheduleLine.getInTaxAmount());  
				}
				else  {
					amount = amount.add(this.getAmountRemainingFromPaymentMove(paymentScheduleLine));
					
				}
			}
		}
		
		return amount;
	}
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'destinataire' pour un export de prélèvement d'une facture
	 * @param company
	 * 				Une société
	 * @param moveLine
	 * 				L' écriture d'export des prélèvement d'une facture
	 * @return
	 * 				Un enregistrement 'destinataire'
	 * @throws AxelorException
	 */
	public String createRecipientCFONB(Company company, Invoice invoice) throws AxelorException  {
		Partner partner = invoice.getPartner();
		BankDetails bankDetails = partner.getBankDetails();
		if(bankDetails == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le tiers %s",
					GeneralService.getExceptionAccountingMsg(),partner.getName()), IException.CONFIGURATION_ERROR);
		}
	
		BigDecimal amount = this.getTotalAmountInvoice(invoice);
		
		String ref = invoice.getDebitNumber();										// Référence
		String partnerName = this.getPayeurPartnerName(partner);					// Nom/Raison sociale du débiteur
		String operationCode = company.getAccountConfig().getDirectDebitOperationCodeExportCFONB();	// Code opération

		return this.createRecipientCFONB(company, amount, ref, partnerName, bankDetails, operationCode);
	}
	
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'destinataire'
	 * @param company
	 * 				Une société
	 * @param amount
	 * 				Le montant de l'enregistrement
	 * @param ref
	 * 				Une référence de prélèvement
	 * @param label
	 * 				Un libellé
	 * @param partner
	 * 				Un tiers payeur
	 * @param bankDetails
	 * 				Un RIB
	 * @param operationCode
	 * 		Le code d'opération défini par société
	 * @return
	 * 				L'enregistrement 'destinataire' 
	 * @throws AxelorException
	 */
	public String createRecipientCFONB(Company company, BigDecimal amount, String ref, String partner, BankDetails bankDetails, String operationCode) throws AxelorException  {
		this.testBankDetailsField(bankDetails);
		
		String amountFixed = amount.setScale(2).toString().replace(".","");

		AccountConfig accountConfig = company.getAccountConfig();
		
		// Récupération des valeurs
		String a = accountConfig.getRecipientRecordCodeExportCFONB(); // Code enregistrement
		String b1 = operationCode;								// Code opération
		String b2 = "";											// Zone réservée
		String b3 = accountConfig.getSenderNumExportCFONB();			// Numéro d'émetteur
		String c1 = ref;										// Référence
		String c2 = partner;									// Nom/Raison sociale du bénéficiaire
		String d1 = bankDetails.getBankAddress();				// Domiciliation
		String d2 = "";											// Déclaration de la balance des paiement
		String d3 = bankDetails.getSortCode();  				// Code guichet de la banque du donneur d'ordre / du débiteur
		String d4 = bankDetails.getAccountNbr();				// Numéro de compte du bénéficiaire / du débiteur
		String e = amountFixed;									// Montant du virement
		String f = ref;											// Libellé
		String g1 = bankDetails.getBankCode();					// Code établissement de la banque du donneur d'ordre / du débiteur
		String g2 = "";											// Zone réservée

		// Tronquage / remplissage à droite (chaine de caractère)
		b2 = StringTool.fillStringRight(b2, ' ', 8);
		b3 = StringTool.fillStringRight(b3, ' ', 6);
		c1 = StringTool.fillStringRight(c1, ' ', 12);
		c2 = StringTool.fillStringRight(c2, ' ', 24);
		d1 = StringTool.fillStringRight(d1, ' ', 24);
		d2 = StringTool.fillStringRight(d2, ' ', 8);
		d4 = StringTool.fillStringRight(d4, ' ', 11);
		f = StringTool.fillStringRight(f, ' ', 31);
		g2 = StringTool.fillStringRight(g2, ' ', 6);
		
		// Tronquage / remplissage à gauche (nombre)
		a = StringTool.fillStringLeft(a, '0', 2);
		b1 = StringTool.fillStringLeft(b1, '0', 2);
		d3 = StringTool.fillStringLeft(d3, '0', 5);
		e = StringTool.fillStringLeft(e, '0', 16);
		g1 = StringTool.fillStringLeft(g1, '0', 5);
		
		return a+b1+b2+b3+c1+c2+d1+d2+d3+d4+e+f+g1+g2;
	}
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'total' au format CFONB pour un remboursement
	 * @param company
	 * 				Une société
	 * @param amount
	 * 				Le montant total des enregistrements 'destinataire'
	 * @return
	 */
	public String createReimbursementTotalCFONB(Company company, BigDecimal amount)  {
		
		// Code opération
		String operationCode = company.getAccountConfig().getTransferOperationCodeExportCFONB();	
		
		return this.createTotalCFONB(company, amount, operationCode);
	}	
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'total' au format CFONB pour un échéancier
	 * @param company
	 * 				Une société
	 * @param amount
	 * 				Le montant total des enregistrements 'destinataire'
	 * @return
	 * 				L'enregistrement 'total'
	 */
	public String createPaymentScheduleTotalCFONB(Company company, BigDecimal amount)  {
		
		// Code opération
		String operationCode = company.getAccountConfig().getDirectDebitOperationCodeExportCFONB();	
		
		return this.createTotalCFONB(company, amount, operationCode);
	}
	
	
	/**
	 * Fonction permettant de créer un enregistrement 'total' au format CFONB
	 * @param company
	 * 				Une société
	 * @param amount
	 * 				Le montant total des enregistrements 'destinataire'
	 * @param operationCode
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP</li>
     *  	</ul>
	 * @return
	 * 				L'enregistrement 'total'
	 */
	public String createTotalCFONB(Company company, BigDecimal amount, String operationCode)  {
		String totalAmount = amount.setScale(2).toString().replace(".","");
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		// Récupération des valeurs
		String a = accountConfig.getTotalRecordCodeExportCFONB();  	// Code enregistrement
		String b1 = operationCode;								// Code opération
		String b2 = "";											// Zone réservée
		String b3 = accountConfig.getSenderNumExportCFONB();			// Numéro d'émetteur
		String c1 = "";											// Zone réservée
		String c2 = "";											// Zone réservée
		String d1 = "";											// Zone réservée
		String d2 = "";											// Zone réservée
		String d3 = "";											// Zone réservée
		String d4 = "";											// Zone réservée
		String e = totalAmount;									// Montant de la remise
		String f = "";											// Zone réservée
		String g1 = "";											// Zone réservée
		String g2 = "";											// Zone réservée
		
		// Tronquage / remplissage à droite (chaine de caractère)
		b2 = StringTool.fillStringRight(b2, ' ', 8);
		b3 = StringTool.fillStringRight(b3, ' ', 6);
		c1 = StringTool.fillStringRight(c1, ' ', 12);
		c2 = StringTool.fillStringRight(c2, ' ', 24);
		d1 = StringTool.fillStringRight(d1, ' ', 24);
		d2 = StringTool.fillStringRight(d2, ' ', 8);
		d3 = StringTool.fillStringRight(d3, ' ', 5);
		d4 = StringTool.fillStringRight(d4, ' ', 11);
		f = StringTool.fillStringRight(f, ' ', 31);
		g1 = StringTool.fillStringRight(g1, ' ', 5);
		g2 = StringTool.fillStringRight(g2, ' ', 6);
		
		// Tronquage / remplissage à gauche (nombre)
		a = StringTool.fillStringLeft(a, '0', 2);
		b1 = StringTool.fillStringLeft(b1, '0', 2);
		e = StringTool.fillStringLeft(e, '0', 16);
		
		return a+b1+b2+b3+c1+c2+d1+d2+d3+d4+e+f+g1+g2;
	}
	
	
	/**
	 * Fonction permettant de créer le CFONB
	 * @param senderCFONB
	 * 				Un enregistrement 'émetteur'
	 * @param recipientCFONB
	 * 				Un liste d'enregistrement 'destinataire'
	 * @param totalCFONB
	 * 				Un enregistrement 'total'
	 * @return
	 * 				Le CFONB
	 */
	public List<String> createCFONBExport(String senderCFONB, List<String> recipientCFONB, String totalCFONB)  {
		// checker meme compte emetteur
		// checker meme type de virement
		// checker meme date de règlement
		
		List<String> cFONB = new ArrayList<String>();
		cFONB.add(senderCFONB);
		cFONB.addAll(recipientCFONB);
		cFONB.add(totalCFONB);
		return cFONB;
	}
	
	
	/**
	 * Procédure permettant de créer un fichier CFONB au format .dat
	 * @param cFONB
	 * 			Le contenu du fichier, des enregistrements CFONB
	 * @param dateTime
	 * 			La date permettant de déterminer le nom du fichier créé
	 * @param destinationFolder
	 * 			Le répertoire de destination
	 * @param prefix
	 * 			Le préfix utilisé
	 * @throws AxelorException
	 */
	public void createCFONBFile(List<String> cFONB, DateTime dateTime, String destinationFolder, String prefix) throws AxelorException  {
		DateFormat yyyyMMddHHmmssFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateFileName = yyyyMMddHHmmssFormat.format(dateTime.toDate());
		String fileName = String.format("%s%s.dat", prefix, dateFileName);
		
		try {
			FileTool.writer(destinationFolder, fileName, cFONB);
		} catch (IOException e) {
			throw new AxelorException(String.format("%s :\n Erreur detectée pendant l'ecriture du fichier CFONB : %s",
					GeneralService.getExceptionAccountingMsg(),e), IException.CONFIGURATION_ERROR);
		}
		
		
	}
	
	
	/**
	 * Méthode permettant de construire le Nom/Raison sociale du tiers payeur d'un mémoire
	 * @param memory
	 * 				Un mémoire
	 * @return
	 * 				Civilité + Nom + Prénom 	si c'est une personne physique
	 * 				Civilité + Nom 				sinon
	 */
	public String getPayeurPartnerName(Partner partner)  {
		
		if(partner.getTitleSelect() != null )  {
			return String.format("%s %s", partner.getTitleSelect(), partner.getName());
		}
		else {
			return String.format("%s", partner.getName());
		}
			
	}
		
	
	/**
	 * Méthode permettant de calculer le montant total des remboursements
	 * @param reimbursementList
	 * 				Une liste de remboursement
	 * @return
	 * 				Le montant total
	 */
	public BigDecimal getTotalAmountReimbursementExport(List<Reimbursement> reimbursementList)  {
		BigDecimal totalAmount = BigDecimal.ZERO;
		for(Reimbursement reimbursement : reimbursementList)  {
			reimbursement = Reimbursement.find(reimbursement.getId());
			totalAmount = totalAmount.add(reimbursement.getAmountReimbursed());
		}
		return totalAmount;
	}
	
	
	/**
	 * Fonction permettant de récupérer le montant total à prélever d'une liste d'échéance de mensu
	 * @param paymentScheduleLineList
	 * 			Une liste d'échéance de mensu
	 * @return
	 * 			Le montant total à prélever
	 */
	public BigDecimal getTotalAmountPaymentSchedule(List<PaymentScheduleLine> paymentScheduleLineList, boolean mensu)  {
		BigDecimal totalAmount = BigDecimal.ZERO;
		for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
			paymentScheduleLine = PaymentScheduleLine.find(paymentScheduleLine.getId());
			if(mensu)  {
				totalAmount = totalAmount.add(paymentScheduleLine.getInTaxAmount());
			}
			else  {
				totalAmount = totalAmount.add(this.getAmountRemainingFromPaymentMove(paymentScheduleLine));
			}
		}
		return totalAmount;
	}
	
	
	/**
	 * Fonction permettant de récupérer le montant total à prélever d'une liste d'échéance de mensu
	 * @param paymentScheduleLineList
	 * 			Une liste d'échéance de mensu
	 * @return
	 * 			Le montant total à prélever
	 */
	public BigDecimal getTotalAmountInvoice(List<Invoice> invoiceList)  {
		BigDecimal totalAmount = BigDecimal.ZERO;
		for(Invoice invoice : invoiceList)  {
			invoice = Invoice.find(invoice.getId());
			totalAmount = totalAmount.add(this.getTotalAmountInvoice(invoice));
		}
		return totalAmount;
	}
	
	
	/**
	 * Fonction permettant de récupérer le montant total à prélever d'une liste d'échéance de mensu
	 * @param paymentScheduleLineList
	 * 			Une liste d'échéance de mensu
	 * @return
	 * 			Le montant total à prélever
	 */
	public BigDecimal getTotalAmountInvoice(Invoice invoice)  {
		for(MoveLine moveLine : invoice.getMove().getMoveLineList())  {
			if(moveLine.getAmountExportedInDirectDebit().compareTo(BigDecimal.ZERO) > 0 && moveLine.getAccount().getReconcileOk())  {
				return moveLine.getAmountExportedInDirectDebit();
			}
		}
		return BigDecimal.ZERO;
	}
	
	
	
	/**
	 * Procédure permettant de vérifier que la chaine de caractère ne contient que des entier
	 * @param s
	 * 			La chaine de caractère à tester
	 * @param company
	 * 			Une société
	 * @param type
	 * 		Le type d'enregistrement :
	 * 		<ul>
     *      <li>0 = émetteur</li>
     *      <li>1 = destinataire</li>
     *      <li>2 = total</li>
     *  	</ul>
	 * @throws AxelorException
	 */
	public void testDigital(String s, Company company, int type) throws AxelorException  {
		if(!StringTool.isDigital(s))  {
			switch(type)  {
				case 0:
					throw new AxelorException(String.format("%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour l'émetteur, société %s",
							GeneralService.getExceptionAccountingMsg(),s,company.getName()), IException.CONFIGURATION_ERROR);
				case 1:
					throw new AxelorException(String.format("%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour le destinataire, société %s",
							GeneralService.getExceptionAccountingMsg(),s,company.getName()), IException.CONFIGURATION_ERROR);
				case 2:
					throw new AxelorException(String.format("%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour le total, société %s",
							GeneralService.getExceptionAccountingMsg(),s,company.getName()), IException.CONFIGURATION_ERROR);
				
				default:
					break;
			}	
		}
	}
	
	
	/**
	 * Procédure permettant de vérifier la longueur d'un CFONB
	 * @param senderCFONB
	 * 			Un enregistrement 'emetteur'
	 * @param totalCFONB
	 * 			Un enregistrement 'total'
	 * @param multiRecipientCFONB
	 * 			Une liste d'enregistrement 'destinataire'
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testLength(String senderCFONB, String totalCFONB, List<String> multiRecipientCFONB, Company company) throws AxelorException  {
		this.testLength(senderCFONB, company, 0, 160);
		this.testLength(totalCFONB, company, 2, 160);
		for(String s : multiRecipientCFONB)  {
			this.testLength(s, company, 1, 160);
		}
	}
	
	
	
	
	/**
	 * Procédure permettant de vérifier la longueur d'un enregistrement CFONB
	 * @param s
	 * 			Un enregistrement CFONB
	 * @param company
	 * 			Une société
	 * @param type
	 * 	 	Le type d'enregistrement :
	 * 		<ul>
     *      <li>0 = émetteur</li>
     *      <li>1 = destinataire</li>
     *      <li>2 = total</li>
     *      <li>3 = entête</li>
     *      <li>4 = détail</li>
     *      <li>5 = fin</li>
     *  	</ul>
	 * 
	 * @param size
	 * 			La longueur de l'enregistrement
	 * @throws AxelorException
	 */
	public void testLength(String s, Company company, int type, int size) throws AxelorException  {
		if(s.length() != size)  {
			String concerned = "";
			switch(type)  {
				case 0:
					concerned = "émetteur";
					break;
				case 1:
					concerned = "destinataire";
					break;
				case 2:
					concerned = "total";
					break;
				case 3:
					concerned = "entête";
					break;
				case 4:
					concerned = "détail";
					break;
				case 5:
					concerned = "fin";
					break;
				default:
					break;
			}	
			throw new AxelorException(String.format("%s :\n Annomlie détectée (l'enregistrement ne fait pas %s caractères : %s) pour l'enregistrement %s, société %s",
					GeneralService.getExceptionAccountingMsg(),size,s,concerned,company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/**
	 * Procédure permettant de vérifier la conformité des champs en rapport avec les exports CFONB d'une société
	 * @param company
	 * 			La société
	 * @throws AxelorException
	 */
	public void testCompanyExportCFONBField(Company company) throws AxelorException  {
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		accountConfigService.getSenderRecordCodeExportCFONB(accountConfig);
		accountConfigService.getSenderNumExportCFONB(accountConfig);
		accountConfigService.getSenderNameCodeExportCFONB(accountConfig);
		accountConfigService.getRecipientRecordCodeExportCFONB(accountConfig);
		accountConfigService.getTotalRecordCodeExportCFONB(accountConfig);
		accountConfigService.getTransferOperationCodeExportCFONB(accountConfig);
		accountConfigService.getDirectDebitOperationCodeExportCFONB(accountConfig);
		accountConfigService.getReimbursementExportFolderPathCFONB(accountConfig);
		accountConfigService.getPaymentScheduleExportFolderPathCFONB(accountConfig);
		
	}
	
	
	
	/**
	 * Procédure permettant de vérifier la conformité des champs d'un RIB
	 * @param bankDetails
	 * 			Le RIB
	 * @throws AxelorException
	 */
	public void testBankDetailsField(BankDetails bankDetails) throws AxelorException  {
		if(bankDetails.getSortCode() == null || bankDetails.getSortCode().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code Guichet pour le RIB %s du tiers payeur %s",
					GeneralService.getExceptionAccountingMsg(),bankDetails.getIban(), bankDetails.getPartner().getName()), IException.CONFIGURATION_ERROR);
		}
		if(bankDetails.getAccountNbr() == null || bankDetails.getAccountNbr().isEmpty()) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Numéro de compte pour le RIB %s du tiers payeur %s",
					GeneralService.getExceptionAccountingMsg(),bankDetails.getIban(), bankDetails.getPartner().getName()), IException.CONFIGURATION_ERROR);
		}
		if(bankDetails.getBankCode() == null || bankDetails.getBankCode().isEmpty()) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Code Banque pour le RIB %s du tiers payeur %s",
					GeneralService.getExceptionAccountingMsg(),bankDetails.getIban(), bankDetails.getPartner().getName()), IException.CONFIGURATION_ERROR);
		}
		if(bankDetails.getBankAddress() == null || bankDetails.getBankAddress().isEmpty()) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une Adresse de Banque pour le RIB %s du tiers payeur %s",
					GeneralService.getExceptionAccountingMsg(),bankDetails.getIban(), bankDetails.getPartner().getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/*********************************************  Import CFONB  ********************************************/
	
	
	/**
	 * 
	 * @param fileName
	 * @param company
	 * @param operation
	 * 	 	Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * @throws AxelorException
	 * @throws IOException
	 */
	public List<String[]> importCFONB(String fileName, Company company, int operation) throws AxelorException, IOException  {
		return this.importCFONB(fileName, company, operation, 999);
	}
	
	
	/**
	 * Récupération par lots
	 * @param fileName
	 * @param company
	 * @param operation
	 * 	 	Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * @throws AxelorException
	 * @throws IOException
	 */
	public Map<List<String[]>,String> importCFONBByLot(String fileName, Company company, int operation) throws AxelorException, IOException  {
		return this.importCFONBByLot(fileName, company, operation, 999);
	}
	
	
	
	/**
	 * 
	 * @param fileName
	 * @param company
	 * @param operation
	 * 	 	Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * @throws AxelorException
	 * @throws IOException
	 */
	public List<String[]> importCFONB(String fileName, Company company, int operation, int optionalOperation) throws AxelorException, IOException  {
		
		//		un enregistrement "en-tête" (code 31)
		// 		un enregistrement "détail" (code 34)
		// 		un enregistrement "fin" (code 39)
		
		this.importFile = FileTool.reader(fileName);
				
		this.testCompanyImportCFONBField(company);
		
		if(GeneralService.getGeneral().getTransferAndDirectDebitInterbankCode() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une Liste des codes motifs de rejet/retour relatifs aux Virements, Prélèvements et TIP dans l'administration générale",
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);	
		}
		
		String headerCFONB = null;
		List<String> multiDetailsCFONB = null;
		String endingCFONB = null;
		List<String[]> importDataList = new ArrayList<String[]>();

		
		// Pour chaque sequence, on récupère les enregistrements, et on les vérifie.
		// Ensuite on supprime les lignes traitées du fichier chargé en mémoire
		// Et on recommence l'opération jusqu'à ne plus avoir de ligne à traiter
		while(this.importFile != null && this.importFile.size() != 0)  {
			headerCFONB = this.getHeaderCFONB(this.importFile, company, operation, optionalOperation);
			if(headerCFONB == null)  {
				throw new AxelorException(String.format("%s :\n Il manque un enregistrement en-tête dans le fichier %s",
						GeneralService.getExceptionAccountingMsg(),fileName), IException.CONFIGURATION_ERROR);
			}
			this.importFile.remove(headerCFONB);
			
			multiDetailsCFONB = this.getDetailsCFONB(this.importFile, company, operation, optionalOperation);
			if(multiDetailsCFONB.isEmpty())  {
				throw new AxelorException(String.format("%s :\n Il manque un ou plusieurs enregistrements détail dans le fichier %s",
						GeneralService.getExceptionAccountingMsg(),fileName), IException.CONFIGURATION_ERROR);
			}
			for(String detail : multiDetailsCFONB)  {
				this.importFile.remove(detail);
			}
			
			endingCFONB = this.getEndingCFONB(this.importFile, company, operation, optionalOperation);
			if(endingCFONB == null)  {
				throw new AxelorException(String.format("%s :\n Il manque un enregistrement fin dans le fichier %s",
						GeneralService.getExceptionAccountingMsg(),fileName), IException.CONFIGURATION_ERROR);
			}
			this.importFile.remove(endingCFONB);
			
			this.testLength(headerCFONB, multiDetailsCFONB, endingCFONB, company);
			
			importDataList.addAll(this.getDetailDataAndCheckAmount(operation, headerCFONB, multiDetailsCFONB, endingCFONB, fileName));
		}
		return importDataList;
	}
	
	
	
	/**
	 * Récupération par lots
	 * @param fileName
	 * @param company
	 * @param operation
	 * 	 	Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * @throws AxelorException
	 * @throws IOException
	 */
	public Map<List<String[]>,String> importCFONBByLot(String fileName, Company company, int operation, int optionalOperation) throws AxelorException, IOException  {
		
		//		un enregistrement "en-tête" (code 31)
		// 		un enregistrement "détail" (code 34)
		// 		un enregistrement "fin" (code 39)
		
		this.importFile = FileTool.reader(fileName);
				
		this.testCompanyImportCFONBField(company);
		
		if(GeneralService.getGeneral().getTransferAndDirectDebitInterbankCode() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une Liste des codes motifs de rejet/retour relatifs aux Virements, Prélèvements et TIP dans l'administration générale",
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);	
		}
		
		String headerCFONB = null;
		List<String> multiDetailsCFONB = null;
		String endingCFONB = null;
		Map<List<String[]>,String> importDataList = new HashMap<List<String[]>,String>();

		
		// Pour chaque sequence, on récupère les enregistrements, et on les vérifie.
		// Ensuite on supprime les lignes traitées du fichier chargé en mémoire
		// Et on recommence l'opération jusqu'à ne plus avoir de ligne à traiter
		while(this.importFile != null && this.importFile.size() != 0)  {
			headerCFONB = this.getHeaderCFONB(this.importFile, company, operation, optionalOperation);
			if(headerCFONB == null)  {
				throw new AxelorException(String.format("%s :\n Il manque un enregistrement en-tête dans le fichier %s",
						GeneralService.getExceptionAccountingMsg(),fileName), IException.CONFIGURATION_ERROR);
			}
			this.importFile.remove(headerCFONB);
			
			multiDetailsCFONB = this.getDetailsCFONB(this.importFile, company, operation, optionalOperation);
			if(multiDetailsCFONB.isEmpty())  {
				throw new AxelorException(String.format("%s :\n Il manque un ou plusieurs enregistrements détail dans le fichier %s",
						GeneralService.getExceptionAccountingMsg(),fileName), IException.CONFIGURATION_ERROR);
			}
			for(String detail : multiDetailsCFONB)  {
				this.importFile.remove(detail);
			}
			
			endingCFONB = this.getEndingCFONB(this.importFile, company, operation, optionalOperation);
			if(endingCFONB == null)  {
				throw new AxelorException(String.format("%s :\n Il manque un enregistrement fin dans le fichier %s",
						GeneralService.getExceptionAccountingMsg(),fileName), IException.CONFIGURATION_ERROR);
			}
			this.importFile.remove(endingCFONB);
			
			this.testLength(headerCFONB, multiDetailsCFONB, endingCFONB, company);
			
			importDataList.put(this.getDetailDataAndCheckAmount(operation, headerCFONB, multiDetailsCFONB, endingCFONB, fileName),this.getHeaderDate(headerCFONB));
		}
		return importDataList;
	}
	
	
	public List<String[]> getDetailDataAndCheckAmount(int operation, String headerCFONB, List<String> multiDetailsCFONB, String endingCFONB, String fileName) throws AxelorException  {
		List<String[]> importDataList = new ArrayList<String[]>();
		switch(operation)  {
			case 0:
				for(String detailCFONB : multiDetailsCFONB)  {
					importDataList.add(this.getDetailData(detailCFONB, false));
				}
				this.checkTotalAmount(multiDetailsCFONB, endingCFONB, fileName, 228, 240);
				break;
			case 1:
				for(String detailCFONB : multiDetailsCFONB)  {
					importDataList.add(this.getDetailData(detailCFONB, false));
				}
				this.checkTotalAmount(multiDetailsCFONB, endingCFONB, fileName, 228, 240);
				break;
			case 2:
				for(String detailCFONB : multiDetailsCFONB)  {
					importDataList.add(this.getDetailData(detailCFONB, true));
				}
				this.checkTotalAmount(multiDetailsCFONB, endingCFONB, fileName, 228, 240);
				break;
			case 3:
				for(String detailCFONB : multiDetailsCFONB)  {
					importDataList.add(this.getDetailDataTIP(detailCFONB));
				}
				this.checkTotalAmount(multiDetailsCFONB, endingCFONB, fileName, 102, 118);
				break;
			case 4:
				for(String detailCFONB : multiDetailsCFONB)  {
					importDataList.add(this.getDetailDataTIP(detailCFONB));
				}
				this.checkTotalAmount(multiDetailsCFONB, endingCFONB, fileName, 102, 118);
				break;
			default:
				break;
		}
		return importDataList;
	}
	
	
	public void checkTotalAmount(List<String> multiDetailsCFONB, String endingCFONB, String fileName, int amountPosStart, int amountPosEnd) throws AxelorException   {
		int totalAmount = 0;
		for(String detailCFONB : multiDetailsCFONB)  {
			totalAmount += Integer.parseInt(detailCFONB.substring(amountPosStart,amountPosEnd));
		}
		
		int totalRecord = Integer.parseInt(endingCFONB.substring(amountPosStart,amountPosEnd));
		
		LOG.debug("Controle du montant total des enregistrement détail ({}) et du montant de l'enregistrement total ({})", 
				new Object[]{totalAmount,totalRecord});
		
		if(totalAmount != totalRecord)  {
			throw new AxelorException(String.format("%s :\n Le montant total de l'enregistrement suivant n'est pas correct (fichier %s) :\n %s",
					GeneralService.getExceptionAccountingMsg(),fileName, endingCFONB), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public void testLength(String headerCFONB, List<String> multiDetailsCFONB, String endingCFONB, Company company) throws AxelorException  {
		this.testLength(headerCFONB, company, 3, 240);
		this.testLength(endingCFONB, company, 5, 240);
		for(String detailCFONB : multiDetailsCFONB)  {
			this.testLength(detailCFONB, company, 4, 240);
		}
	}
	
	
	/**
	 * Fonction permettant de récupérer les infos de rejet d'un prélèvement ou virement
	 * @param detailCFONB
	 * 			Un enregistrement 'détail' d'un rejet de prélèvement au format CFONB
	 * @param isRejectTIP
	 * 			Est ce que cela concerne les rejets de TIP ?
	 * @return
	 * 			Les infos de rejet d'un prélèvement ou virement
	 */
	public String[] getDetailData(String detailCFONB, boolean isRejectTIP)  {
		String[] detailData = new String[4];
		if (LOG.isDebugEnabled())  {  LOG.debug("detailCFONB : {}",detailCFONB);  }
		
		detailData[0] = detailCFONB.substring(214, 220);  																	// Date de rejet
		if(isRejectTIP)  {
			detailData[1] = detailCFONB.substring(159, 183).split("/")[0].trim();											// Ref facture pour TIP
		}
		else  {
			detailData[1] = detailCFONB.substring(152, 183).split("/")[0].trim();											// Ref prélèvement ou remboursement
		}
		detailData[2] = detailCFONB.substring(228, 240).substring(0, 10)+"."+detailCFONB.substring(228, 240).substring(10);	// Montant rejeté
		detailData[3] = detailCFONB.substring(226, 228);																	// Motif du rejet
		
		LOG.debug("Obtention des données d'un enregistrement détail CFONB: Date de rejet = {}, Ref prélèvement = {}, Montant rejeté = {}, Motif du rejet = {}", 
				new Object[]{detailData[0],detailData[1],detailData[2],detailData[3]});
		
		return detailData;
	}
	
	/**
	 * Fonction permettant de récupérer les infos de paiement par TIP ou TIP+chèque
	 * @param detailCFONB
	 * 			Un enregistrement 'détail' d'un paiement par TIP au format CFONB
	 * @return
	 */
	public String[] getDetailDataTIP(String detailCFONB)  {
		String[] detailData = new String[6];
		
		detailData[0] = detailCFONB.substring(2, 4);  																		// Mode de paiement
		detailData[1] = detailCFONB.substring(125, 149).split("/")[0].trim();												// Ref facture
		detailData[2] = detailCFONB.substring(81, 102);																		// RIB
		detailData[3] = detailCFONB.substring(155, 157);																	// clé RIB
		detailData[4] = detailCFONB.substring(154, 155);																	// action RIB
		detailData[5] = detailCFONB.substring(102, 116)+"."+detailCFONB.substring(116, 118);								// Montant rejeté				
		
		LOG.debug("Obtention des données d'un enregistrement détail CFONB d'un TIP : Mode de paiement = {}, Ref facture = {}, RIB = {}, clé RIB = {}, action RIB = {}, Montant rejeté = {}", 
				new Object[]{detailData[0],detailData[1],detailData[2],detailData[3],detailData[4],detailData[5]});
		
		return detailData;
	}
	
	
	/**
	 * Fonction permettant de récupérer la date de rejet de l'en-tête d'un lot de rejet de prélèvement ou virement
	 * @param detailCFONB
	 * 			Un enregistrement 'détail' d'un rejet de prélèvement au format CFONB
	 * @param isRejectTIP
	 * 			Est ce que cela concerne les rejets de TIP ?
	 * @return
	 * 			Les infos de rejet d'un prélèvement ou virement
	 */
	public String getHeaderDate(String headerCFONB)  {
		return headerCFONB.substring(10, 16);
	}
	
	
	
	/**
	 * Méthode permettant de récupérer le mode de paiement en fonction du code de début de lot de l'enregistrement
	 * @param company
	 * @param code
	 * @return
	 * @throws AxelorException 
	 */
	public PaymentMode getPaymentMode(Company company, String code) throws AxelorException  {
		LOG.debug("Récupération du mode de paiement depuis l'enregistrement CFONB : Société = {} , code CFONB = {}", new Object[]{company.getName(),code});
		
		if(code.equals(company.getAccountConfig().getIpoOperationCodeImportCFONB()))  {
			return PaymentMode.all().filter("self.code = 'TIP'").fetchOne();
		}
		else if(code.equals(company.getAccountConfig().getIpoAndChequeOperationCodeImportCFONB()))  {
			return PaymentMode.all().filter("self.code = 'TIC'").fetchOne();
		}
		throw new AxelorException(String.format("%s :\n Aucun mode de paiement trouvé pour le code %s et la société %s",
				GeneralService.getExceptionAccountingMsg(), code, company.getName()), IException.INCONSISTENCY);
	}
	
	
	/**
	 * Procédure permettant de vérifier la conformité des champs en rapport avec les imports CFONB d'une société
	 * @param company
	 * 				Une société
	 * @throws AxelorException
	 */
	public void testCompanyImportCFONBField(Company company) throws AxelorException  {
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		accountConfigService.getHeaderRecordCodeImportCFONB(accountConfig);
		accountConfigService.getDetailRecordCodeImportCFONB(accountConfig);
		accountConfigService.getEndingRecordCodeImportCFONB(accountConfig);
		accountConfigService.getTransferOperationCodeImportCFONB(accountConfig);
		accountConfigService.getDirectDebitOperationCodeImportCFONB(accountConfig);
		accountConfigService.getIpoRejectOperationCodeImportCFONB(accountConfig);
		accountConfigService.getIpoAndChequeOperationCodeImportCFONB(accountConfig);
		accountConfigService.getIpoOperationCodeImportCFONB(accountConfig);
		
	}
	
	
	
	/**
	 * 
	 * @param file
	 * @param company
	 * @param operation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 */
	public String getHeaderCFONB(List<String> file, Company company, int operation, int optionalOperation)  {
		String recordCode = this.getHeaderRecordCode(company, operation);
		String optionalRecordCode = this.getHeaderRecordCode(company, optionalOperation);
		String operationCode = this.getImportOperationCode(company, operation);
		String optionalOperationCode = this.getImportOperationCode(company, optionalOperation);
		
		LOG.debug("Obtention enregistrement en-tête CFONB: recordCode = {}, operationCode = {}, optionalRecordCode = {}, optionalOperationCode = {}", 
				new Object[]{recordCode,operationCode,optionalRecordCode,optionalOperationCode});
		
		for(String s : file)  {
			LOG.debug("file line : {}",s);
			LOG.debug("s.substring(0, 2) : {}",s.substring(0, 2));
			if(s.substring(0, 2).equals(recordCode) || s.substring(0, 2).equals(optionalRecordCode))  {
				LOG.debug("s.substring(8, 10) : {}",s.substring(8, 10));
				LOG.debug("s.substring(2, 4) : {}",s.substring(2, 4));
				if((s.substring(8, 10).equals(operationCode) && optionalOperation == 999)|| s.substring(2, 4).equals(operationCode) || s.substring(2, 4).equals(optionalOperationCode))  {
					return s;
				}
			}
			else  {
				break;
			}
		}
		return null;
	}
	
	
	/**
	 * Fonction permettant de récupérer le code d'enregistrement en-tête
	 * @param company
	 * 			Une société
	 * @param operation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * 		999 si operation non correct
	 */
	public String getHeaderRecordCode(Company company, int operation)  {
		if(operation == 0 || operation == 1 || operation == 2)  {
			return company.getAccountConfig().getHeaderRecordCodeImportCFONB();
		}
		else if(operation == 3 || operation == 4)  {
			return company.getAccountConfig().getSenderRecordCodeExportCFONB();
		}
		return "999";
	}
	
	
	/**
	 * 
	 * @param file
	 * @param company
	 * @param operation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 */
	public List<String> getDetailsCFONB(List<String> file, Company company, int operation, int optionalOperation)  {
		
		List<String> stringList = new ArrayList<String>();
		String recordCode = this.getDetailRecordCode(company, operation);
		String operationCode = this.getImportOperationCode(company, operation);
		String optionalRecordCode = this.getDetailRecordCode(company, optionalOperation);
		String optionalOperationCode = this.getImportOperationCode(company, optionalOperation);
		
		LOG.debug("Obtention enregistrement détails CFONB: recordCode = {}, operationCode = {}, optionalRecordCode = {}, optionalOperationCode = {}", 
				new Object[]{recordCode,operationCode,optionalRecordCode,optionalOperationCode});
		
		for(String s : file)  {
			if(s.substring(0, 2).equals(recordCode) || s.substring(0, 2).equals(optionalRecordCode))  {
				if((s.substring(8, 10).equals(operationCode) && optionalOperation == 999)|| s.substring(2, 4).equals(operationCode) || s.substring(2, 4).equals(optionalOperationCode))  {
					stringList.add(s);
				}
			}
			else  {
				break;
			}
		}
				
		return stringList;
	}
	
	
	
	/**
	 * Fonction permettant de récupérer le code d'enregistrement détail
	 * @param company
	 * 			Une société
	 * @param operation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * 		999 si operation non correct
	 */
	public String getDetailRecordCode(Company company, int operation)  {
		if(operation == 0 || operation == 1 || operation == 2)  {
			return company.getAccountConfig().getDetailRecordCodeImportCFONB();
		}
		else if(operation == 3 || operation == 4)  {
			return company.getAccountConfig().getRecipientRecordCodeExportCFONB();
		}
		return "999";
	}
	
	
	/**
	 * 
	 * @param file
	 * @param company
	 * @param operation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 */
	public String getEndingCFONB(List<String> file, Company company, int operation, int optionalOperation)  {
		String operationCode = this.getImportOperationCode(company, operation);
		String recordCode = this.getEndingRecordCode(company, operation);
		String optionalRecordCode = this.getEndingRecordCode(company, optionalOperation);
		String optionalOperationCode = this.getImportOperationCode(company, optionalOperation);

		LOG.debug("Obtention enregistrement fin CFONB: recordCode = {}, operationCode = {}, optionalRecordCode = {}, optionalOperationCode = {}", 
				new Object[]{recordCode,operationCode,optionalRecordCode,optionalOperationCode});
		for(String s : file)  {
			if(s.substring(0, 2).equals(recordCode) || s.substring(0, 2).equals(optionalRecordCode))  {
				if((s.substring(8, 10).equals(operationCode) && optionalOperation == 999)|| s.substring(2, 4).equals(operationCode) || s.substring(2, 4).equals(optionalOperationCode))  {
					return s;
				}
			}
			else  {
				break;
			}
		}
		return null;
	}
	
	
	/**
	 * Fonction permettant de récupérer le code d'enregistrement fin
	 * @param company
	 * 			Une société
	 * @param operation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * 		999 si operation non correct
	 */
	public String getEndingRecordCode(Company company, int operation)  {
		if(operation == 0 || operation == 1 || operation == 2)  {
			return company.getAccountConfig().getEndingRecordCodeImportCFONB();
		}
		else if(operation == 3 || operation == 4)  {
			return company.getAccountConfig().getTotalRecordCodeExportCFONB();
		}
		return "999";
	}
	
	

	/**
	 * Méthode permettant de récupérer le code "opération" défini par société en fonction du type d'opération souhaité
	 *  
	 * @param company
	 * 		La société
	 * @param operation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * 		Le code opération
	 */
	public String getImportOperationCode(Company company, int operation)  {
		String operationCode = "";
		switch(operation)  {
			case 0:
				operationCode = company.getAccountConfig().getTransferOperationCodeImportCFONB();
				break;
			case 1:
				operationCode = company.getAccountConfig().getDirectDebitOperationCodeImportCFONB();
				break;
			case 2:
				operationCode = company.getAccountConfig().getIpoRejectOperationCodeImportCFONB();
				break;
			case 3:
				operationCode = company.getAccountConfig().getIpoOperationCodeImportCFONB();
				break;
			case 4:
				operationCode = company.getAccountConfig().getIpoAndChequeOperationCodeImportCFONB();
				break;
			default:
				break;
		}
		return operationCode;
	}
	
	
	public BigDecimal getAmountRemainingFromPaymentMove(PaymentScheduleLine psl)  {
		BigDecimal amountRemaining = BigDecimal.ZERO;
		if(psl.getAdvanceOrPaymentMove() != null && psl.getAdvanceOrPaymentMove().getMoveLineList() != null)  {
			for(MoveLine moveLine : psl.getAdvanceOrPaymentMove().getMoveLineList())  {
				if(moveLine.getAccount().getReconcileOk())  {
					amountRemaining = amountRemaining.add(moveLine.getCredit());
				}
			}
		}
		return amountRemaining;
	}
	
	public BigDecimal getAmountRemainingFromPaymentMove(Invoice invoice)  {
		BigDecimal amountRemaining = BigDecimal.ZERO;
		if(invoice.getPaymentMove() != null && invoice.getPaymentMove().getMoveLineList() != null)  {
			for(MoveLine moveLine : invoice.getPaymentMove().getMoveLineList())  {
				if(moveLine.getAccount().getReconcileOk())  {
					amountRemaining = amountRemaining.add(moveLine.getCredit());
				}
			}
		}
		return amountRemaining;
	}
	
}
