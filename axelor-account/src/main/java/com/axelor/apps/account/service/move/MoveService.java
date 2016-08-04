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
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveLineService moveLineService;
	protected MoveCreateService moveCreateService;
	protected MoveValidateService moveValidateService;
	protected MoveToolService moveToolService;
	protected ReconcileService reconcileService;
	protected MoveDueService moveDueService;
	protected PaymentService paymentService;
	protected MoveExcessPaymentService moveExcessPaymentService;
	protected AccountConfigService accountConfigService;
	protected MoveRepository moveRepository;
	protected LocalDate today;

	@Inject
	public MoveService(GeneralService generalService, MoveLineService moveLineService, MoveCreateService moveCreateService, MoveValidateService moveValidateService, MoveToolService moveToolService,
			ReconcileService reconcileService, MoveDueService moveDueService, PaymentService paymentService, MoveExcessPaymentService moveExcessPaymentService, MoveRepository moveRepository, AccountConfigService accountConfigService) {

		this.moveLineService = moveLineService;
		this.moveCreateService = moveCreateService;
		this.moveValidateService = moveValidateService;
		this.moveToolService = moveToolService;
		this.reconcileService = reconcileService;
		this.moveDueService = moveDueService;
		this.paymentService = paymentService;
		this.moveExcessPaymentService = moveExcessPaymentService;
		this.moveRepository = moveRepository;
		this.accountConfigService = accountConfigService;
		
		today = generalService.getTodayDate();

	}


	public MoveLineService getMoveLineService()  { return moveLineService; }
	public MoveCreateService getMoveCreateService()  { return moveCreateService; }
	public MoveValidateService getMoveValidateService()  { return moveValidateService; }
	public MoveToolService getMoveToolService()  { return moveToolService; }
	public ReconcileService getReconcileService()  { return reconcileService; }


	/**
	 * Créer une écriture comptable propre à la facture.
	 *
	 * @param invoice
	 * @param consolidate
	 * @return
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createMove(Invoice invoice) throws AxelorException{

		Move move = null;

		if (invoice != null && invoice.getInvoiceLineList() != null) {

			Journal journal = invoice.getJournal();
			Company company = invoice.getCompany();
			Partner partner = invoice.getPartner();
			Account account = invoice.getPartnerAccount();

			log.debug("Création d'une écriture comptable spécifique à la facture {} (Société : {}, Journal : {})", new Object[]{invoice.getInvoiceId(), company.getName(), journal.getCode()});

			move = moveCreateService.createMove(journal, company, invoice, partner, invoice.getInvoiceDate(), invoice.getPaymentMode());

			if (move != null)  {

				boolean isPurchase = InvoiceToolService.isPurchase(invoice);

				boolean isDebitCustomer = moveToolService.isDebitCustomer(invoice, false);

				boolean consolidate = moveToolService.toDoConsolidate();

				move.getMoveLineList().addAll(moveLineService.createMoveLines(invoice, move, company, partner, account, consolidate, isPurchase, isDebitCustomer));

				moveRepository.save(move);

				invoice.setMove(move);

				invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
				moveValidateService.validateMove(move);

			}
		}

		return move;

	}


	/**
	 * Méthode permettant d'employer les trop-perçus
	 * 2 cas :
	 * 		- le compte des trop-perçus est le même que celui de la facture : alors on lettre directement
	 *  	- le compte n'est pas le même : on créée une O.D. de passage sur le bon compte
	 * @param invoice
	 * @return
	 * @throws AxelorException
	 *
	 *
	 *
	 */
	public Move createMoveUseExcessPaymentOrDue(Invoice invoice) throws AxelorException{

		Move move = null;

		if (invoice != null) {

			if(moveToolService.isDebitCustomer(invoice, true))  {

				// Emploie du trop perçu
				this.createMoveUseExcessPayment(invoice);

			}
			else   {

				// Emploie des dûs
				this.createMoveUseInvoiceDue(invoice);

			}
		}
		return move;
	}


	/**
	 * Méthode permettant d'employer les dûs sur l'avoir
	 * On récupère prioritairement les dûs (factures) selectionné sur l'avoir, puis les autres dûs du tiers
	 *
	 * 2 cas :
	 * 		- le compte des dûs est le même que celui de l'avoir : alors on lettre directement
	 *  	- le compte n'est pas le même : on créée une O.D. de passage sur le bon compte
	 * @param invoice
	 * @param company
	 * @param useExcessPayment
	 * @return
	 * @throws AxelorException
	 */
	public Move createMoveUseInvoiceDue(Invoice invoice) throws AxelorException{

		Company company = invoice.getCompany();
		Account account = invoice.getPartnerAccount();
		Partner partner = invoice.getPartner();

		Move move = null;

		List<MoveLine> debitMoveLines = Lists.newArrayList();
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		if(accountConfig.getAutoReconcileOnInvoice())  {		
			// Récupération des dûs
			debitMoveLines.addAll(moveDueService.getInvoiceDue(invoice, true));
		}

		if(debitMoveLines != null && debitMoveLines.size() != 0)  {
			MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);
			
			// Si c'est le même compte sur les trop-perçus et sur la facture, alors on lettre directement
			if(moveToolService.isSameAccount(debitMoveLines, invoice.getPartnerAccount()))  {
				List<MoveLine> creditMoveLineList = new ArrayList<MoveLine>();
				creditMoveLineList.add(invoiceCustomerMoveLine);
				paymentService.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLineList);
			}
			// Sinon on créée une O.D. pour passer du compte de la facture à un autre compte sur les trop-perçus
			else  {
				this.createMoveUseDebit(invoice, debitMoveLines, invoiceCustomerMoveLine);
			}

			// Gestion du passage en 580
			reconcileService.balanceCredit(invoiceCustomerMoveLine);

			BigDecimal remainingPaidAmount = invoiceCustomerMoveLine.getAmountRemaining();
			// Si il y a un restant à payer, alors on crée un trop-perçu.
			if(remainingPaidAmount.compareTo(BigDecimal.ZERO) > 0 )  {
				this.createExcessMove(invoice, company, partner, account, remainingPaidAmount, invoiceCustomerMoveLine);
			}

			invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
		}

		return move;
	}


	public void createMoveUseExcessPayment(Invoice invoice) throws AxelorException{

		Company company = invoice.getCompany();
		
		//Récupération des acomptes de la facture
		List<MoveLine> creditMoveLineList = moveExcessPaymentService.getAdvancePaymentMoveList(invoice);
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		if(accountConfig.getAutoReconcileOnInvoice())
		{
			// Récupération des trop-perçus
			creditMoveLineList.addAll(moveExcessPaymentService.getExcessPayment(invoice, moveToolService.getCustomerAccount(invoice.getPartner(), company, InvoiceToolService.isPurchase(invoice))));
		}
		if(creditMoveLineList != null && creditMoveLineList.size() != 0)  {

			Partner partner = invoice.getPartner();
			Account account = invoice.getPartnerAccount();
			MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);

			Journal journal = accountConfigService.getMiscOperationJournal(accountConfig);

			// Si c'est le même compte sur les trop-perçus et sur la facture, alors on lettre directement
			if(moveToolService.isSameAccount(creditMoveLineList, account))  {
				List<MoveLine> debitMoveLineList = new ArrayList<MoveLine>();
				debitMoveLineList.add(invoiceCustomerMoveLine);
				paymentService.useExcessPaymentOnMoveLines(debitMoveLineList, creditMoveLineList);
			}
			// Sinon on créée une O.D. pour passer du compte de la facture à un autre compte sur les trop-perçus
			else  {

				log.debug("Création d'une écriture comptable O.D. spécifique à l'emploie des trop-perçus {} (Société : {}, Journal : {})", new Object[]{invoice.getInvoiceId(), company.getName(), journal.getCode()});

				Move move = moveCreateService.createMove(journal, company, null, partner, invoice.getInvoiceDate(), null);

				if (move != null)  {
					BigDecimal totalCreditAmount = moveToolService.getTotalCreditAmount(creditMoveLineList);
					BigDecimal amount = totalCreditAmount.min(invoiceCustomerMoveLine.getDebit());

					// Création de la ligne au crédit
					MoveLine creditMoveLine =  moveLineService.createMoveLine(move , partner, account , amount, false, today, 1, null);
					move.getMoveLineList().add(creditMoveLine);

					// Emploie des trop-perçus sur les lignes de debit qui seront créées au fil de l'eau
					paymentService.useExcessPaymentWithAmountConsolidated(creditMoveLineList, amount, move, 2, partner,
							 company, account, invoice.getInvoiceDate(), invoice.getDueDate());

					moveValidateService.validateMove(move);

					//Création de la réconciliation
					Reconcile reconcile = reconcileService.createReconcile(invoiceCustomerMoveLine, creditMoveLine, amount, false);
					reconcileService.confirmReconcile(reconcile);
				}
			}

			invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
		}
	}

	
	public Move createMoveUseDebit(Invoice invoice, List<MoveLine> debitMoveLines, MoveLine invoiceCustomerMoveLine) throws AxelorException{
		Company company = invoice.getCompany();
		Partner partner = invoice.getPartner();
		Account account = invoice.getPartnerAccount();

		Journal journal = accountConfigService.getMiscOperationJournal(accountConfigService.getAccountConfig(company));

		log.debug("Création d'une écriture comptable O.D. spécifique à l'emploie des trop-perçus {} (Société : {}, Journal : {})", new Object[]{invoice.getInvoiceId(), company.getName(), journal.getCode()});

		BigDecimal remainingAmount = invoice.getInTaxTotal().abs();

		log.debug("Montant à payer avec l'avoir récupéré : {}", remainingAmount);

		Move oDmove = moveCreateService.createMove(journal, company, null, partner, invoice.getInvoiceDate(), null);

		if (oDmove != null){
			BigDecimal totalDebitAmount = moveToolService.getTotalDebitAmount(debitMoveLines);
			BigDecimal amount = totalDebitAmount.min(invoiceCustomerMoveLine.getCredit());

			// Création de la ligne au débit
			MoveLine debitMoveLine =  moveLineService.createMoveLine(oDmove , partner, account , amount, true, today, 1, null);
			oDmove.getMoveLineList().add(debitMoveLine);

			// Emploie des dûs sur les lignes de credit qui seront créées au fil de l'eau
			paymentService.createExcessPaymentWithAmount(debitMoveLines, amount, oDmove, 2, partner, company, null, account, today);

			moveValidateService.validateMove(oDmove);

			//Création de la réconciliation
			Reconcile reconcile = reconcileService.createReconcile(debitMoveLine, invoiceCustomerMoveLine, amount, false);
			reconcileService.confirmReconcile(reconcile);
		}
		return oDmove;
	}


	/**
	 * Procédure permettant de créer une écriture de trop-perçu
	 * @param company
	 * 			Une société
	 * @param partner
	 * 			Un tiers payeur
	 * @param account
	 * 			Le compte client (411 toujours)
	 * @param amount
	 * 			Le montant du trop-perçu
	 * @param invoiceCustomerMoveLine
	 * 			La ligne d'écriture client de la facture
	 * @throws AxelorException
	 */
	public void createExcessMove(Invoice refund, Company company, Partner partner, Account account, BigDecimal amount, MoveLine invoiceCustomerMoveLine) throws AxelorException  {

		Journal journal = accountConfigService.getMiscOperationJournal(accountConfigService.getAccountConfig(company));

		Move excessMove = moveCreateService.createMove(journal, company, refund, partner, null);

		MoveLine debitMoveLine = moveLineService.createMoveLine(excessMove,
				partner,
				account,
				amount,
				true,
				this.today,
				1,
				null);
		excessMove.getMoveLineList().add(debitMoveLine);

		MoveLine creditMoveLine = moveLineService.createMoveLine(excessMove,
				partner,
				account,
				amount,
				false,
				this.today,
				2,
				null);
		excessMove.getMoveLineList().add(creditMoveLine);

		moveValidateService.validateMove(excessMove);

		//Création de la réconciliation
		Reconcile reconcile = reconcileService.createReconcile(debitMoveLine, invoiceCustomerMoveLine, amount, false);
		reconcileService.confirmReconcile(reconcile);
	}


	@Transactional
	public Move generateReverse(Move move) throws AxelorException{
		Move newMove = moveCreateService.createMove(move.getJournal(),
								  move.getCompany(),
								  move.getInvoice(),
								  move.getPartner(),
								  today,
								  move.getPaymentMode(),
								  move.getIgnoreInReminderOk(),
								  move.getIgnoreInAccountingOk());

		for(MoveLine line: move.getMoveLineList()){
			log.debug("Moveline {}",line);
			Boolean isDebit = true;
			BigDecimal amount = line.getCredit();
			if(amount.compareTo(BigDecimal.ZERO) == 0){
				isDebit = false;
				amount = line.getDebit();
			}
			MoveLine newMoveLine = moveLineService.createMoveLine(newMove,
																newMove.getPartner(),
																line.getAccount(),
																amount,
																isDebit,
																null,
																line.getCounter(),
																null);
			newMove.addMoveLineListItem(newMoveLine);
		}
		return moveRepository.save(newMove);
	}

		
}