/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
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
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
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
			LocalDate dueDate, int ref, String descriptionOption){
		
		LOG.debug("Création d'une ligne d'écriture comptable (compte comptable : {}, montant : {}, debit ? : {}, moins ? : {}," +
			" date d'échéance : {}, référence : {}", 
			new Object[]{account.getName(), amount, isDebit, isMinus, dueDate, ref});
		
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		
		MoveLine moveLine= new MoveLine();
		
		moveLine.setMove(move);
		moveLine.setPartner(partner);
		
		if(partner != null)  {
			FiscalPositionService fiscalPositionService = injector.getInstance(FiscalPositionService.class);
			account = fiscalPositionService.getAccount(partner.getFiscalPosition(), account);
		}
		
		moveLine.setAccount(account);
		
		moveLine.setDate(date);
		//TODO à rétablir si date d'échéance
		moveLine.setDueDate(dueDate);
		moveLine.setCounter(ref);
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
			LocalDate dueDate, int ref, String descriptionOption){
				
		return this.createMoveLine(move, partner, account, amount, isDebit, isMinus, toDay, dueDate, ref, descriptionOption);
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
		
		TaxAccountService taxAccountService = injector.getInstance(TaxAccountService.class);
		
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
		
		moveLines.add( this.createMoveLine(move, partner, account2, invoice.getInTaxTotal(), isDebitCustomer, isMinus, invoice.getInvoiceDate(), invoice.getDueDate(), moveLineId++, invoice.getInvoiceId()));
		
		// Traitement des lignes de facture
		for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()){

			analyticAccounts.clear();
			
			Product product = invoiceLine.getProduct();
			
			if(product == null)  {
				throw new AxelorException(String.format("Produit absent de la ligne de facture, facture : %s (société : %s)", 
						invoice.getInvoiceId(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			accountManagement = accountManagementService.getAccountManagement(product, company);
			
			account2 = accountManagementService.getProductAccount(accountManagement, isPurchase);
			
			if(account2 == null)  {
				throw new AxelorException(String.format("Compte comptable absent de la configuration pour la ligne : %s (société : %s)", 
						invoiceLine.getName(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			for (AnalyticAccountManagement analyticAccountManagement : accountManagement.getAnalyticAccountManagementList()){
				if(analyticAccountManagement.getAnalyticAccount() == null){
					throw new AxelorException(String.format("Le compte analytique %s associé au compte comptable de vente pour le produit %s n'est pas configuré: (société : %s)", 
							analyticAccountManagement.getAnalyticAxis().getName(),invoiceLine.getProductName(), company.getName()), IException.CONFIGURATION_ERROR);
				}
				else{
					analyticAccounts.add(analyticAccountManagement.getAnalyticAccount());
				}
			}
			
			exTaxTotal = invoiceLine.getAccountingExTaxTotal();
			
			LOG.debug("Traitement de la ligne de facture : compte comptable = {}, montant = {}", new Object[]{account2.getName(), exTaxTotal});
			
			MoveLine moveLine = this.createMoveLine(move, partner, account2, exTaxTotal, !isDebitCustomer, isMinus, invoice.getInvoiceDate(), null, moveLineId++, invoice.getInvoiceId());
			moveLine.getAnalyticAccountSet().addAll(analyticAccounts);
			moveLine.setTaxLine(invoiceLine.getTaxLine());
			
			moveLines.add(moveLine);
			
		}
		
		// Traitement des lignes de tva
		for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()){
			
			Tax tax = invoiceLineTax.getTaxLine().getTax();
			
			account2 = taxAccountService.getAccount(tax, company);
			
			exTaxTotal = invoiceLineTax.getAccountingTaxTotal();
			
			if (account2 == null)  {
				throw new AxelorException(String.format("Compte comptable absent de la ligne de taxe : %s (société : %s)", 
						tax.getName(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			MoveLine moveLine = this.createMoveLine(move, partner, account2, exTaxTotal, !isDebitCustomer, isMinus, invoice.getInvoiceDate(), null, moveLineId++, invoice.getInvoiceId());
			moveLine.setTaxLine(invoiceLineTax.getTaxLine()); 
			
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
			keys.add(moveLine.getTaxLine());
			
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
					moveLine.setCounter(moveLineId++);
					moveLines.add(moveLine);
				}
				else if (credit.compareTo(debit) == 1){
					moveLine.setCredit(credit.subtract(debit));
					moveLine.setDebit(BigDecimal.ZERO);
					moveLine.setCounter(moveLineId++);
					moveLines.add(moveLine);
				}
				
			}
			else if (debit.compareTo(BigDecimal.ZERO) == 1 || credit.compareTo(BigDecimal.ZERO) == 1){
				moveLine.setCounter(moveLineId++);
				moveLines.add(moveLine);
			}
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