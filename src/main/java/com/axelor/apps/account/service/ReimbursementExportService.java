/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.IMove;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
//import com.axelor.apps.tool.xml.Marschaller;
//import com.axelor.apps.xsd.sepa.AccountIdentification3Choice;
//import com.axelor.apps.xsd.sepa.AmountType2Choice;
//import com.axelor.apps.xsd.sepa.BranchAndFinancialInstitutionIdentification3;
//import com.axelor.apps.xsd.sepa.CashAccount7;
//import com.axelor.apps.xsd.sepa.CreditTransferTransactionInformation1;
//import com.axelor.apps.xsd.sepa.CurrencyAndAmount;
//import com.axelor.apps.xsd.sepa.Document;
//import com.axelor.apps.xsd.sepa.FinancialInstitutionIdentification5Choice;
//import com.axelor.apps.xsd.sepa.GroupHeader1;
//import com.axelor.apps.xsd.sepa.Grouping1Code;
//import com.axelor.apps.xsd.sepa.ObjectFactory;
//import com.axelor.apps.xsd.sepa.Pain00100102;
//import com.axelor.apps.xsd.sepa.PartyIdentification8;
//import com.axelor.apps.xsd.sepa.PaymentIdentification1;
//import com.axelor.apps.xsd.sepa.PaymentInstructionInformation1;
//import com.axelor.apps.xsd.sepa.PaymentMethod3Code;
//import com.axelor.apps.xsd.sepa.PaymentTypeInformation1;
//import com.axelor.apps.xsd.sepa.RemittanceInformation1;
//import com.axelor.apps.xsd.sepa.ServiceLevel1Code;
//import com.axelor.apps.xsd.sepa.ServiceLevel2Choice;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReimbursementExportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReimbursementExportService.class); 
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private ReconcileService reconcileService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private BlockingService blockingService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	private LocalDate today;

	@Inject
	public ReimbursementExportService() {

		this.today = GeneralService.getTodayDate();
	}
	
	/**
	 * 
	 * @param reimbursementExport
	 */
	public void fillMoveLineSet(Reimbursement reimbursement, List<MoveLine> moveLineList, BigDecimal total)  {
		
		LOG.debug("In fillMoveLineSet");
		LOG.debug("Nombre de trop-perçus trouvés : {}", moveLineList.size());
		
		for(MoveLine moveLine : moveLineList)  {
			// On passe les lignes d'écriture (trop perçu) à l'état 'en cours de remboursement'
			moveLine.setReimbursementStateSelect(IAccount.REIMBURSING);
		}
		
		reimbursement.setMoveLineSet(new HashSet<MoveLine>());
		reimbursement.getMoveLineSet().addAll(moveLineList);
		
		LOG.debug("End fillMoveLineSet");
	}
	
	
	/**
	 * 
	 * @param reimbursementExport
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Reimbursement runCreateReimbursement(List<MoveLine> moveLineList, Company company, Partner partner) throws AxelorException  {
		
		LOG.debug("In runReimbursementProcess");
		
		BigDecimal total = this.getTotalAmountRemaining(moveLineList);
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		// Seuil bas respecté et remboursement manuel autorisé
		if(total.compareTo(accountConfig.getLowerThresholdReimbursement()) > 0 )  {
			
			Reimbursement reimbursement = createReimbursement(partner, company);
			
			fillMoveLineSet(reimbursement, moveLineList, total);
			
			if(total.compareTo(accountConfig.getUpperThresholdReimbursement()) > 0 || reimbursement.getBankDetails() == null)  {
			// Seuil haut dépassé	
				reimbursement.setStatus(Status.findByCode("tov"));
			}
			else  {
				reimbursement.setStatus(Status.findByCode("val"));
			}
			
			reimbursement.save();
			return reimbursement;
		}

		LOG.debug("End runReimbursementProcess");
		return null;
	}
	
	
	/**
	 * Fonction permettant de calculer le montant total restant à payer / à lettrer
	 * @param movelineList
	 * 				Une liste de ligne d'écriture
	 * @return
	 * 				Le montant total restant à payer / à lettrer
	 */
	public BigDecimal getTotalAmountRemaining(List<MoveLine> moveLineList)  {
		BigDecimal total = BigDecimal.ZERO;
		for(MoveLine moveLine : moveLineList)  {
			total=total.add(moveLine.getAmountRemaining());
		}
		
		LOG.debug("Total Amount Remaining : {}",total);
		
		return total;
	}
	
	
	/**
	 * Methode permettant de créer l'écriture de remboursement
	 * @param reimbursementExport
	 * 				Un objet d'export des prélèvements
	 * @throws AxelorException
	 */
	public void createReimbursementMove(Reimbursement reimbursement, Company company) throws AxelorException  {
		reimbursement = Reimbursement.find(reimbursement.getId());
		
		Partner partner = null;
		Move newMove = null;
		boolean first = true;
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		if(reimbursement.getMoveLineSet() != null && !reimbursement.getMoveLineSet().isEmpty())  {
			int seq = 1;
			for(MoveLine moveLine : reimbursement.getMoveLineSet())  {
				BigDecimal amountRemaining = moveLine.getAmountRemaining();
				if(amountRemaining.compareTo(BigDecimal.ZERO) > 0)  {
					partner = moveLine.getPartner();
					
					// On passe les lignes d'écriture (trop perçu) à l'état 'remboursé'
					moveLine.setReimbursementStateSelect(IAccount.REIMBURSED);
					
					if(first)  {
						newMove = moveService.createMove(accountConfig.getReimbursementJournal(), company, null, partner, null);
						first = false;
					}
					// Création d'une ligne au débit
					MoveLine newDebitMoveLine = moveLineService.createMoveLine(newMove , partner, moveLine.getAccount(), amountRemaining, true, false, today, seq, null);
					newMove.getMoveLineList().add(newDebitMoveLine);		
					if(reimbursement.getDescription() != null && !reimbursement.getDescription().isEmpty())  {
						newDebitMoveLine.setDescription(reimbursement.getDescription());
					}
					
					seq++;
					
					//Création de la réconciliation
					Reconcile reconcile = reconcileService.createReconcile(newDebitMoveLine, moveLine, amountRemaining);
					reconcileService.confirmReconcile(reconcile, false);
				}			
			}
			// Création de la ligne au crédit
			MoveLine newCreditMoveLine = moveLineService.createMoveLine(newMove, partner, accountConfig.getReimbursementAccount(), reimbursement.getAmountReimbursed(), false, false, today, seq, null);
		
			newMove.getMoveLineList().add(newCreditMoveLine);
			if(reimbursement.getDescription() != null && !reimbursement.getDescription().isEmpty())  {
				newCreditMoveLine.setDescription(reimbursement.getDescription());
			}
			moveService.validateMove(newMove);
			newMove.save();
		}
	}
	
	
	/**
	 * Procédure permettant de tester la présence des champs et des séquences nécessaire aux remboursements.
	 *
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		accountConfigService.getReimbursementAccount(accountConfig);
		accountConfigService.getReimbursementJournal(accountConfig);
		accountConfigService.getReimbursementExportFolderPath(accountConfig);
		
		if(!sequenceService.hasSequence(IAdministration.REIMBOURSEMENT, company)) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Remboursement pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void reimburse(Reimbursement reimbursement, Company company) throws AxelorException  {
		reimbursement.setAmountReimbursed(reimbursement.getAmountToReimburse());
		this.createReimbursementMove(reimbursement, company);
		reimbursement.setStatus(Status.findByCode("rei"));
		reimbursement.save();
	}
	
	
	/**
	 * Méthode permettant de créer un remboursement
	 * @param partner
	 * 				Un tiers
	 * @param company
	 * 				Une société
	 * @param reimbursementExport
	 * 				Un export des remboursement
	 * @return
	 * 			  	Le remboursmeent créé
	 * @throws AxelorException
	 */
	public Reimbursement createReimbursement(Partner partner, Company  company) throws AxelorException   {
		Reimbursement reimbursement = new Reimbursement();
		reimbursement.setPartner(partner);
		
		BankDetails bankDetails = partner.getBankDetails();
		
		reimbursement.setBankDetails(bankDetails);
		
		reimbursement.setRef(sequenceService.getSequenceNumber(IAdministration.REIMBOURSEMENT, company));
		
		return reimbursement;
	}
	
	
	/**
	 * Le tiers peux t-il être remboursé ?
	 * Si le tiers est bloqué en remboursement et que la date de fin de blocage n'est pas passée alors on ne peut pas rembourser.
	 * 
	 * @return
	 */
	public boolean canBeReimbursed(Partner partner, Company company){
		
		return !blockingService.isReminderBlocking(partner, company);
	}
	
	
	
	/**
	 * Procédure permettant de mettre à jour la liste des RIBs du tiers
	 * @param reimbursement
	 * 				Un remboursement
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updatePartnerCurrentRIB(Reimbursement reimbursement)  {
		BankDetails bankDetails = reimbursement.getBankDetails();
		Partner partner = reimbursement.getPartner();
		
		if(partner != null && bankDetails != null && !bankDetails.equals(partner.getBankDetails()))  {
			partner.setBankDetails(bankDetails);
			partner.save();
		}
	}
	
	
	/**
	 * Méthode permettant de créer un fichier xml de virement au format SEPA
	 * @param export
	 * @param dateTime
	 * @param reimbursementList
	 * @throws AxelorException
	 * @throws DatatypeConfigurationException
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void exportSepa(Company company, DateTime dateTime, List<Reimbursement> reimbursementList, BankDetails bankDetails) throws AxelorException, DatatypeConfigurationException, JAXBException, IOException {
		
		String exportFolderPath = accountConfigService.getReimbursementExportFolderPath(accountConfigService.getAccountConfig(company));
		
		if (exportFolderPath == null)  {
			throw new AxelorException(String.format("Le dossier d'export des remboursement (format SEPA) n'est pas configuré pour la société %s.",
					company.getName()), IException.MISSING_FIELD);
		}
			
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
		Date date = dateTime.toDate();
		BigDecimal ctrlSum = BigDecimal.ZERO;
		int nbOfTxs = 0;
		
	/**
	 * Création du documemnt XML en mémoire
	 */

//		ObjectFactory factory = new ObjectFactory();
//		
//	// Débit
//		
//		// Paiement
//		ServiceLevel2Choice svcLvl = factory.createServiceLevel2Choice();
//		svcLvl.setCd(ServiceLevel1Code.SEPA);
//
//		PaymentTypeInformation1 pmtTpInf = factory.createPaymentTypeInformation1();
//		pmtTpInf.setSvcLvl(svcLvl);
//		
//		// Payeur
//		PartyIdentification8 dbtr = factory.createPartyIdentification8();
//		dbtr.setNm(bankDetails.getOwnerName());
//		
//		// IBAN
//		AccountIdentification3Choice iban = factory.createAccountIdentification3Choice();
//		iban.setIBAN(bankDetails.getIban());
//		
//		CashAccount7 dbtrAcct = factory.createCashAccount7();
//		dbtrAcct.setId(iban);
//		
//		// BIC
//		FinancialInstitutionIdentification5Choice finInstnId = factory.createFinancialInstitutionIdentification5Choice();
//		finInstnId.setBIC(bankDetails.getBic());
//		
//		BranchAndFinancialInstitutionIdentification3 dbtrAgt = factory.createBranchAndFinancialInstitutionIdentification3();
//		dbtrAgt.setFinInstnId(finInstnId);
//		
//	// Lot
//		
//		PaymentInstructionInformation1 pmtInf = factory.createPaymentInstructionInformation1();
//		pmtInf.setPmtMtd(PaymentMethod3Code.TRF);
//		pmtInf.setPmtTpInf(pmtTpInf);
//		pmtInf.setReqdExctnDt(datatypeFactory.newXMLGregorianCalendar(dateFormat.format(date)));
//		pmtInf.setDbtr(dbtr);
//		pmtInf.setDbtrAcct(dbtrAcct);
//		pmtInf.setDbtrAgt(dbtrAgt);
//		
//	// Crédit
//		CreditTransferTransactionInformation1 cdtTrfTxInf = null; PaymentIdentification1 pmtId = null;
//		AmountType2Choice amt = null; CurrencyAndAmount instdAmt = null;
//		PartyIdentification8 cbtr = null; CashAccount7 cbtrAcct = null;
//		BranchAndFinancialInstitutionIdentification3 cbtrAgt = null;
//		RemittanceInformation1 rmtInf = null;
//		for (Reimbursement reimbursement : reimbursementList){
//
//			reimbursement = Reimbursement.find(reimbursement.getId());
//			
//			nbOfTxs++;
//			ctrlSum = ctrlSum.add(reimbursement.getAmountReimbursed());
//			bankDetails = reimbursement.getBankDetails();
//			
//			// Paiement
//			pmtId = factory.createPaymentIdentification1();
//			pmtId.setEndToEndId(reimbursement.getRef());
//			
//			// Montant
//			instdAmt = factory.createCurrencyAndAmount();
//			instdAmt.setCcy("EUR");
//			instdAmt.setValue(reimbursement.getAmountReimbursed());
//			
//			amt = factory.createAmountType2Choice();
//			amt.setInstdAmt(instdAmt);
//			
//			// Débiteur
//			cbtr = factory.createPartyIdentification8();
//			cbtr.setNm(bankDetails.getOwnerName());
//			
//			// IBAN
//			iban = factory.createAccountIdentification3Choice();
//			iban.setIBAN(bankDetails.getIban());
//			
//			cbtrAcct = factory.createCashAccount7();
//			cbtrAcct.setId(iban);
//			
//			// BIC
//			finInstnId = factory.createFinancialInstitutionIdentification5Choice();
//			finInstnId.setBIC(bankDetails.getBic());
//			
//			cbtrAgt = factory.createBranchAndFinancialInstitutionIdentification3();
//			cbtrAgt.setFinInstnId(finInstnId);
//			
//			rmtInf = factory.createRemittanceInformation1();
//			
//			rmtInf.getUstrd().add(reimbursement.getDescription());
//						
//			// Transaction
//			cdtTrfTxInf = factory.createCreditTransferTransactionInformation1();
//			cdtTrfTxInf.setPmtId(pmtId);
//			cdtTrfTxInf.setAmt(amt);
//			cdtTrfTxInf.setCdtr(cbtr);
//			cdtTrfTxInf.setCdtrAcct(cbtrAcct);
//			cdtTrfTxInf.setCdtrAgt(cbtrAgt);
//			cdtTrfTxInf.setRmtInf(rmtInf);
//			
//			pmtInf.getCdtTrfTxInf().add(cdtTrfTxInf);
//		}
//		
//	// En-tête
//		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//		
//		GroupHeader1 grpHdr = factory.createGroupHeader1();
//		grpHdr.setCreDtTm(datatypeFactory.newXMLGregorianCalendar(dateFormat.format(date)));
//		grpHdr.setNbOfTxs(Integer.toString(nbOfTxs));
//		grpHdr.setCtrlSum(ctrlSum);
//		grpHdr.setGrpg(Grouping1Code.MIXD);
//		grpHdr.setInitgPty(dbtr);
//	
//	// Parent
//		Pain00100102 pain00100102 = factory.createPain00100102();
//		pain00100102.setGrpHdr(grpHdr);
//		pain00100102.getPmtInf().add(pmtInf);
//
//	// Document		
//		Document xml = factory.createDocument();
//		xml.setPain00100102(pain00100102);
		
		/**
		 * Création du documemnt XML physique
		 */
//		Marschaller.marschalFile(factory.createDocument(xml), "com.axelor.apps.xsd.sepa", 
//				exportFolderPath, String.format("%s.xml", dateFormat.format(date)));
//		
	}
	
	
	/************************* Remboursement lors d'une facture fin de cycle *********************************/
	
	
	
	/**
	 * Procédure permettant de créer un remboursement si un trop perçu est généré à la facture fin de cycle
	 * @param partner
	 * 			Un tiers
	 * @param company
	 * 			Une société
	 * @param moveLine
	 * 			Un trop-perçu
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createReimbursementInvoice(Partner partner, Company company, List<MoveLine> moveLineList) throws AxelorException  {
			
		BigDecimal total = this.getTotalAmountRemaining(moveLineList);
		
		if(total.compareTo(BigDecimal.ZERO) > 0)  {			
			
			this.testCompanyField(company);
			
			Reimbursement reimbursement = this.createReimbursement(partner, company);
				
			this.fillMoveLineSet(reimbursement, moveLineList, total);
				
			if(total.compareTo(company.getAccountConfig().getUpperThresholdReimbursement()) > 0 || reimbursement.getBankDetails() == null)  {
			// Seuil haut dépassé	
				reimbursement.setStatus(Status.findByCode("tov"));
			}
			else  {
			// Seuil haut non dépassé
				reimbursement.setStatus(Status.findByCode("val"));
			}
			reimbursement.save();
		}
		
	}
	
	
	/**
	 * Procédure permettant de créer un remboursement si un trop perçu est généré à la facture fin de cycle grand comptes
	 * @param invoice
	 * 			Une facture
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createReimbursementInvoice(Invoice invoice) throws AxelorException  {
		Company company = invoice.getCompany();
		Partner partner = invoice.getPartner();
		
		// récupération des trop-perçus du tiers
		List<MoveLine> moveLineList = MoveLine.filter("self.account.reconcileOk = 'true' AND self.fromSchedulePaymentOk = 'false' " +
				"AND self.move.state = ?1 AND self.amountRemaining > 0 AND self.credit > 0 AND self.partner = ?2 AND self.reimbursementStateSelect = ?3 "
				,IMove.VALIDATED_MOVE , partner, IAccount.NULL).fetch();
		
		this.createReimbursementInvoice(partner, company, moveLineList);
		
	}
	
	
}
