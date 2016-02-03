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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentScheduleService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected PaymentScheduleLineService paymentScheduleLineService;
	protected PaymentScheduleLineRepository paymentScheduleLineRepo;
	protected SequenceService sequenceService;
	protected PaymentScheduleRepository paymentScheduleRepo;
	protected PartnerService partnerService;

	protected LocalDate date;

	@Inject
	public PaymentScheduleService(GeneralService generalService, PaymentScheduleLineService paymentScheduleLineService, PaymentScheduleLineRepository paymentScheduleLineRepo,
			SequenceService sequenceService, PaymentScheduleRepository paymentScheduleRepo, PartnerService partnerService) {
		this.paymentScheduleLineService = paymentScheduleLineService;
		this.paymentScheduleLineRepo = paymentScheduleLineRepo;
		this.sequenceService = sequenceService;
		this.paymentScheduleRepo = paymentScheduleRepo;
		this.partnerService = partnerService;
		
		date = generalService.getTodayDate();
	}

	/**
	 * Création d'un échéancier sans ces lignes.
	 *
	 * @param partner
	 * 			Le tiers.
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
				nbrTerm, partnerService.getDefaultBankDetails(partner), partner.getPaymentMode());

		paymentSchedule.getInvoiceSet().addAll(invoices);

		return paymentSchedule;
	}

	/**
	 * Création d'un échéancier sans ces lignes.
	 *
	 * @param partner
	 * 			Le tiers.
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
		String seq = sequenceService.getSequenceNumber(IAdministration.PAYMENT_SCHEDULE, company);
		if(seq == null)  {
			throw new AxelorException(String.format("%s :\n"+
							I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_5)+" %s",
							GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
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

					log.debug("Somme TTC des lignes de l'échéancier {} : total = {}, ajout = {}", new Object[] {paymentSchedule.getScheduleId(), totalAmount, paymentScheduleLine.getInTaxAmount()});

					totalAmount = totalAmount.add(paymentScheduleLine.getInTaxAmount());
				}
			}
		}

		log.debug("Obtention de la somme TTC des lignes de l'échéancier {} : {}", new Object[] {paymentSchedule.getScheduleId(), totalAmount});

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

		log.debug("Mise à jour de l'échéancier {} : {}", new Object[] {paymentSchedule.getScheduleId(), inTaxTotal});

		for (PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList()){

			if (paymentScheduleLine.getStatusSelect() == PaymentScheduleLineRepository.STATUS_IN_PROGRESS && !paymentScheduleLine.getRejectedOk()) {

				log.debug("Mise à jour de la ligne {} ", paymentScheduleLine.getName());

				paymentScheduleLine.setInTaxAmount(inTaxTotal);
			}
		}

		paymentScheduleRepo.save(paymentSchedule);

	}

	/**
	 * Création d'un échéancier avec ces lignes.
	 *
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
	 *
	 * @return
	 * 			L'échéancier créé.
	 * @throws AxelorException
	 */
	public PaymentSchedule createPaymentSchedule(Partner partner, Company company, LocalDate date, LocalDate firstTermDate, BigDecimal initialInTaxAmount, int nbrTerm, BankDetails bankDetails, PaymentMode paymentMode) throws AxelorException{

		Invoice invoice = null;
		PaymentSchedule paymentSchedule = this.createPaymentSchedule(partner, invoice, company, date, firstTermDate, nbrTerm, bankDetails, paymentMode);

		paymentSchedule.setPaymentScheduleLineList(new ArrayList<PaymentScheduleLine>());

		for (int term = 1; term < nbrTerm + 1; term++){
			paymentSchedule.getPaymentScheduleLineList().add(paymentScheduleLineService.createPaymentScheduleLine(paymentSchedule, initialInTaxAmount, term, firstTermDate.plusMonths(term-1)));
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
		log.debug("In getPaymentSchedulerMoveLineToPay ....");
		List<MoveLine> moveLines = new ArrayList<MoveLine>();
		for (Invoice invoice : paymentSchedule.getInvoiceSet())  {
			if (invoice.getCompanyInTaxTotalRemaining().compareTo(BigDecimal.ZERO) > 0 && invoice.getMove() != null && invoice.getMove().getMoveLineList() != null)  {
				for (MoveLine moveLine : invoice.getMove().getMoveLineList()){
					if (moveLine.getAccount().getReconcileOk() && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0 && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0){
						moveLines.add(moveLine);
					}
				}
			}
		}
		log.debug("End getPaymentSchedulerMoveLineToPay.");
		return moveLines;
	}


	/**
	 * Permet de valider un échéancier.
     *
	 * @param paymentSchedule
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validatePaymentSchedule(PaymentSchedule paymentSchedule) throws AxelorException {

		log.debug("Validation de l'échéancier {}", paymentSchedule.getScheduleId());

		if(paymentSchedule.getPaymentScheduleLineList() == null || paymentSchedule.getPaymentScheduleLineList().size() == 0)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_6),
					GeneralServiceImpl.EXCEPTION, paymentSchedule.getScheduleId()), IException.INCONSISTENCY);
		}

//		this.updateInvoices(paymentSchedule); //TODO

		paymentSchedule.setStatusSelect(PaymentScheduleRepository.STATUS_CONFIRMED);

		paymentScheduleRepo.save(paymentSchedule);
	}


	public void updateInvoices(PaymentSchedule paymentSchedule)  {

		if (paymentSchedule.getInvoiceSet() != null){

			List<MoveLine> moveLineInvoiceToPay = this.getPaymentSchedulerMoveLineToPay(paymentSchedule);

			for (MoveLine moveLineInvoice : moveLineInvoiceToPay){

				moveLineInvoice.getMove().setIgnoreInReminderOk(true);
				this.updateInvoice(moveLineInvoice.getMove().getInvoice(), paymentSchedule);

			}
		}
	}


	public void updateInvoice(Invoice invoice, PaymentSchedule paymentSchedule)  {

		invoice.setSchedulePaymentOk(true);
		invoice.setPaymentSchedule(paymentSchedule);
	}



	/**
	 * Methode qui annule un échéancier
	 *
	 * @param paymentSchedule
	 */
	public void cancelPaymentSchedule(PaymentSchedule paymentSchedule){

		// L'échéancier est passé à annulé
		paymentSchedule.setStatusSelect(PaymentScheduleRepository.STATUS_CANCELED);

		for(PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList())  {

			// Si l'échéance n'est pas complètement payée
			if(paymentScheduleLine.getInTaxAmountPaid().compareTo(paymentScheduleLine.getInTaxAmount()) != 0 ) {

				// L'échéance est passée à cloturé
				paymentScheduleLine.setStatusSelect(PaymentScheduleLineRepository.STATUS_CLOSED);
			}
		}

		for(Invoice invoice : paymentSchedule.getInvoiceSet())  {
			// L'échéancier n'est plus selectionné sur la facture
			invoice.setPaymentSchedule(null);

			// L'échéancier est assigné dans un nouveau champs afin de garder un lien invisble pour l'utilisateur, mais utilisé pour le passage en irrécouvrable
			invoice.setCanceledPaymentSchedule(paymentSchedule);
			invoice.setSchedulePaymentOk(false);
		}

	}


	/**
	 * Methode permettant de savoir si l'échéance passée en paramètre est la dernière de l'échéancier
	 * @param paymentScheduleLine
	 * @return
	 */
	public boolean isLastSchedule(PaymentScheduleLine paymentScheduleLine)  {
		if(paymentScheduleLine != null)  {
			if(paymentScheduleLineRepo.all().filter("self.paymentSchedule = ?1 and self.scheduleDate > ?2 and self.statusSelect = ?3",
					paymentScheduleLine.getPaymentSchedule(), paymentScheduleLine.getScheduleDate(), PaymentScheduleLineRepository.STATUS_IN_PROGRESS).fetchOne() == null)  {
				log.debug("Dernière échéance");
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

  		log.debug("Cloture de l'échéancier");

		//On récupère un statut cloturé, afin de pouvoir changer l'état des lignes d'échéanciers

		for(PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList())  {
			paymentScheduleLine.setStatusSelect(PaymentScheduleLineRepository.STATUS_CLOSED);
		}
		paymentSchedule.setStatusSelect(PaymentScheduleRepository.STATUS_CLOSED);

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
	 * Créer des lignes d'échéancier à partir des lignes de factures de celui-ci.
	 *
	 * @param paymentSchedule
	 * @throws AxelorException
	 */
	@Transactional
	public void createPaymentScheduleLines(PaymentSchedule paymentSchedule){

		this.initCollection(paymentSchedule);

		paymentSchedule.getPaymentScheduleLineList().addAll(paymentScheduleLineService.createPaymentScheduleLines(paymentSchedule));
		paymentScheduleRepo.save(paymentSchedule);

	}



	public void initCollection(PaymentSchedule paymentSchedule)  {

		if (paymentSchedule.getPaymentScheduleLineList() == null)  {
			paymentSchedule.setPaymentScheduleLineList(new ArrayList<PaymentScheduleLine>());
		}
		else  {
			paymentSchedule.getPaymentScheduleLineList().clear();
		}

	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void toCancelPaymentSchedule(PaymentSchedule paymentSchedule){
		this.cancelPaymentSchedule(paymentSchedule);
		paymentScheduleRepo.save(paymentSchedule);
	}

}
