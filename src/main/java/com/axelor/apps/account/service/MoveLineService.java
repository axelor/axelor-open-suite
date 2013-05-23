package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAccountManagement;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoiceLineVat;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Vat;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class MoveLineService {

	private static final Logger LOG = LoggerFactory.getLogger(MoveLineService.class);

	@Inject
	private Injector injector;
	
	private LocalDate toDay;
	
	@Inject
	public MoveLineService() {
		
		toDay = GeneralService.getTodayDate();
		
	}

	/**
	 * Créer une ligne d'écriture comptable
	 * 
	 * @param move
	 * @param partner
	 * @param account
	 * @param amount
	 * @param isDebit
	 * 		<code>true = débit</code>, 
	 * 		<code>false = crédit</code>
	 * @param isMinus
	 * 		<code>true = moins</code>, 
	 * 		<code>false = plus</code>
	 * @param dueDate
	 * 		Date d'échécance
	 * @param ref
	 * @param ignoreInAccountingOk
	 * 		<code>true = ignoré en compta</code>
	 * @param ignoreInReminderOk
	 * 		<code>true = ignoré en relance</code>
	 * @param fromSchedulePaymentOk
	 * 		<code>true = proviens d'un échéancier</code>
	 * 
	 * @return
	 */
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount, boolean isDebit, boolean isMinus, LocalDate date,
			LocalDate dueDate, int ref, boolean ignoreInAccountingOk, boolean ignoreInReminderOk, boolean fromSchedulePaymentOk, String descriptionOption){
		
		LOG.debug("Création d'une ligne d'écriture comptable (compte comptable : {}, montant : {}, debit ? : {}, moins ? : {}," +
			" date d'échéance : {}, référence : {}, ignoré en compta : {}, ignoré en relance : {}, issu d'un échéancier : {}", 
			new Object[]{account.getName(), amount, isDebit, isMinus, dueDate, ref, ignoreInAccountingOk, ignoreInReminderOk, fromSchedulePaymentOk});
		
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		
		MoveLine moveLine= new MoveLine();
		
		moveLine.setMove(move);
		moveLine.setPartner(partner);
		moveLine.setDate(date);
		//TODO à rétablir si date d'échéance
		moveLine.setDueDate(dueDate);
		moveLine.setAccount(account);
		moveLine.setIgnoreInReminderOk(ignoreInReminderOk);
		moveLine.setIgnoreInAccountingOk(ignoreInAccountingOk);
		moveLine.setFromSchedulePaymentOk(fromSchedulePaymentOk);
		moveLine.setCounter(Integer.toString(ref));
		moveLine.setAnalyticAccountSet(new HashSet<AnalyticAccount>());
		
		if (isMinus){
			
			if (isDebit)  {
				debit = amount.negate();
			}
			else  {
				credit = amount.negate();
			}
		}
		else {
			
			if (isDebit)  {
				debit = amount;
			}
			else  {
				credit = amount;
			}
		}
		
		moveLine.setDebit(debit);
		moveLine.setCredit(credit);
		
		moveLine.setDescription(this.determineDescriptionMoveLine(move.getJournal(), descriptionOption));
		
		return moveLine;
	}

	/**
	 * Créer une ligne d'écriture comptable
	 * 
	 * @param move
	 * @param partner
	 * @param account
	 * @param amount
	 * @param isDebit
	 * 		<code>true = débit</code>, 
	 * 		<code>false = crédit</code>
	 * @param isMinus
	 * 		<code>true = moins</code>, 
	 * 		<code>false = plus</code>
	 * @param dueDate
	 * 		Date d'échécance
	 * @param ref
	 * @param ignoreInAccountingOk
	 * 		<code>true = ignoré en compta</code>
	 * @param ignoreInReminderOk
	 * 		<code>true = ignoré en relance</code>
	 * @param fromSchedulePaymentOk
	 * 		<code>true = proviens d'un échéancier</code>
	 * 
	 * @return
	 */
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount, boolean isDebit, boolean isMinus,
			LocalDate dueDate, int ref, boolean ignoreInAccountingOk, boolean ignoreInReminderOk, boolean fromSchedulePaymentOk, String descriptionOption){
				
		return this.createMoveLine(move, partner, account, amount, isDebit, isMinus, toDay, dueDate, ref, ignoreInAccountingOk, ignoreInReminderOk, fromSchedulePaymentOk, descriptionOption);
	}

	/**
	 * Créer une ligne d'écriture comptable
	 * 
	 * @param move
	 * @param partner
	 * @param account
	 * @param amount
	 * 		
	 * @param dueDate
	 * 		Date d'échécance
	 * @param ref
	 * @param ignoreInAccountingOk
	 * 		<code>true = ignoré en compta</code>
	 * @param ignoreInReminderOk
	 * 		<code>true = ignoré en relance</code>
	 * @param fromSchedulePaymentOk
	 * 		<code>true = proviens d'un échéancier</code>
	 * 
	 * @return
	 */
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount, LocalDate date,
			LocalDate dueDate, int ref, boolean ignoreInAccountingOk, boolean ignoreInReminderOk, boolean fromSchedulePaymentOk, String descriptionOption){
		
		boolean isDebit = false;
		boolean isMinus = false;
		
		if (amount.compareTo(BigDecimal.ZERO) == -1){
			isDebit = true;
			isMinus = true;
		}
		
		return this.createMoveLine(move, partner, account, amount, isDebit, isMinus, date, dueDate, ref, ignoreInAccountingOk, ignoreInReminderOk, fromSchedulePaymentOk, descriptionOption);
	}

	/**
	 * Créer une ligne d'écriture comptable
	 * 
	 * @param move
	 * @param partner
	 * @param account
	 * @param amount
	 * 		
	 * @param dueDate
	 * 		Date d'échécance
	 * @param ref
	 * @param ignoreInAccountingOk
	 * 		<code>true = ignoré en compta</code>
	 * @param ignoreInReminderOk
	 * 		<code>true = ignoré en relance</code>
	 * @param fromSchedulePaymentOk
	 * 		<code>true = proviens d'un échéancier</code>
	 * 
	 * @return
	 */
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount,
			LocalDate dueDate, int ref, boolean ignoreInAccountingOk, boolean ignoreInReminderOk, boolean fromSchedulePaymentOk, String descriptionOption){
		
		return this.createMoveLine(move, partner, account, amount, toDay, dueDate, ref, ignoreInAccountingOk, ignoreInReminderOk, fromSchedulePaymentOk, descriptionOption);
	}

	/**
	 * Créer une ligne d'écriture comptable
	 * 
	 * @param move
	 * @param partner
	 * @param account
	 * @param amount
	 * 		
	 * @param dueDate
	 * 		Date d'échécance
	 * @param ref
	 * @param isPurchase
	 * 		<code>true = provient d'une facture d'achat</code>
	 * 
	 * @return
	 */
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount, LocalDate date, 
			LocalDate dueDate, int ref, boolean isPurchase, String descriptionOption){
		
		boolean isDebit = true;
		boolean isMinus = false;
		
		if (isPurchase){
			isDebit = false;
			isMinus = true;
			if (amount.compareTo(BigDecimal.ZERO) == -1){
				
				isDebit = true;
				isMinus = false;
				
			}	
		}
		else {
			if (amount.compareTo(BigDecimal.ZERO) == -1){
				isDebit = false;
				isMinus = true;
			}
		}
		
		return this.createMoveLine(move, partner, account, amount, isDebit, isMinus, date, dueDate, ref, false, false, false, descriptionOption);
	}

	/**
	 * Créer une ligne d'écriture comptable
	 * 
	 * @param move
	 * @param partner
	 * @param account
	 * @param amount
	 * 		
	 * @param dueDate
	 * 		Date d'échécance
	 * @param ref
	 * @param isPurchase
	 * 		<code>true = proviens d'une facture d'achat</code>
	 * 
	 * @return
	 */
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount,
			LocalDate dueDate, int ref, boolean isPurchase, String descriptionOption){
		
		return this.createMoveLine(move, partner, account, amount, toDay, dueDate, ref, isPurchase, descriptionOption);
	}
	
	
	/**
	 * Créer les lignes d'écritures comptables d'une facture.
	 * 
	 * @param invoice
	 * @param move
	 * @param consolidate
	 * @return
	 */
	public List<MoveLine> createMoveLines(Invoice invoice, Move move, Company company, Partner partner, Account account, boolean consolidate, boolean isPurchase, boolean isDebitCustomer, boolean isMinus) throws AxelorException{

		LOG.debug("Création des lignes d'écriture comptable de la facture/l'avoir {}", invoice.getInvoiceId());
		
		Account account2 = account;
		
		AccountManagementService accountManagementService = injector.getInstance(AccountManagementService.class);
		
		VatAccountService vatAccountService = injector.getInstance(VatAccountService.class);
		
		List<MoveLine> moveLines = new ArrayList<MoveLine>();
		
		AccountManagement accountManagement = null;
		Set<AnalyticAccount> analyticAccounts = new HashSet<AnalyticAccount>();
		BigDecimal exTaxTotal = null;
		
		int moveLineId = 1;
		
		if (partner == null)  {
			throw new AxelorException(String.format("Tiers absent de la facture %s", invoice.getInvoiceId()), IException.MISSING_FIELD);
		}
		if (account2 == null)  {
			throw new AxelorException(String.format("Compte tiers absent de la facture %s", invoice.getInvoiceId()), IException.MISSING_FIELD);
		}
		
		moveLines.add( this.createMoveLine(move, partner, account2, invoice.getInTaxTotal(), isDebitCustomer, isMinus, invoice.getInvoiceDate(), invoice.getDueDate(), moveLineId, false, false, false, invoice.getInvoiceId()));
		
		// Traitement des lignes de facture
		for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()){

			analyticAccounts.clear();
			
			if(invoiceLine.getProduct() == null)  {
				throw new AxelorException(String.format("Produit absent de la ligne de facture, facture : %s (société : %s)", invoice.getInvoiceId(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			accountManagement = accountManagementService.getAccountManagement(invoiceLine.getProduct(), company);
			
			if (accountManagement == null)  {
				throw new AxelorException(String.format("Configuration comptable absente du produit : %s (société : %s)", invoiceLine.getProduct().getName(), company.getName()), IException.CONFIGURATION_ERROR);
			}
				
			account2 = accountManagementService.getProductAccount(accountManagement, isPurchase);
			
			for (AnalyticAccountManagement analyticAccountManagement : accountManagement.getAnalyticAccountManagementList()){
				if(analyticAccountManagement.getAnalyticAccount() == null){
					throw new AxelorException(String.format("Le compte analytique %s associé au compte comptable de vente pour le produit %s n'est pas configuré: (société : %s)", analyticAccountManagement.getAnalyticAxis().getName(),invoiceLine.getProductName(), company.getName()), IException.CONFIGURATION_ERROR);
				}
				else{
					analyticAccounts.add(analyticAccountManagement.getAnalyticAccount());
				}
			}
			
			exTaxTotal = invoiceLine.getAccountingExTaxTotal();
			
			LOG.debug("Traitement de la ligne de facture : compte comptable = {}, montant = {}", new Object[]{account2.getName(), exTaxTotal});
			
			MoveLine moveLine = this.createMoveLine(move, partner, account2, exTaxTotal, !isDebitCustomer, isMinus, invoice.getInvoiceDate(), null, moveLineId++, false, false, false, invoice.getInvoiceId());
			moveLine.getAnalyticAccountSet().addAll(analyticAccounts);
			moveLine.setVatLine(invoiceLine.getVatLine());
			
			moveLines.add(moveLine);
			
		}
		
		// Traitement des lignes de taxes
		for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()){
			
			account2 = accountManagementService.getAccount(invoiceLineTax.getTax(), company, isPurchase);
			exTaxTotal = invoiceLineTax.getAccountingExTaxTotal();
			
			if (account2 == null)  {
				throw new AxelorException(String.format("Compte comptable absent de la taxe : %s (société : %s)", invoiceLineTax.getTax().getName(), company.getName()), IException.CONFIGURATION_ERROR);
			}
				
			LOG.debug("Traitement de la ligne de tax : compte comptable = {}, montant = {}", new Object[]{account2.getName(), exTaxTotal});
			
			MoveLine moveLine = this.createMoveLine(move, partner, account2, exTaxTotal, !isDebitCustomer, isMinus, invoice.getInvoiceDate(), null, moveLineId++, false, false, false, invoice.getInvoiceId());
			moveLine.setVatLine(invoiceLineTax.getVatLine());
			
			moveLines.add(moveLine);
			
		}
		
		// Traitement des lignes de tva
		for (InvoiceLineVat invoiceLineVat : invoice.getInvoiceLineVatList()){
			
			Vat vat = invoiceLineVat.getVatLine().getVat();
			
			account2 = vatAccountService.getAccount(vat, company);
			
			exTaxTotal = invoiceLineVat.getAccountingVatTotal();
			
			if (account2 == null)  {
				throw new AxelorException(String.format("Compte comptable absent de la ligne de tva : %s (société : %s)", vat.getName(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			MoveLine moveLine = this.createMoveLine(move, partner, account2, exTaxTotal, !isDebitCustomer, isMinus, invoice.getInvoiceDate(), null, moveLineId++, false, false, false, invoice.getInvoiceId());
			moveLine.setVatLine(invoiceLineVat.getVatLine()); 
			
			moveLines.add(moveLine);
			
		}
		
		if (consolidate)  { this.consolidateMoveLines(moveLines); }
			
		return moveLines;
	}
	
	/**
	 * Consolider des lignes d'écritures par compte comptable.
	 * 
	 * @param moveLines
	 */
	public void consolidateMoveLines(List<MoveLine> moveLines){
		
		Map<List<Object>, MoveLine> map = new HashMap<List<Object>, MoveLine>();
		MoveLine consolidateMoveLine = null;
		List<Object> keys = new ArrayList<Object>();
		
		for (MoveLine moveLine : moveLines){
			
			keys.clear();
			keys.add(moveLine.getAccount());
			keys.add(moveLine.getAnalyticAccountSet());
			keys.add(moveLine.getVatLine());
			
			if (map.containsKey(keys)){
				
				consolidateMoveLine = map.get(keys);
				consolidateMoveLine.setCredit(consolidateMoveLine.getCredit().add(moveLine.getCredit()));
				consolidateMoveLine.setDebit(consolidateMoveLine.getDebit().add(moveLine.getDebit()));
				consolidateMoveLine.getAnalyticAccountSet().addAll(moveLine.getAnalyticAccountSet());
				
			}
			else {
				map.put(keys, moveLine);
			}
			
		}
		
		BigDecimal credit = null;
		BigDecimal debit = null;
		
		int moveLineId = 1;
		moveLines.clear();
		
		for (MoveLine moveLine : map.values()){
			
			credit = moveLine.getCredit();
			debit = moveLine.getDebit();
			
			if (debit.compareTo(BigDecimal.ZERO) == 1 && credit.compareTo(BigDecimal.ZERO) == 1){
				
				if (debit.compareTo(credit) == 1){
					moveLine.setDebit(debit.subtract(credit));
					moveLine.setCredit(BigDecimal.ZERO);
					moveLine.setCounter(Integer.toString(moveLineId++));
					moveLines.add(moveLine);
				}
				else if (credit.compareTo(debit) == 1){
					moveLine.setCredit(credit.subtract(debit));
					moveLine.setDebit(BigDecimal.ZERO);
					moveLine.setCounter(Integer.toString(moveLineId++));
					moveLines.add(moveLine);
				}
				
			}
			else if (debit.compareTo(BigDecimal.ZERO) == 1 || credit.compareTo(BigDecimal.ZERO) == 1){
				moveLine.setCounter(Integer.toString(moveLineId++));
				moveLines.add(moveLine);
			}
		}
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (en débit et non complétement payée sur le compte client) de la facture ou du rejet de facture
	 * @param invoice
	 * 			Une facture
	 * @param isInvoiceReject
	 * 			La facture est-elle rejetée?
	 * @return
	 */
	public MoveLine getCustomerMoveLine(Invoice invoice, boolean isInvoiceReject)  {
		if(isInvoiceReject)  {
			return invoice.getRejectMoveLine();
		}
		else  {
			return this.getDebitCustomerMoveLine(invoice);
		}
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte client) d'une écriture de facture
	 * @param invoice
	 * 			Une facture
	 * @return
	 */
	public MoveLine getCustomerMoveLine(Move move, boolean refund)  {
		if(!refund)  {
			return this.getDebitCustomerMoveLine(move);
		}
		else  {
			return this.getCreditCustomerMoveLine(move);
		}
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte client) de la facture
	 * @param invoice
	 * 			Une facture
	 * @return
	 */
	public MoveLine getCustomerMoveLine(Invoice invoice)  {
		if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) >= 0)  {
			return this.getDebitCustomerMoveLine(invoice);
		}
		else  {
			return this.getCreditCustomerMoveLine(invoice);
		}
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (au credit et non complétement lettrée sur le compte client) de la facture
	 * @param invoice
	 * 			Une facture
	 * @return
	 */
	public MoveLine getCreditCustomerMoveLine(Invoice invoice)  {
		if(invoice.getMove() != null)  {
			return this.getCreditCustomerMoveLine(invoice.getMove());
		}
		return null;
	}
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (au credit et non complétement lettrée sur le compte client) de l'écriture de facture
	 * @param move
	 * 			Une écriture de facture
	 * @return
	 */
	public MoveLine getCreditCustomerMoveLine(Move move)  {
		for(MoveLine moveLine : move.getMoveLineList())  {
			if(moveLine.getAccount().getReconcileOk() && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0 
					&& moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
				return moveLine;
			}
		}
		return null;
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (au débit et non complétement lettrée sur le compte client) de la facture
	 * @param invoice
	 * 			Une facture
	 * @return
	 */
	public MoveLine getDebitCustomerMoveLine(Invoice invoice)  {
		if(invoice.getMove() != null)  {
			return this.getDebitCustomerMoveLine(invoice.getMove());
		}
		return null;
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (au débit et non complétement lettrée sur le compte client) de l'écriture de facture
	 * @param move
	 * 			Une écriture de facture
	 * @return
	 */
	public MoveLine getDebitCustomerMoveLine(Move move)  {
		for(MoveLine moveLine : move.getMoveLineList())  {
			if(moveLine.getAccount().getReconcileOk() && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0 
					&& moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
				return moveLine;
			}
		}
		return null;
	}
	
	
	/**
	 * Fonction permettant de générér automatiquement la description des lignes d'écritures
	 * @param journal
	 * 			Le journal de l'écriture
	 * @param descriptionOption
	 * 			Le n° pièce réglée, facture, avoir ou de l'opération rejetée
	 * @return
	 */
	public String determineDescriptionMoveLine(Journal journal, String descriptionOption)  {
		String description = "";
		if(journal != null)  {
			if(journal.getDescriptionModel() != null)  {
		
				description = String.format("%s", journal.getDescriptionModel());
			}
			if(journal.getDescriptionIdentificationOk() && descriptionOption != null)  {
				description += String.format(" %s", descriptionOption);
			}
		}
		return description;
	}
	
	
	/**
	 * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur la facture liée à l'écriture
	 * @param moveLine
	 * 			Une ligne d'écriture
	 */
	@Transactional
	public void usherProcess(MoveLine moveLine)  {
		
		Invoice invoice = moveLine.getInvoice();
		if(invoice != null)  {
			if(moveLine.getUsherPassageOk())  {
				invoice.setUsherPassageOk(true);
			}
			else  {
				invoice.setUsherPassageOk(false);
			}
			invoice.save();
		}
	}
}