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
package com.axelor.apps.account.service.move;

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
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.FiscalPositionServiceAccountImpl;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveLineService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected AccountManagementServiceAccountImpl accountManagementService;
	protected TaxAccountService taxAccountService;
	protected FiscalPositionServiceAccountImpl fiscalPositionService;
	protected LocalDate today;
	protected AnalyticDistributionLineService analyticDistributionLineService;
	protected GeneralService generalService;
	protected AnalyticMoveLineService analyticMoveLineService;
	
	@Inject
	public MoveLineService(AccountManagementServiceAccountImpl accountManagementService, TaxAccountService taxAccountService,
			FiscalPositionServiceAccountImpl fiscalPositionService, GeneralService generalService,
			AnalyticDistributionLineService analyticDistributionLineService, AnalyticMoveLineService analyticMoveLineService) {
		this.accountManagementService = accountManagementService;
		this.taxAccountService = taxAccountService;
		this.fiscalPositionService = fiscalPositionService;
		this.analyticDistributionLineService = analyticDistributionLineService;
		this.generalService = generalService;
		this.analyticMoveLineService = analyticMoveLineService;
		today = generalService.getTodayDate();
	}
	
	
	public MoveLine computeAnalyticDistribution(MoveLine moveLine){
		List<AnalyticDistributionLine> analyticDistributionLineList = moveLine.getAnalyticDistributionLineList();
		if(analyticDistributionLineList != null && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setMoveLine(moveLine);
				analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
				analyticDistributionLine.setDate(generalService.getTodayDate());
			}
		}
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
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount, boolean isDebit, LocalDate date,
			LocalDate dueDate, int counter, String descriptionOption){

		log.debug("Création d'une ligne d'écriture comptable (compte comptable : {}, montant : {}, debit ? : {}, " +
			" date d'échéance : {}, référence : {}",  new Object[]{account.getName(), amount, isDebit, dueDate, counter});

		if(partner != null)  {
			account = fiscalPositionService.getAccount(partner.getFiscalPosition(), account);
		}

		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;

		if(amount.compareTo(BigDecimal.ZERO) == -1)  {
			isDebit = !isDebit;
			amount = amount.negate();
		}

		if(isDebit)  {
			debit = amount;
		}
		else  {
			credit = amount;
		}

		return new MoveLine(move, partner, account, date, dueDate, counter, debit, credit, this.determineDescriptionMoveLine(move.getJournal(), descriptionOption));
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
	public MoveLine createMoveLine(Move move, Partner partner, Account account, BigDecimal amount, boolean isDebit, LocalDate dueDate, int ref, String descriptionOption){

		return this.createMoveLine(move, partner, account, amount, isDebit, today, dueDate, ref, descriptionOption);
	}


	/**
	 * Créer les lignes d'écritures comptables d'une facture.
	 *
	 * @param invoice
	 * @param move
	 * @param consolidate
	 * @return
	 */
	public List<MoveLine> createMoveLines(Invoice invoice, Move move, Company company, Partner partner, Account account, boolean consolidate, boolean isPurchase, boolean isDebitCustomer) throws AxelorException{

		log.debug("Création des lignes d'écriture comptable de la facture/l'avoir {}", invoice.getInvoiceId());

		Account account2 = account;

		List<MoveLine> moveLines = new ArrayList<MoveLine>();

		AccountManagement accountManagement = null;
		Set<AnalyticAccount> analyticAccounts = new HashSet<AnalyticAccount>();

		int moveLineId = 1;

		if (partner == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.MOVE_LINE_1), IException.MISSING_FIELD, invoice.getInvoiceId());
		}
		if (account2 == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.MOVE_LINE_2), IException.MISSING_FIELD, invoice.getInvoiceId());
		}

		moveLines.add( this.createMoveLine(move, partner, account2, invoice.getCompanyInTaxTotal(), isDebitCustomer, invoice.getInvoiceDate(), invoice.getDueDate(), moveLineId++, invoice.getInvoiceId()));

		// Traitement des lignes de facture
		for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()){
			
			if(invoiceLine.getProduct() != null){
				BigDecimal exTaxTotal = invoiceLine.getCompanyExTaxTotal();
				
				if(exTaxTotal.compareTo(BigDecimal.ZERO) != 0)  {
				
					analyticAccounts.clear();
		
					Product product = invoiceLine.getProduct();
		
					if(product == null)  {
						throw new AxelorException(I18n.get(IExceptionMessage.MOVE_LINE_3),
								IException.CONFIGURATION_ERROR, invoice.getInvoiceId(), company.getName());
					}
		
					accountManagement = accountManagementService.getAccountManagement(product, company);
		
					account2 = accountManagementService.getProductAccount(accountManagement, isPurchase);
		
					if(account2 == null)  {
						throw new AxelorException(I18n.get(IExceptionMessage.MOVE_LINE_4),
								IException.CONFIGURATION_ERROR, invoiceLine.getName(), company.getName());
					}
		
					exTaxTotal = invoiceLine.getCompanyExTaxTotal();
		
					log.debug("Traitement de la ligne de facture : compte comptable = {}, montant = {}", new Object[]{account2.getName(), exTaxTotal});

				
					MoveLine moveLine = this.createMoveLine(move, partner, account2, exTaxTotal, !isDebitCustomer, invoice.getInvoiceDate(), null, moveLineId++, invoice.getInvoiceId());
					if(invoiceLine.getAnalyticDistributionLineList() != null)  {
						for (AnalyticDistributionLine analyticDistributionLineIt : invoiceLine.getAnalyticDistributionLineList()) {
							AnalyticDistributionLine analyticDistributionLine = Beans.get(AnalyticDistributionLineRepository.class).copy(analyticDistributionLineIt, false);
							moveLine.addAnalyticDistributionLineListItem(analyticDistributionLine);
						}
					}
					moveLine.setTaxLine(invoiceLine.getTaxLine());
					moveLines.add(moveLine);
				}
			}
			
		}

		// Traitement des lignes de tva
		for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()){

			BigDecimal exTaxTotal = invoiceLineTax.getCompanyTaxTotal();
			
			if(exTaxTotal.compareTo(BigDecimal.ZERO) != 0)  {
			
				Tax tax = invoiceLineTax.getTaxLine().getTax();
	
				account2 = taxAccountService.getAccount(tax, company);
	
				if (account2 == null)  {
					throw new AxelorException(I18n.get(IExceptionMessage.MOVE_LINE_6),
							IException.CONFIGURATION_ERROR, tax.getName(), company.getName() );
				}

				MoveLine moveLine = this.createMoveLine(move, partner, account2, exTaxTotal, !isDebitCustomer, invoice.getInvoiceDate(), null, moveLineId++, invoice.getInvoiceId());
				moveLine.setTaxLine(invoiceLineTax.getTaxLine());

				moveLines.add(moveLine);
			}

		}

		if (consolidate)  { this.consolidateMoveLines(moveLines); }

		return moveLines;
	}
	
	public MoveLine findConsolidateMoveLine(Map<List<Object>, MoveLine> map, MoveLine moveLine, List<Object> keys){
		if(map != null && !map.isEmpty()){
			Map<List<Object>, MoveLine> copyMap = new HashMap<List<Object>, MoveLine>(map);
			while(!copyMap.isEmpty()){
				if(map.containsKey(keys)){
					MoveLine moveLineIt =  map.get(keys);
					int count = 0;
					if(moveLineIt.getAnalyticDistributionLineList() == null && moveLine.getAnalyticDistributionLineList() == null){
						return moveLineIt;
					}
					else if(moveLineIt.getAnalyticDistributionLineList() == null || moveLine.getAnalyticDistributionLineList() == null){
						break;
					}
					List<AnalyticDistributionLine> list1 = moveLineIt.getAnalyticDistributionLineList();
					List<AnalyticDistributionLine> list2 = moveLine.getAnalyticDistributionLineList();
					List<AnalyticDistributionLine> copyList = new ArrayList<AnalyticDistributionLine>(list1);
					if(list1.size() == list2.size()){
						for (AnalyticDistributionLine analyticDistributionLine : list2) {
							for (AnalyticDistributionLine analyticDistributionLineIt : copyList) {
								if(analyticDistributionLine.getAnalyticAxis().equals(analyticDistributionLineIt.getAnalyticAxis()) &&
										analyticDistributionLine.getAnalyticAccount().equals(analyticDistributionLineIt.getAnalyticAccount()) &&
										analyticDistributionLine.getPercentage().equals(analyticDistributionLineIt.getPercentage()) &&
										((analyticDistributionLine.getAnalyticJournal() == null && analyticDistributionLineIt.getAnalyticJournal() == null)
												|| analyticDistributionLine.getAnalyticJournal().equals(analyticDistributionLineIt.getAnalyticJournal()))){
									copyList.remove(analyticDistributionLineIt);
									count++;
									break;
								}
							}
						}
						if(count == list1.size()){
							return moveLineIt;
						}
					}
				}
				else{
					return null;
				}
			}
		}
			
		return null;
	}
	

	/**
	 * Consolider des lignes d'écritures par compte comptable.
	 *
	 * @param moveLines
	 */
	public List<MoveLine> consolidateMoveLines(List<MoveLine> moveLines){

		Map<List<Object>, MoveLine> map = new HashMap<List<Object>, MoveLine>();
		MoveLine consolidateMoveLine = null;
		List<Object> keys = new ArrayList<Object>();

		for (MoveLine moveLine : moveLines){

			keys.clear();
			keys.add(moveLine.getAccount());
			keys.add(moveLine.getTaxLine());
			consolidateMoveLine = this.findConsolidateMoveLine(map, moveLine, keys);
			if (consolidateMoveLine != null){
				
				consolidateMoveLine.setCredit(consolidateMoveLine.getCredit().add(moveLine.getCredit()));
				consolidateMoveLine.setDebit(consolidateMoveLine.getDebit().add(moveLine.getDebit()));
				if(consolidateMoveLine.getAnalyticDistributionLineList() != null && !consolidateMoveLine.getAnalyticDistributionLineList().isEmpty()){
					for (AnalyticDistributionLine analyticDistributionLine : consolidateMoveLine.getAnalyticDistributionLineList()) {
						for (AnalyticDistributionLine analyticDistributionLineIt : moveLine.getAnalyticDistributionLineList()) {
							if(analyticDistributionLine.getAnalyticAxis().equals(analyticDistributionLineIt.getAnalyticAxis()) &&
									analyticDistributionLine.getAnalyticAccount().equals(analyticDistributionLineIt.getAnalyticAccount()) &&
									analyticDistributionLine.getPercentage().equals(analyticDistributionLineIt.getPercentage()) &&
									((analyticDistributionLine.getAnalyticJournal() == null && analyticDistributionLineIt.getAnalyticJournal() == null)
											|| analyticDistributionLine.getAnalyticJournal().equals(analyticDistributionLineIt.getAnalyticJournal()))){
								analyticDistributionLine.setAmount(analyticDistributionLine.getAmount().add(analyticDistributionLineIt.getAmount()));
								break;
							}
						}
					}
				}
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
		
		return moveLines;
	}
	
	public List<MoveLine> consolidateMoveLinesWithoutAnalytic(List<MoveLine> moveLines){

		Map<List<Object>, MoveLine> map = new HashMap<List<Object>, MoveLine>();
		MoveLine consolidateMoveLine = null;
		List<Object> keys = new ArrayList<Object>();

		for (MoveLine moveLine : moveLines){

			keys.clear();
			keys.add(moveLine.getAccount());
			keys.add(moveLine.getTaxLine());
			if (map.containsKey(keys)){
				consolidateMoveLine = map.get(keys);
				
				consolidateMoveLine.setCredit(consolidateMoveLine.getCredit().add(moveLine.getCredit()));
				consolidateMoveLine.setDebit(consolidateMoveLine.getDebit().add(moveLine.getDebit()));
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
		
		return moveLines;
	}
	
	public List<MoveLine> consolidateMoveLinesOnlyAnalytic(List<MoveLine> moveLines){

		Map<List<Object>, MoveLine> map = new HashMap<List<Object>, MoveLine>();
		MoveLine consolidateMoveLine = null;
		List<Object> keys = new ArrayList<Object>();

		for (MoveLine moveLine : moveLines){

			keys.clear();
			keys.add(moveLine.getAccount());
			consolidateMoveLine = this.findConsolidateMoveLine(map, moveLine, keys);
			if (consolidateMoveLine != null){
				
				consolidateMoveLine.setCredit(consolidateMoveLine.getCredit().add(moveLine.getCredit()));
				consolidateMoveLine.setDebit(consolidateMoveLine.getDebit().add(moveLine.getDebit()));
				for (AnalyticDistributionLine analyticDistributionLine : consolidateMoveLine.getAnalyticDistributionLineList()) {
					for (AnalyticDistributionLine analyticDistributionLineIt : moveLine.getAnalyticDistributionLineList()) {
						if(analyticDistributionLine.getAnalyticAxis().equals(analyticDistributionLineIt.getAnalyticAxis()) &&
								analyticDistributionLine.getAnalyticAccount().equals(analyticDistributionLineIt.getAnalyticAccount()) &&
								analyticDistributionLine.getPercentage().equals(analyticDistributionLineIt.getPercentage()) &&
								analyticDistributionLine.getAnalyticJournal().equals(analyticDistributionLineIt.getAnalyticJournal())){
							analyticDistributionLine.setAmount(analyticDistributionLine.getAmount().add(analyticDistributionLineIt.getAmount()));
							break;
						}
					}
				}
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
		
		return moveLines;
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

		Invoice invoice = moveLine.getMove().getInvoice();
		if(invoice != null)  {
			if(moveLine.getUsherPassageOk())  {
				invoice.setUsherPassageOk(true);
			}
			else  {
				invoice.setUsherPassageOk(false);
			}
			Beans.get(InvoiceRepository.class).save(invoice);
		}
	}
}