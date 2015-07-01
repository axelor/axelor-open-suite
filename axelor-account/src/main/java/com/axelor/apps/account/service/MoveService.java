/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.CashRegister;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveService extends MoveRepository {

	private static final Logger LOG = LoggerFactory.getLogger(MoveService.class);
	
	@Inject
	private PeriodService periodService;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private PaymentService paymentService;
	
	@Inject
	private ReconcileService reconcileService;
	
	@Inject
	private AccountCustomerService accountCustomerService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	private LocalDate toDay;
	
	@Inject
	public MoveService() {
		
		toDay = GeneralService.getTodayDate();
		
	}
	
	/**
	 * Créer une écriture comptable à la date du jour impactant la compta.
	 * 
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @return
	 * @throws AxelorException 
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, PaymentMode paymentMode) throws AxelorException{
		
		return this.createMove(journal, company, invoice, partner, toDay, paymentMode);
	}
	
	
	
	/**
	 * Créer une écriture comptable impactant la compta.
	 * 
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param dateTime
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @return
	 * @throws AxelorException 
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, LocalDate date, PaymentMode paymentMode) throws AxelorException{
		
		return this.createMove(journal, company, invoice, partner, date, paymentMode, false, false);
		
	}
	
	
	
	
	/**
	 * Créer une écriture comptable de toute pièce en passant tous les paramètres qu'il faut.
	 * 
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param dateTime
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @return
	 * @throws AxelorException 
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, LocalDate date, PaymentMode paymentMode, boolean ignoreInReminderOk, 
			boolean ignoreInAccountingOk) throws AxelorException{
		
		LOG.debug("Création d'une écriture comptable (journal : {}, société : {}", new Object[]{journal.getName(), company.getName()});
		
		Move move = new Move();

		move.setJournal(journal);
		move.setCompany(company);
		
		move.setIgnoreInReminderOk(ignoreInReminderOk);
		move.setIgnoreInAccountingOk(ignoreInAccountingOk);
		
		Period period = periodService.rightPeriod(date, company);
			
		move.setPeriod(period);
		move.setDate(date);
		move.setMoveLineList(new ArrayList<MoveLine>());
		
		if (invoice != null)  {
			move.setInvoice(invoice);
		}
		if (partner != null)  {
			move.setPartner(partner);
			move.setCurrency(partner.getCurrency());
		}	
		move.setPaymentMode(paymentMode);
		
		save(move);
		move.setReference("*"+move.getId());
		
		return move;
		
	}
	
	
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
			
			LOG.debug("Création d'une écriture comptable spécifique à la facture {} (Société : {}, Journal : {})", new Object[]{invoice.getInvoiceId(), company.getName(), journal.getCode()});
			
			move = this.createMove(journal, company, invoice, partner, invoice.getInvoiceDate(), invoice.getPaymentMode());
			
			if (move != null)  {
				
				boolean isPurchase = InvoiceToolService.isPurchase(invoice);
				
				boolean isDebitCustomer = this.isDebitCustomer(invoice);
				
				boolean consolidate = this.toDoConsolidate();
								
				move.getMoveLineList().addAll(moveLineService.createMoveLines(invoice, move, company, partner, account, consolidate, isPurchase, isDebitCustomer));
				
				save(move);

				invoice.setMove(move);
				
				invoice.setCompanyInTaxTotalRemaining(this.getInTaxTotalRemaining(invoice));
				this.validateMove(move);
				
			}
		}
		
		return move;
		
	}
	
	
	/**
	 * Créer une écriture comptable de toute pièce spécifique à une saisie paiement.
	 * 
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @param agency
	 * 		L'agence dans laquelle s'effectue le paiement
	 * @return
	 * @throws AxelorException 
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, LocalDate date, PaymentMode paymentMode, CashRegister cashRegister) throws AxelorException{
		
		Move move = this.createMove(journal, company, invoice, partner, date, paymentMode);
		move.setCashRegister(cashRegister);
		return move;
	}
	
	
	public boolean isMinus(Invoice invoice)  {
		// Si le montant est négatif, alors on doit inverser le signe du montant
		if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
			return true;
		}
		else  {
			return false;
		}
	}
	
	
	
	public boolean toDoConsolidate()  {
		return GeneralServiceAccount.IsInvoiceMoveConsolidated();
	}
	
	

	/**
	 * 
	 * @param invoice
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public boolean isDebitCustomer(Invoice invoice) throws AxelorException  {
		boolean isDebitCustomer;
		
		switch(invoice.getOperationTypeSelect())  {
		case 1:
			isDebitCustomer = false;
			break;
		case 2:
			isDebitCustomer = true;
			break;
		case 3:
			isDebitCustomer = true;
			break;
		case 4:
			isDebitCustomer = false;
			break;
		
		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_1), invoice.getInvoiceId()), IException.MISSING_FIELD);
		}	
		
		// Si le montant est négatif, alors on inverse le sens
		if(this.isMinus(invoice))  {
			isDebitCustomer = !isDebitCustomer;
		}
		
		return isDebitCustomer;
	}
	
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte client) de la facture
	 * Récupération par boucle. A privilégié si les lignes d'écriture sont déjà managées par JPA ou si le nombre de lignes
	 * d'écriture n'est pas important (< 100).
	 * @param invoice
	 * 			Une facture
	 * @return
	 * @throws AxelorException 
	 */
	public MoveLine getInvoiceCustomerMoveLineByLoop(Invoice invoice) throws AxelorException  {
		if(this.isDebitCustomer(invoice))  {
			return moveLineService.getDebitCustomerMoveLine(invoice);
		}
		else  {
			return moveLineService.getCreditCustomerMoveLine(invoice);
		}
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte client) de la facture
	 * Récupération par requête. A privilégié si les lignes d'écritures ne sont pas managées par JPA ou si le nombre
	 * d'écriture est très important (> 100)
	 * @param invoice
	 * 			Une facture
	 * @return
	 * @throws AxelorException 
	 */
	public MoveLine getInvoiceCustomerMoveLineByQuery(Invoice invoice) throws AxelorException  {
		
		if(this.isDebitCustomer(invoice))  {
			return moveLineService.all().filter("self.move = ?1 AND self.account = ?2 AND self.debit > 0 AND self.amountRemaining > 0", 
					invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
		}
		else  {
			return moveLineService.all().filter("self.move = ?1 AND self.account = ?2 AND self.credit > 0 AND self.amountRemaining > 0", 
				invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
		}
	}
	
	
//	public MoveLine getCustomerMoveLineByQuerySum(Invoice invoice) throws AxelorException  {
//		
//		if(this.isDebitCustomer(invoice))  {
//			JPA.
//			return MoveLine.all().filter("self.move = ?1 AND self.account = ?2 AND self.debit > 0 AND self.amountRemaining > 0", 
//					invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
//		}
//		else  {
//			return MoveLine.all().filter("self.move = ?1 AND self.account = ?2 AND self.credit > 0 AND self.amountRemaining > 0", 
//				invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
//		}
//	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (en débit et non complétement payée sur le compte client) de la facture ou du rejet de facture
	 * Récupération par boucle. A privilégié si les lignes d'écriture sont déjà managées par JPA ou si le nombre de lignes
	 * d'écriture n'est pas important (< 100).
	 * 
	 * @param invoice
	 * 			Une facture
	 * @param isInvoiceReject
	 * 			La facture est-elle rejetée?
	 * @return
	 * @throws AxelorException 
	 */
	public MoveLine getCustomerMoveLineByLoop(Invoice invoice) throws AxelorException  {
		if(invoice.getRejectMoveLine() != null && invoice.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
			return invoice.getRejectMoveLine();
		}
		else  {
			return this.getInvoiceCustomerMoveLineByLoop(invoice);
		}
	}
	
	
	/**
	 * Fonction permettant de récuperer la ligne d'écriture (en débit et non complétement payée sur le compte client) de la facture ou du rejet de facture
	 * Récupération par requête. A privilégié si les lignes d'écritures ne sont pas managées par JPA ou si le nombre
	 * d'écriture est très important (> 100)
	 * 
	 * @param invoice
	 * 			Une facture
	 * @param isInvoiceReject
	 * 			La facture est-elle rejetée?
	 * @return
	 * @throws AxelorException 
	 */
	public MoveLine getCustomerMoveLineByQuery(Invoice invoice) throws AxelorException  {
		if(invoice.getRejectMoveLine() != null && invoice.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
			return invoice.getRejectMoveLine();
		}
		else  {
			return this.getInvoiceCustomerMoveLineByQuery(invoice);
		}
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
			
			if(this.isDebitCustomer(invoice))  {
				
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
		
		// Récupération des dûs
		MoveLine invoiceCustomerMoveLine = this.getCustomerMoveLineByLoop(invoice);

		List<MoveLine> debitMoveLines = (List<MoveLine>) paymentService.getInvoiceDue(invoice, true); //TODO ajouter parametrage general

		if(debitMoveLines != null && debitMoveLines.size() != 0)  {
			// Si c'est le même compte sur les trop-perçus et sur la facture, alors on lettre directement
			if(this.isSameAccount(debitMoveLines, invoice.getPartnerAccount()))  {
				List<MoveLine> creditMoveLineList = new ArrayList<MoveLine>();
				creditMoveLineList.add(invoiceCustomerMoveLine);
				paymentService.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLineList);
			}
			// Sinon on créée une O.D. pour passer du compte de la facture à un autre compte sur les trop-perçus
			else  {
				this.createMoveUseDebit(invoice, debitMoveLines, invoiceCustomerMoveLine);
			}

			// Gestion du passage en 580
			reconcileService.balanceCredit(invoiceCustomerMoveLine, company, true);

			BigDecimal remainingPaidAmount = invoiceCustomerMoveLine.getAmountRemaining();
			// Si il y a un restant à payer, alors on crée un trop-perçu.
			if(remainingPaidAmount.compareTo(BigDecimal.ZERO) > 0 )  {
				this.createExcessMove(invoice, company, partner, account, remainingPaidAmount, invoiceCustomerMoveLine);
			}
			
			invoice.setCompanyInTaxTotalRemaining(this.getInTaxTotalRemaining(invoice));
		}
	
		return move;
	}
	
	
	public Account getCustomerAccount(Partner partner, Company company, boolean isSupplierAccount) throws AxelorException  {
		
		AccountingSituation accountingSituation = accountCustomerService.getAccountingSituationService().getAccountingSituation(partner, company);
		
		if(accountingSituation != null)  {
			
			if(!isSupplierAccount && accountingSituation.getCustomerAccount() != null )  {
				return accountingSituation.getCustomerAccount();
			}  
			else if(isSupplierAccount && accountingSituation.getSupplierAccount() != null)  {
				return accountingSituation.getSupplierAccount();
			}
		}
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
			
		if(isSupplierAccount)  {
			return accountConfigService.getSupplierAccount(accountConfig);
		}
		else  {
			return accountConfigService.getCustomerAccount(accountConfig);
		}
		 
	}
	
	
	public Move createMoveUseExcessPayment(Invoice invoice) throws AxelorException{

		Move move = null;
		
		Company company = invoice.getCompany();
		
		// Récupération des trop-perçus
		List<MoveLine> creditMoveLineList = (List<MoveLine>) paymentService.getExcessPayment(invoice, this.getCustomerAccount(invoice.getPartner(), company, InvoiceToolService.isPurchase(invoice)));
		
		if(creditMoveLineList != null && creditMoveLineList.size() != 0)  {
			
			Partner partner = invoice.getPartner();
			Account account = invoice.getPartnerAccount();
			MoveLine invoiceCustomerMoveLine = this.getCustomerMoveLineByLoop(invoice);
			
			Journal journal = accountConfigService.getMiscOperationJournal(accountConfigService.getAccountConfig(company));
	
			// Si c'est le même compte sur les trop-perçus et sur la facture, alors on lettre directement
			if(this.isSameAccount(creditMoveLineList, account))  {
				List<MoveLine> debitMoveLineList = new ArrayList<MoveLine>();
				debitMoveLineList.add(invoiceCustomerMoveLine);
				paymentService.useExcessPaymentOnMoveLines(debitMoveLineList, creditMoveLineList);
			}
			// Sinon on créée une O.D. pour passer du compte de la facture à un autre compte sur les trop-perçus
			else  {

				LOG.debug("Création d'une écriture comptable O.D. spécifique à l'emploie des trop-perçus {} (Société : {}, Journal : {})", new Object[]{invoice.getInvoiceId(), company.getName(), journal.getCode()});
				
				move = this.createMove(journal, company, null, partner, invoice.getInvoiceDate(), null);
				
				if (move != null){
					BigDecimal totalCreditAmount = this.getTotalCreditAmount(creditMoveLineList);
					BigDecimal amount = totalCreditAmount.min(invoiceCustomerMoveLine.getDebit()); 
					
					// Création de la ligne au crédit
					MoveLine creditMoveLine =  moveLineService.createMoveLine(move , partner, account , amount, false, toDay, 1, null);
					move.getMoveLineList().add(creditMoveLine);
					
					// Emploie des trop-perçus sur les lignes de debit qui seront créées au fil de l'eau
					paymentService.useExcessPaymentWithAmountConsolidated(creditMoveLineList, amount, move, 2, partner,
							 company, account, invoice.getInvoiceDate(), invoice.getDueDate());
						
					this.validateMove(move);
					
					//Création de la réconciliation
					Reconcile reconcile = reconcileService.createReconcile(invoiceCustomerMoveLine, creditMoveLine, amount);
					reconcileService.confirmReconcile(reconcile);
				}
			}

			invoice.setCompanyInTaxTotalRemaining(this.getInTaxTotalRemaining(invoice));
		}
		return move;
	}
	
		
	
	/**
	 * Fonction permettant de savoir si toutes les lignes d'écritures utilise le même compte que celui passé en paramètre
	 * @param moveLineList
	 * 		Une liste de lignes d'écritures
	 * @param account
	 * 		Le compte que l'on souhaite tester
	 * @return
	 */
	public boolean isSameAccount(List<MoveLine> moveLineList, Account account)  {
		for(MoveLine moveLine : moveLineList)  {
			if(!moveLine.getAccount().equals(account))  {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit 
	 * @param creditMoveLineList
	 * 			Une liste de ligne d'écriture au credit
	 * @return
	 */
	public BigDecimal getTotalCreditAmount(List<MoveLine> creditMoveLineList)  {
		BigDecimal totalCredit = BigDecimal.ZERO;
		for(MoveLine moveLine : creditMoveLineList)  {
			totalCredit = totalCredit.add(moveLine.getAmountRemaining());
		}
		return totalCredit;
	}
	
	/**
	 * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit 
	 * @param creditMoveLineList
	 * 			Une liste de ligne d'écriture au credit
	 * @return
	 */
	public BigDecimal getTotalDebitAmount(List<MoveLine> debitMoveLineList)  {
		BigDecimal totalDebit = BigDecimal.ZERO;
		for(MoveLine moveLine : debitMoveLineList)  {
			totalDebit = totalDebit.add(moveLine.getAmountRemaining());
		}
		return totalDebit;
	}
	
	
	public Move createMoveUseDebit(Invoice invoice, List<MoveLine> debitMoveLines, MoveLine invoiceCustomerMoveLine) throws AxelorException{
		Company company = invoice.getCompany();
		Partner partner = invoice.getPartner();
		Account account = invoice.getPartnerAccount();
		
		Journal journal = accountConfigService.getMiscOperationJournal(accountConfigService.getAccountConfig(company));
		
		LOG.debug("Création d'une écriture comptable O.D. spécifique à l'emploie des trop-perçus {} (Société : {}, Journal : {})", new Object[]{invoice.getInvoiceId(), company.getName(), journal.getCode()});
		
		BigDecimal remainingAmount = invoice.getInTaxTotal().abs();
		
		LOG.debug("Montant à payer avec l'avoir récupéré : {}", remainingAmount);
		
		Move oDmove = this.createMove(journal, company, null, partner, invoice.getInvoiceDate(), null);
		
		if (oDmove != null){
			BigDecimal totalDebitAmount = this.getTotalDebitAmount(debitMoveLines);
			BigDecimal amount = totalDebitAmount.min(invoiceCustomerMoveLine.getCredit()); 
			
			// Création de la ligne au débit
			MoveLine debitMoveLine =  moveLineService.createMoveLine(oDmove , partner, account , amount, true, toDay, 1, null);
			oDmove.getMoveLineList().add(debitMoveLine);
			
			// Emploie des dûs sur les lignes de credit qui seront créées au fil de l'eau
			paymentService.createExcessPaymentWithAmount(debitMoveLines, amount, oDmove, 2, partner, company, null, account, toDay);
			
			this.validateMove(oDmove);
			
			//Création de la réconciliation
			Reconcile reconcile = reconcileService.createReconcile(debitMoveLine, invoiceCustomerMoveLine, amount);
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
		
		Move excessMove = this.createMove(journal, company, refund, partner, null);
		
		MoveLine debitMoveLine = moveLineService.createMoveLine(excessMove,
				partner,
				account,
				amount,
				true,
				this.toDay,
				1,
				null);
		excessMove.getMoveLineList().add(debitMoveLine);
		
		MoveLine creditMoveLine = moveLineService.createMoveLine(excessMove,
				partner,
				account,
				amount,
				false,
				this.toDay,
				2,
				null);
		excessMove.getMoveLineList().add(creditMoveLine);
		
		this.validateMove(excessMove);
		
		//Création de la réconciliation
		Reconcile reconcile = reconcileService.createReconcile(debitMoveLine, invoiceCustomerMoveLine, amount);
		reconcileService.confirmReconcile(reconcile);
	}
	
	
	
	public MoveLine getOrignalInvoiceFromRefund(Invoice invoice)  {

		Invoice originalInvoice = invoice.getOriginalInvoice();		
		
		if(originalInvoice != null && originalInvoice.getMove() != null)  {
			for(MoveLine moveLine : originalInvoice.getMove().getMoveLineList())  {
				if(moveLine.getAccount().getReconcileOk() && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0 
						&& moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
					return moveLine;
				}
			}
		}
		
		return null;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(Move move) throws AxelorException  {

		LocalDate date = move.getDate();
		Partner partner = move.getPartner();
	
		int counter = 1;
		for(MoveLine moveLine : move.getMoveLineList())  {
			moveLine.setDate(date);
			if(moveLine.getAccount() != null && moveLine.getAccount().getReconcileOk())  {
				moveLine.setDueDate(date);   
			}
			
			moveLine.setPartner(partner);
			moveLine.setCounter(counter);
			counter++;
		}
		
		this.validateMove(move);
		save(move);
	}
	
	
	/**
	 * Valider une écriture comptable.
	 * 
	 * @param move
	 * 
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validateMove(Move move) throws AxelorException {
		
		this.validateMove(move, true);
		
	}
	
	
	
	/**
	 * Valider une écriture comptable.
	 * 
	 * @param move
	 * 
	 * @throws AxelorException
	 */
	public void validateMove(Move move, boolean updateCustomerAccount) throws AxelorException {

		LOG.debug("Validation de l'écriture comptable {}", move.getReference());

		Journal journal = move.getJournal();
		Company company = move.getCompany();
		if(journal == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_2)),IException.CONFIGURATION_ERROR);
		}
		if(company == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_3)),IException.CONFIGURATION_ERROR);
		}
		
		if(move.getPeriod() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_4)),IException.CONFIGURATION_ERROR);
		}
		
		if (journal.getSequence() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_5), 
					journal.getName()), IException.CONFIGURATION_ERROR);
		}
		
		move.setReference(sequenceService.getSequenceNumber(journal.getSequence()));
		
		this.validateEquiponderanteMove(move);
		
		save(move);
		
		if(updateCustomerAccount)  {
			this.updateCustomerAccount(move);
		}
		else  {
			this.flagPartners(move);
		}
		
		move.setValidationDate(toDay);
	
	}
	
	
	
	public void flagPartners(Move move)  {
		
		accountCustomerService.flagPartners(accountCustomerService.getPartnerOfMove(move), move.getCompany());
		
	}
	
	
	
	/**
	 * Mise à jour du compte client
	 * 
	 * @param move
	 * 
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateCustomerAccount(Move move)  {
		
		accountCustomerService.updatePartnerAccountingSituation(accountCustomerService.getPartnerOfMove(move), move.getCompany(), true, true, false);
	}
	
	
	
	/**
	 * Procédure permettant de vérifier qu'une écriture est équilibré, et la validé si c'est le cas
	 * @param move
	 * 			Une écriture
	 * @throws AxelorException
	 */
	public void validateEquiponderanteMove(Move move) throws AxelorException {

		LOG.debug("Validation de l'écriture comptable {}", move.getReference());

		if (move.getMoveLineList() != null){

			BigDecimal totalDebit = BigDecimal.ZERO;
			BigDecimal totalCredit = BigDecimal.ZERO;
			
			for (MoveLine moveLine : move.getMoveLineList()){
				
				if(moveLine.getDebit().compareTo(BigDecimal.ZERO) == 1 && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 1)  {
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_6),
							moveLine.getName()), IException.INCONSISTENCY);
				}
				
				totalDebit = totalDebit.add(moveLine.getDebit());
				totalCredit = totalCredit.add(moveLine.getCredit());
			}
			
			if (totalDebit.compareTo(totalCredit) != 0){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_7), 
						move.getReference(), totalDebit, totalCredit), IException.INCONSISTENCY);
			}
			move.setStatusSelect(STATUS_VALIDATED);
		}
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BigDecimal getInTaxTotalRemaining(Invoice invoice) throws AxelorException{
		BigDecimal inTaxTotalRemaining = BigDecimal.ZERO;
			
		LOG.debug("Update Remaining amount of invoice : {}", invoice.getInvoiceId());
		
		if(invoice!=null)  {
			
			boolean isMinus = this.isMinus(invoice);
			
			LOG.debug("Methode 1 : debut"); //TODO
			Beans.get(InvoiceRepository.class).save(invoice);
			LOG.debug("Methode 1 : milieu");
			MoveLine moveLine = this.getCustomerMoveLineByLoop(invoice);
			LOG.debug("Methode 1 : fin");
			
			LOG.debug("Methode 2 : debut");
//			MoveLine moveLine2 = this.getCustomerMoveLineByQuery(invoice);
			LOG.debug("Methode 2 : fin");
			
			if(moveLine != null)  {
				inTaxTotalRemaining = inTaxTotalRemaining.add(moveLine.getAmountRemaining());
				
				if(isMinus)  {
					inTaxTotalRemaining = inTaxTotalRemaining.negate();
				}
			}
		}
		return inTaxTotalRemaining;
	}
	
	
	/**
	 * Methode permettant de récupérer la contrepartie d'une ligne d'écriture
	 * @param moveLine
	 * 			Une ligne d'écriture
	 * @return
	 */
	public MoveLine getOppositeMoveLine(MoveLine moveLine)  {
		if(moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
			for(MoveLine oppositeMoveLine : moveLine.getMove().getMoveLineList())  {
				if(oppositeMoveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
					return oppositeMoveLine;
				}
			}
		}
		if(moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
			for(MoveLine oppositeMoveLine : moveLine.getMove().getMoveLineList())  {
				if(oppositeMoveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
					return oppositeMoveLine;
				}
			}
		}
		return null;
	}	
	
	@Transactional
	public Move generateReverse(Move move) throws AxelorException{
		Move newMove = createMove(move.getJournal(), 
								  move.getCompany(),
								  move.getInvoice(),
								  move.getPartner(), 
								  toDay, 
								  move.getPaymentMode(),
								  move.getIgnoreInReminderOk(), 
								  move.getIgnoreInAccountingOk());
		
		for(MoveLine line: move.getMoveLineList()){
			LOG.debug("Moveline {}",line);
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
																0, 
																null); 
			newMove.addMoveLineListItem(newMoveLine);
		}
		return save(newMove);
	}
	
	public boolean validateMultiple(List<? extends Move> moveList){
		boolean error = false;
		for(Move move: moveList){
			try{
				validate(move);
			}catch (Exception e){ 
				TraceBackService.trace(e);
				error = true;
			}
		}
		return error;
	}
}