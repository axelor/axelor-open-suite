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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerService;
import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentScheduleService {

	private static final Logger LOG = LoggerFactory.getLogger(PaymentScheduleService.class);
	
	@Inject
	private PaymentScheduleLineService psls;
	
	@Inject
	private MoveService ms;
	
	@Inject
	private MoveLineService mls;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	private AlarmEngineService<Partner> aes;

	@Inject
	private UserInfoService uis;
	
	@Inject
	private DoubtfulCustomerService dcs;
	
	private LocalDate date;

	@Inject
	public PaymentScheduleService() {

		date = GeneralService.getTodayDate();
		
	}
	
	/**
	 * Création d'un échéancier sans ces lignes.
	 * 
	 * @param contractLine
	 * 			Le contrat cible.
	 * @param invoices
	 *			Collection de factures.
	 * @param company
	 * 			La société.
	 * @param startDate
	 * 			Date de première échéance.
	 * @param nbrTerm
	 * 			Nombre d'échéances.
	 * 
	 * @return
	 * 			L'échéancier créé.
	 * @throws AxelorException 
	 */
	public PaymentSchedule createPaymentSchedule(Partner partner, Company company, Set<Invoice> invoices, LocalDate startDate, int nbrTerm) throws AxelorException{
		
		Invoice invoice = null;
		
		PaymentSchedule paymentSchedule = this.createPaymentSchedule(partner, invoice, company, date, startDate,
				nbrTerm, partner.getBankDetails(), partner.getPaymentMode());
		
		paymentSchedule.getInvoiceSet().addAll(invoices);
		
		return paymentSchedule;
	}

	/**
	 * Création d'un échéancier sans ces lignes.
	 * 
	 * @param contractLine
	 * 			Le contrat cible.
	 * @param invoice
	 * 			Facture globale permettant de définir la facture pour une échéance.
	 * 			L'échéancier est automatiquement associé à la facture si celle-ci existe.
	 * @param company
	 * 			La société.
	 * @param date
	 * 			Date de création.
	 * @param startDate
	 * 			Date de première échéance.
	 * @param nbrTerm
	 * 			Nombre d'échéances.
	 * @param bankDetails
	 * 			RIB.
	 * @param paymentMode
	 * 			Mode de paiement.
	 * @param payerPartner
	 * 			Tiers payeur.
	 * @param type
	 * 			Type de l'échéancier.
	 * 			<code>0 = paiement</code>
	 * 			<code>1 = mensu masse</code>
	 * 			<code>2 = mensu grand-compte</code>
	 * 
	 * @return
	 * 			L'échéancier créé.
	 * @throws AxelorException 
	 */
	public PaymentSchedule createPaymentSchedule(Partner partner, Invoice invoice, Company company, LocalDate date, LocalDate startDate, int nbrTerm, BankDetails bankDetails, PaymentMode paymentMode) throws AxelorException{
		
		PaymentSchedule paymentSchedule = new PaymentSchedule();
		
		paymentSchedule.setCompany(company);
		paymentSchedule.setScheduleId(this.getPaymentScheduleSequence(company));
		paymentSchedule.setCreationDate(date);
		paymentSchedule.setStartDate(startDate);
		paymentSchedule.setNbrTerm(nbrTerm);
		paymentSchedule.setBankDetails(bankDetails);
		paymentSchedule.setPaymentMode(paymentMode);
		paymentSchedule.setPartner(partner);
		
		if (paymentSchedule.getInvoiceSet() == null) {
			paymentSchedule.setInvoiceSet(new HashSet<Invoice>());
		}
		else {
			paymentSchedule.getInvoiceSet().clear();
		}
		
		if (invoice != null){
			paymentSchedule.setInvoice(invoice);
			invoice.setPaymentSchedule(paymentSchedule);
		}
		
		return paymentSchedule;
	}
	
	
	/**
	 * Fonction permettant de tester et de récupérer une séquence de prélèvement
	 * @param company
	 * 			Une société
	 * @param journal
	 * 			Un journal
	 * @return
	 * @throws AxelorException
	 */
	public String getPaymentScheduleSequence(Company company) throws AxelorException  {
		String seq = sgs.getSequence(IAdministration.PAYMENT_SCHEDULE, company, null, false);
		if(seq == null)  {
			throw new AxelorException(String.format(
							"%s :\n Veuillez configurer une séquence Echéancier pour la société %s ",
							GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
	
	
	/**
	 * Obtenir le total des factures des lignes d'un échéancier.
	 * 
	 * @param paymentSchedule
	 * 			L'échéancier cible.
	 * 
	 * @return
	 * 			Le somme des montants TTC des lignes de l'échéancier. 
	 */
	public BigDecimal getInvoiceTermTotal(PaymentSchedule paymentSchedule){
		
		BigDecimal totalAmount = BigDecimal.ZERO;
		
		if (paymentSchedule != null && paymentSchedule.getPaymentScheduleLineList() != null && !paymentSchedule.getPaymentScheduleLineList().isEmpty())  {
			for (PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList()){
				if (paymentScheduleLine.getInTaxAmount() != null) {
					
					LOG.debug("Somme TTC des lignes de l'échéancier {} : total = {}, ajout = {}", new Object[] {paymentSchedule.getScheduleId(), totalAmount, paymentScheduleLine.getInTaxAmount()});
					
					totalAmount = totalAmount.add(paymentScheduleLine.getInTaxAmount());
				}
			}
		}
		
		LOG.debug("Obtention de la somme TTC des lignes de l'échéancier {} : {}", new Object[] {paymentSchedule.getScheduleId(), totalAmount});
		
		return totalAmount;
				
	}
	

	/**
	 * Mise à jour d'un échéancier avec un nouveau montant d'échéance.
	 * 
	 * @param paymentSchedule
	 * 			L'échéancier cible.
	 * @param inTaxTotal
	 * 			Nouveau montant d'une échéance.
	 */
	@Transactional
	public void updatePaymentSchedule(PaymentSchedule paymentSchedule, BigDecimal inTaxTotal){
		
		LOG.debug("Mise à jour de l'échéancier {} : {}", new Object[] {paymentSchedule.getScheduleId(), inTaxTotal});
		
		for (PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList()){
			
			if (paymentScheduleLine.getStatus() != null && paymentScheduleLine.getStatus().getCode().equals("upr") && !paymentScheduleLine.getRejectedOk()) {
				
				LOG.debug("Mise à jour de la ligne {} ", paymentScheduleLine.getName());

				paymentScheduleLine.setInTaxAmount(inTaxTotal);
			}
		}
		
		paymentSchedule.save();
		
	}
	
	/**
	 * Création d'un échéancier avec ces lignes.
	 * 
	 * @param contractLine
	 * 			Le contrat cible.
	 * @param company
	 * 			La société.
	 * @param date
	 * 			Date de création.
	 * @param firstTermDate
	 * 			Date de première échéance.
	 * @param initialInTaxAmount
	 * 			Montant d'une échéance.
	 * @param nbrTerm
	 * 			Nombre d'échéances.
	 * @param bankDetails
	 * 			RIB.
	 * @param paymentMode
	 * 			Mode de paiement.
	 * @param payerPartner
	 * 			Tiers payeur.
	 * @param type
	 * 			Type de l'échéancier.
	 * 			<code>0 = paiement</code>
	 * 			<code>1 = mensu masse</code>
	 * 			<code>2 = mensu grand-compte</code>
	 * 
	 * @return
	 * 			L'échéancier créé.
	 * @throws AxelorException
	 */
	public PaymentSchedule createPaymentSchedule(Partner partner, Company company, LocalDate date, LocalDate firstTermDate, BigDecimal initialInTaxAmount, int nbrTerm, BankDetails bankDetails, PaymentMode paymentMode) throws AxelorException{
		
		Invoice invoice = null;
		PaymentSchedule paymentSchedule = this.createPaymentSchedule(partner, invoice, company, date, firstTermDate, nbrTerm, bankDetails, paymentMode);
			
		paymentSchedule.setPaymentScheduleLineList(new ArrayList<PaymentScheduleLine>());
		Status status = Status.all().filter("code = 'upr'").fetchOne();
		for (int term = 1; term < nbrTerm + 1; term++){
			paymentSchedule.getPaymentScheduleLineList().add(psls.createPaymentScheduleLine(paymentSchedule, initialInTaxAmount, term, firstTermDate.plusMonths(term-1), status));
		}
		
		return paymentSchedule;
	}

	
	/**
	 * This method is used to get the movelines to be paid based on a paymentSchedule
	 * It loops on the invoice M2M content and gets the movelines which are to pay 
	 * @param ps
	 * @return
	 */
	public List<MoveLine> getPaymentSchedulerMoveLineToPay(PaymentSchedule paymentSchedule){
		LOG.debug("In getPaymentSchedulerMoveLineToPay ....");
		List<MoveLine> moveLines = new ArrayList<MoveLine>();
		for (Invoice invoice : paymentSchedule.getInvoiceSet())  {
			if (invoice.getInTaxTotalRemaining().compareTo(BigDecimal.ZERO) > 0 && invoice.getMove() != null && invoice.getMove().getMoveLineList() != null)  {
				for (MoveLine moveLine : invoice.getMove().getMoveLineList()){
					if (moveLine.getAccount().getReconcileOk() && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0 && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0){
						moveLines.add(moveLine);
					}
				}
			}
		}
		LOG.debug("End getPaymentSchedulerMoveLineToPay.");
		return moveLines;
	}
	
	
	/**
	 * Permet de valider une saisie paiement.
	 * Crée une moveLine pour chaque ligne et une move représentant la totalité de la saisie paiement
	 * @param ps
	 * 
	 * @return 
	 *
	 * @throws AxelorException 			
	 */
	public Move validatePaymentSchedule(PaymentSchedule paymentSchedule, boolean fromMonthlyPayment) throws AxelorException {
				
		LOG.debug("Validation de l'échéancier {}", paymentSchedule.getScheduleId());
		
		Move move = null;
		
		if (paymentSchedule != null){
			
			if (paymentSchedule.getTotalToPayAmount().compareTo(paymentSchedule.getInTaxAmount()) != 0)  {
				throw new AxelorException(String.format("Le total des échances de l'échéancier %s est différent du total des factures : %s <> %s",
						paymentSchedule.getScheduleId(), paymentSchedule.getTotalToPayAmount(), paymentSchedule.getInTaxAmount()), IException.INCONSISTENCY);
			}
				
			if (paymentSchedule.getPaymentScheduleLineList() != null){

				BigDecimal total = BigDecimal.ZERO;

				for (PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList())  {
					total = total.add(paymentScheduleLine.getInTaxAmount());
				}
				
				Company company = paymentSchedule.getCompany();
				Partner payerPartner = paymentSchedule.getPartner();
				
				if (company.getTechnicalJournal() == null){
					throw new AxelorException(String.format("Veuillez configurer un Journal Technique pour la société %s", company.getName()), IException.CONFIGURATION_ERROR);
				}
				
				move = ms.createMove(company.getTechnicalJournal(), company, null, payerPartner, null, false);
				MoveLine moveLine = null;
				int moveLineId = 0;
				
				for (PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList()){
					
					moveLine = mls.createMoveLine(move, payerPartner, company.getCustomerAccount(), paymentScheduleLine.getInTaxAmount(), true, false, paymentScheduleLine.getScheduleDate(), moveLineId++, true, false, true, null);
					moveLine.setPaymentScheduleLine(paymentScheduleLine);
					move.getMoveLineList().add(moveLine);
					paymentScheduleLine.setMoveLineGenerated(moveLine);
					
				}
				
				moveLine = mls.createMoveLine(move, payerPartner, company.getCustomerAccount(), total, false, false, date, moveLineId++, true, false, true, null);
				move.getMoveLineList().add(moveLine);
				
				if (paymentSchedule.getInvoiceSet() != null){
					
					List<MoveLine> moveLineInvoiceToPay = this.getPaymentSchedulerMoveLineToPay(paymentSchedule);
					
					for (MoveLine moveLineInvoice : moveLineInvoiceToPay){

						moveLineInvoice.setIgnoreInReminderOk(true);
						moveLineInvoice.getMove().getInvoice().setSchedulePaymentOk(true);
						moveLineInvoice.getMove().getInvoice().setPaymentSchedule(paymentSchedule);
						
					}
				}
				
				move.setState("validated");
			}
		}
		
		return move;
		
	}
	
	public void validate(PaymentSchedule paymentSchedule){
		
		paymentSchedule.setState("2");
		
		Partner partner = paymentSchedule.getPartner();
		
		if (PaymentSchedule.all().filter("partner = ?1 AND state = '2'", partner).count() > 0){
			//TODO: attribuer code au message
			Alarm alarm = aes.get("", partner, true);
		}
		
	}
	
	/**
	 * Methode qui annule un échéancier
	 * 
	 * @param paymentSchedule
	 */
	public void cancelPaymentSchedule(PaymentSchedule paymentSchedule){
		Status closedStatus = Status.all().filter("self.code = 'clo'").fetchOne();
		
		// L'échéancier est passé à annulé
		paymentSchedule.setState("4");
		
		Move move = null;
		
		for(PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList())  {
		
			// Si l'échéance n'est pas complètement payée
			if(paymentScheduleLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0 ) {
			
				// L'échéance est passée à cloturé
				paymentScheduleLine.setStatus(closedStatus);
				
				if(move == null && paymentScheduleLine.getMoveLineGenerated() != null)  {
					
					// Récupération de la pièce technique
					move = paymentScheduleLine.getMoveLineGenerated().getMove();
				}
			}
		}
		
		for(Invoice invoice : paymentSchedule.getInvoiceSet())  {
			// L'échéancier n'est plus selectionné sur la facture
			invoice.setPaymentSchedule(null);
			
			// L'échéancier est assigné dans un nouveau champs afin de garder un lien invisble pour l'utilisateur, mais utilisé pour le passage en irrécouvrable
			invoice.setCanceledPaymentSchedule(paymentSchedule);  
			invoice.setSchedulePaymentOk(false);
		}
		
		if(move != null)  {
			move.setState(IAccount.CANCELED_MOVE);
		}
		
	}
	
	
	/**
	 * Methode permettant de savoir si l'échéance passée en paramètre est la dernière de l'échéancier
	 * @param paymentScheduleLine
	 * @return
	 */
	public boolean isLastSchedule(PaymentScheduleLine paymentScheduleLine)  {
		if(paymentScheduleLine != null)  {
			if(PaymentScheduleLine.all().filter("paymentSchedule = ?1 and scheduleDate > ?2 and status.code ='upr'", paymentScheduleLine.getPaymentSchedule(), paymentScheduleLine.getScheduleDate()).fetchOne() == null)  {
				LOG.debug("Dernière échéance");
				return true;
			}
			else  {
				return false;
			}
		}
		else  {
			return false;
		}
	}
	

	/**
	 * Méthode permettant de passer les statuts des lignes d'échéances et de l'échéancier à 'clo' ie cloturé
	 * @param invoice
	 * 			Une facture de fin de cycle
	 * @throws AxelorException 
	 */
  	public void closePaymentSchedule(PaymentSchedule paymentSchedule) throws AxelorException  {
		
		LOG.debug("Cloture de l'échéancier");
		
		//On récupère un statut cloturé, afin de pouvoir changer l'état des lignes d'échéanciers
		Status statusClo = Status.all().filter("code = 'clo'").fetchOne();
		
		for(PaymentScheduleLine psl : paymentSchedule.getPaymentScheduleLineList())  {
			psl.setStatus(statusClo);
		}
		paymentSchedule.setState("3");
		
	}
  	
  	public LocalDate getMostOldDatePaymentScheduleLine(List<PaymentScheduleLine> paymentScheduleLineList)  {
		LocalDate minPaymentScheduleLineDate = new LocalDate();
		
		if(paymentScheduleLineList != null && !paymentScheduleLineList.isEmpty())  {
			for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
				if(minPaymentScheduleLineDate.isAfter(paymentScheduleLine.getScheduleDate()))  {	minPaymentScheduleLineDate=paymentScheduleLine.getScheduleDate();	}
			}
		}
		else  {	minPaymentScheduleLineDate=null;	}
		return minPaymentScheduleLineDate;
	}
  	
  	public LocalDate getMostRecentDatePaymentScheduleLine(List<PaymentScheduleLine> paymentScheduleLineList)  {
		LocalDate minPaymentScheduleLineDate = new LocalDate();
		
		if(paymentScheduleLineList != null && !paymentScheduleLineList.isEmpty())  {
			for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
				if(minPaymentScheduleLineDate.isBefore(paymentScheduleLine.getScheduleDate()))  {	minPaymentScheduleLineDate=paymentScheduleLine.getScheduleDate();	}
			}
		}
		else  {	minPaymentScheduleLineDate=null;	}
		return minPaymentScheduleLineDate;
	}
  	
	
// Transactional
	
	/**
	 * Valider un échéancier.
	 * 
	 * @param paymentSchedule
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validatePaymentSchedule(PaymentSchedule paymentSchedule) throws AxelorException {
		if(paymentSchedule.getPaymentScheduleLineList() == null || paymentSchedule.getPaymentScheduleLineList().size() == 0)  {
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez d'abord créer les lignes d'échéancier pour l'échéancier %s ",
					GeneralService.getExceptionAccountingMsg(), paymentSchedule.getScheduleId()), IException.INCONSISTENCY);
		}
		
		Move generatedMove = this.validatePaymentSchedule(paymentSchedule, false);
		if (generatedMove != null){
			paymentSchedule.setGeneratedMove(generatedMove);
			paymentSchedule.setState("2");
		}
		
		if(	paymentSchedule.getPaymentScheduleType() != null && (
									paymentSchedule.getPaymentScheduleType().getCode().equals("ELJ") ||
									paymentSchedule.getPaymentScheduleType().getCode().equals("ERJ") ||
									paymentSchedule.getPaymentScheduleType().getCode().equals("ESU")))  {
			// Passage en client douteux
			dcs.doubtfulCustomerProcess(paymentSchedule);
		}
		
		paymentSchedule.save();
		
	}
	
	/**
	 * Créer des lignes d'échéancier à partir des lignes de factures de celui-ci.
	 * 
	 * @param paymentSchedule
	 * @throws AxelorException
	 */
	@Transactional
	public void createPaymentScheduleLines(PaymentSchedule paymentSchedule){
		
		if (paymentSchedule.getPaymentScheduleLineList() == null)  {
			paymentSchedule.setPaymentScheduleLineList(new ArrayList<PaymentScheduleLine>());
		}
		else  { 
			paymentSchedule.getPaymentScheduleLineList().clear();
		}
		
		paymentSchedule.getPaymentScheduleLineList().addAll(psls.createPaymentScheduleLines(paymentSchedule, paymentSchedule.getInvoiceSet()));
		paymentSchedule.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void toCancelPaymentSchedule(PaymentSchedule paymentSchedule){
		this.cancelPaymentSchedule(paymentSchedule);
		paymentSchedule.save();
	}
	
	
	/**
	 * Methode permettant de créer un échéancier de paiement sur 1 ou 2 mois en fonction du montant restant à payer sur la facture de fin de cycle
	 * @param invoice
	 * 			Une facture de fin de cycle
	 * @param fromBatch
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createPaymentSchedule(Invoice invoice) throws AxelorException  {
		
		LOG.debug("Begin CreatePaymentSchedule");
			
		PaymentSchedule paymentScheduleReference = invoice.getPaymentSchedule();
		Partner partner = invoice.getClientPartner();
		Company company = invoice.getCompany();

		if(paymentScheduleReference != null)  {

			LOG.debug("Restant dû de la facture : {}", invoice.getInTaxTotalRemaining());
			
			// Si le restant dû est positif
			if(invoice.getInTaxTotalRemaining().compareTo(BigDecimal.ZERO) > 0)  {
				
				LOG.debug("CreatePaymentSchedule - le restant dû est positif");
				
				// Récupération du montant de la dernière mensualité
				LOG.debug("Récupération du montant de la dernière mensualité");
				
				List<PaymentScheduleLine> pslList = new ArrayList<PaymentScheduleLine>();
				pslList = paymentScheduleReference.getPaymentScheduleLineList();
				BigDecimal pslAmount = pslList.get(pslList.size()-1).getInTaxAmount();
				
				// Récupération de la date de la dernière mensualité
				LOG.debug("Récupération de la date de la dernière mensualité");
				LocalDate lastScheduleDate = pslList.get(pslList.size()-1).getScheduleDate();
				
				// Création d'une liste de facture
				LOG.debug("Création d'une liste de facture");
				Set<Invoice> invoiceSet = new HashSet<Invoice>();
				invoiceSet.add(invoice);
				
				// Si le restant dû est inférieur ou égale au montant d'une échéance
				if(invoice.getInTaxTotalRemaining().compareTo(pslAmount) <= 0)  {
					// alors on l'étale sur une échéance
					LOG.debug("CreatePaymentSchedule - le restant dû est inférieur ou égale au montant d'une échéance");
					
					PaymentSchedule paymentSchedule = this.createPaymentSchedule(partner, company, invoiceSet, lastScheduleDate.plusMonths(1), 1);
					this.createPaymentScheduleLines(paymentSchedule);
					this.validatePaymentSchedule(paymentSchedule);
					
					LOG.debug("CreatePaymentSchedule - echéancier de paiement créé : {}", paymentSchedule);
					paymentSchedule.save();
				}
				else {
					
					// sinon on l'étale sur deux échéance
					LOG.debug("CreatePaymentSchedule - le restant dû est supérieur au montant d'une échéance");
					// La première échéance est égale au montant d'une échéance du cycle
					// La deuxième échéance est égale à : Restant dû - échéance du cycle
					
					PaymentSchedule paymentSchedule = this.createPaymentSchedule(partner, company, invoiceSet, lastScheduleDate.plusMonths(1), 2);
					this.createPaymentScheduleLines(paymentSchedule);
					
					
					if(paymentSchedule != null)  {
						
						LOG.debug("paymentSchedule : {}",paymentSchedule);
						LOG.debug("paymentSchedule.getPaymentScheduleLineList() : {}",paymentSchedule.getPaymentScheduleLineList());
						
						BigDecimal balance = invoice.getInTaxTotalRemaining().subtract(pslAmount);
						for(PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList())  {
							if(paymentScheduleLine.getScheduleLineSeq() == 1)  {
								paymentScheduleLine.setInTaxAmount(pslAmount);
								paymentScheduleLine.setAmountRemaining(pslAmount);
							}
							else  {
								paymentScheduleLine.setInTaxAmount(balance);
								paymentScheduleLine.setAmountRemaining(balance);
							}
						}
					
						LOG.debug("CreatePaymentSchedule - echéancier de paiement créé : {}", paymentSchedule);
						paymentSchedule.save();
					}
					else  {
						
						LOG.debug("Impossible de crée un échéancier de paiement ");
						throw new AxelorException(String.format("%s :\n Impossible de créer un échéancier de paiement ... ", GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
					}
					this.validatePaymentSchedule(paymentSchedule);
				}
			}
		}
		LOG.debug("End CreatePaymentSchedule"); 
	}
}
