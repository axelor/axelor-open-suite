package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReconcileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReconcileService.class); 
	
	@Inject
	private MoveService ms;
	
	@Inject
	private MoveLineService mls;
	
	@Inject
	private AccountCustomerService acs;
	
	private LocalDate today;

	@Inject
	public ReconcileService() {
		
		this.today = GeneralService.getTodayDate();
		
	}
	
	/**
	 * Permet de déréconcilier
	 * @param reconcile
	 * 			Une reconciliation
	 * @return
	 * 			L'etat de la réconciliation
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String unreconcile(Reconcile reconcile) throws AxelorException  {
		
		LOG.debug("In unreconcile ....");
		LOG.debug("Credit .... {} "+reconcile.getLineCredit().getAmountPaid());
		LOG.debug("Debit .... {}"+reconcile.getLineDebit().getAmountPaid());
		
		if (reconcile.getState().equals("2"))  {
			if (reconcile.getLineCredit() != null && reconcile.getLineDebit() != null)  {						
					// Change the state
					reconcile.setState("3");
					//Add the reconciled amount to the reconciled amount in the move line
					reconcile.getLineCredit().setAmountPaid(reconcile.getLineCredit().getAmountPaid().subtract(reconcile.getAmount()));
					reconcile.getLineDebit().setAmountPaid(reconcile.getLineDebit().getAmountPaid().subtract(reconcile.getAmount()));		
					
					// Update amount remaining on invoice or refund
					if(reconcile.getLineDebit().getMove().getInvoice() != null)  {
						reconcile.getLineDebit().getMove().getInvoice().setInTaxTotalRemaining(
								ms.getInTaxTotalRemaining(reconcile.getLineDebit().getMove().getInvoice(), reconcile.getLineDebit().getMove().getInvoice().getPartnerAccount()));
					}
					if(reconcile.getLineCredit().getMove().getInvoice() != null)  {
						reconcile.getLineCredit().getMove().getInvoice().setInTaxTotalRemaining(
								ms.getInTaxTotalRemaining(reconcile.getLineCredit().getMove().getInvoice(), reconcile.getLineCredit().getMove().getInvoice().getPartnerAccount()));
					}
					
					reconcile.save();
					LOG.debug("End Unreconcile.");
					return reconcile.getState();
			}
			else  {
				throw new AxelorException(
						String.format("%s :\nReconciliation %s: Merci de renseigner les lignes d'écritures concernées.",
								GeneralService.getExceptionAccountingMsg(), reconcile.getFullName()), IException.CONFIGURATION_ERROR);
			}
		}
		else  {
			throw new AxelorException(
					String.format("%s :\nReconciliation %s: Vous ne pouvez pas délétrer en dehors de l'état 'Confirmée'.",
							GeneralService.getExceptionAccountingMsg(), reconcile.getFullName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	/**
	 * Permet de créer une réconciliation en passant les paramètres qu'il faut
	 * @param lineDebit
	 * 			Une ligne d'écriture au débit
	 * @param lineCredit
	 * 			Une ligne d'écriture au crédit
	 * @param amount
	 * 			Le montant à reconciler
	 * @param canBeZeroBalanceOk
	 * 			Peut être soldé?
	 * @return
	 * 			Une reconciliation
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Reconcile createGenericReconcile(MoveLine lineDebit, MoveLine lineCredit, BigDecimal amount, boolean canBeZeroBalanceOk, boolean mustBeZeroBalance, boolean inverse){
		
		LOG.debug("In createReconcile ....");
		
		Reconcile reconcile= new Reconcile();
		LOG.debug("In createReconcile .... : Amount : {}", amount);
		reconcile.setAmount(amount.setScale(2, RoundingMode.HALF_EVEN));
		LOG.debug("In createReconcile .... : Amount rounded : {}", reconcile.getAmount());
		
		if(inverse)  {
			reconcile.setLineDebit(lineCredit);
			reconcile.setLineCredit(lineDebit);
		}
		else  {
			reconcile.setLineDebit(lineDebit);
			reconcile.setLineCredit(lineCredit);
		}
		
		reconcile.setCanBeZeroBalanceOk(canBeZeroBalanceOk);
		reconcile.setMustBeZeroBalanceOk(mustBeZeroBalance);
		
		reconcile.save();
		
		LOG.debug("End createReconcile.");
		
		return reconcile;
	}
	
	
	/**
	 * Permet de créer une réconciliation en passant les paramètres qu'il faut
	 * @param lineDebit
	 * 			Une ligne d'écriture au débit
	 * @param lineCredit
	 * 			Une ligne d'écriture au crédit
	 * @param amount
	 * 			Le montant à reconciler
	 * @return
	 * 			Une reconciliation
	 */
	public Reconcile createReconcile(MoveLine lineDebit, MoveLine lineCredit, BigDecimal amount){
		return createGenericReconcile(lineDebit, lineCredit, amount, false, false, false);
	}
	

	/**
	 * Permet de créer une réconciliation en passant les paramètres qu'il faut
	 * @param lineDebit
	 * 			Une ligne d'écriture au débit
	 * @param lineCredit
	 * 			Une ligne d'écriture au crédit
	 * @param amount
	 * 			Le montant à reconciler
	 * @return
	 * 			Une reconciliation
	 */
	public Reconcile createReconcile(MoveLine lineDebit, MoveLine lineCredit, BigDecimal amount, boolean inverse){
		
		return createGenericReconcile(lineCredit, lineDebit, amount, false, false, inverse);
		
	}
	
	
	
	/**
	 * Permet de confirmer une  réconciliation
	 * On ne peut réconcilier que des moveLine ayant le même compte
	 * @param reconcile
	 * 			Une reconciliation
	 * @return
	 * 			L'etat de la reconciliation
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String confirmReconcile(Reconcile reconcile) throws AxelorException  {
		
		LOG.debug("In confirmReconcile ....");
		if (reconcile != null)  {
			if (reconcile.getLineCredit() != null && reconcile.getLineDebit() != null)  {
				// Check if move lines accounts are the same (debit and credit)
				LOG.debug("Compte ligne de credit : {} , Compte ligne de debit : {}",reconcile.getLineCredit().getAccount(),reconcile.getLineDebit().getAccount());
				if (!reconcile.getLineCredit().getAccount().equals(reconcile.getLineDebit().getAccount())){
					throw new AxelorException(String.format("%s :\nReconciliation %s: Les lignes d'écritures sélectionnées doivent concerner le même compte comptable.", 
							GeneralService.getExceptionAccountingMsg(), reconcile.getFullName()), IException.CONFIGURATION_ERROR);		
				}
				// Check if the amount to reconcile is != zero
				else if (reconcile.getAmount() == null || reconcile.getAmount().compareTo(BigDecimal.ZERO) == 0)  {
					throw new AxelorException(String.format("%s :\nReconciliation %s: Le montant réconcilié doit être différent de zéro.", 
							GeneralService.getExceptionAccountingMsg(), reconcile.getFullName()), IException.INCONSISTENCY);				
							
				}
				else{
					// Check if the amount to reconcile is less than the 2 moves credit and debit
					LOG.debug("AMOUNT  : : : {}",reconcile.getAmount());
					LOG.debug("credit - paid  : : : {}",reconcile.getLineCredit().getCredit().subtract(reconcile.getLineCredit().getAmountPaid()));
					LOG.debug("debit - paid  : : : {}",reconcile.getLineDebit().getDebit().subtract(reconcile.getLineDebit().getAmountPaid()));
				
					if ((reconcile.getAmount().compareTo(reconcile.getLineCredit().getCredit().subtract(reconcile.getLineCredit().getAmountPaid())) > 0 
							|| (reconcile.getAmount().compareTo(reconcile.getLineDebit().getDebit().subtract(reconcile.getLineDebit().getAmountPaid())) > 0))){
						throw new AxelorException(String.format("%s :\nReconciliation %s: Le montant réconcilié doit être inférieur ou égale au montant restant à réconcilier des lignes d'écritures.", 
								GeneralService.getExceptionAccountingMsg(), reconcile.getFullName()), IException.INCONSISTENCY);						
								
					}
					else{
						
						//Add the reconciled amount to the reconciled amount in the move line
						reconcile.getLineCredit().setAmountPaid(reconcile.getLineCredit().getAmountPaid().add(reconcile.getAmount()));
						reconcile.getLineDebit().setAmountPaid(reconcile.getLineDebit().getAmountPaid().add(reconcile.getAmount()));
						
						// Update amount remaining on invoice or refund
						if(reconcile.getLineDebit().getMove().getInvoice() != null)  {
							reconcile.getLineDebit().getMove().getInvoice().setInTaxTotalRemaining(
									ms.getInTaxTotalRemaining(reconcile.getLineDebit().getMove().getInvoice(), reconcile.getLineDebit().getMove().getInvoice().getPartnerAccount()));
						}
						if(reconcile.getLineCredit().getMove().getInvoice() != null)  {
							reconcile.getLineCredit().getMove().getInvoice().setInTaxTotalRemaining(
									ms.getInTaxTotalRemaining(reconcile.getLineCredit().getMove().getInvoice(), reconcile.getLineCredit().getMove().getInvoice().getPartnerAccount()));
						}
						
						acs.updatePartnerAccountingSituation(reconcile);
						
						// Change the state
						reconcile.setState("2");
						
						if(reconcile.getCanBeZeroBalanceOk())  {
							// Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte transitoire si le seuil est respecté
							canBeZeroBalance(reconcile);
						}
						
						reconcile.save();
						
						LOG.debug("End confirmReconcile.");
						return reconcile.getState();
					}
				}
			}
			else{
				throw new AxelorException(String.format("%s :\nReconciliation %s: Merci de renseigner les lignes d'écritures concernées.", 
						GeneralService.getExceptionAccountingMsg(), reconcile.getFullName()), IException.CONFIGURATION_ERROR);					
			}
		}
		else{
			LOG.debug("***************** ****** *** NO ID *** ****** **********************");
			return null;
		}
	}
	
	
	/**
	 * Procédure permettant de récupérer l'écriture d'avoir d'un trop-perçu généré par un avoir
	 * @param moveLine
	 * 			un trop-perçu
	 * @return
	 */
	public Move getRefundMove (MoveLine moveLine)  {
		Move move = moveLine.getMove();
		if(move.getJournal().equals(move.getCompany().getTechnicalJournal()))  {
			MoveLine oppositeMoveLine = ms.getOppositeMoveLine(moveLine);
			if(oppositeMoveLine.getReconcileList1() != null && oppositeMoveLine.getReconcileList1().size() == 1)  {
				return oppositeMoveLine.getReconcileList1().get(0).getLineCredit().getMove();
			}
		}
		return null;
	}
	
	
	/**
	 * Méthode permettant de lettrer une écriture au débit avec une écriture au crédit
	 * @param debitMoveLine
	 * @param creditMoveLine
	 * @throws AxelorException
	 */
	public void reconcile(MoveLine debitMoveLine, MoveLine creditMoveLine) throws AxelorException  {
		
		BigDecimal amount = debitMoveLine.getAmountRemaining().min(creditMoveLine.getAmountRemaining());
		Reconcile reconcile = this.createReconcile(debitMoveLine, creditMoveLine, amount);
		this.confirmReconcile(reconcile);
		
	}
	
	
	
	/**
	 * Procédure permettant de gérer les écarts de règlement, check sur la case à cocher 'Peut être soldé'
	 *  Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte transitoire si le seuil est respecté
	 * @param reconcile
	 * 			Une reconciliation
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void canBeZeroBalance(Reconcile reconcile) throws AxelorException  {
		
		MoveLine debitMoveLine = reconcile.getLineDebit();
		
		BigDecimal debitAmountRemaining = debitMoveLine.getAmountRemaining();
		LOG.debug("Montant à payer / à lettrer au débit : {}", debitAmountRemaining);
		if(debitAmountRemaining.compareTo(BigDecimal.ZERO) > 0)  {
			Company company = reconcile.getLineDebit().getMove().getCompany();
			if(debitAmountRemaining.plus().compareTo(company.getThresholdDistanceFromRegulation()) < 0 || reconcile.getMustBeZeroBalanceOk())  {
				
				LOG.debug("Seuil respecté");
				
				Partner partner = debitMoveLine.getPartner();
				Account account = debitMoveLine.getAccount();
				
				if(company.getMiscOperationJournal() == null)  {
					throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des O.D. pour la société %s",
							GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
				}
				
				Move newMove = ms.createMove(company.getMiscOperationJournal(), company, null, partner, null, false);
				
				// Création de la ligne au crédit
				MoveLine newCreditMoveLine = mls.createMoveLine(newMove, partner, account, debitAmountRemaining, false, false, 
						today, 1, false, false, false, null);
				
				// Création de la ligne au debit
				MoveLine newDebitMoveLine = mls.createMoveLine(newMove, partner, company.getCashPositionVariationAccount(), debitAmountRemaining, true, false, 
						today, 2, false, false, false, null);
				
				newMove.getMoveLineList().add(newCreditMoveLine);
				newMove.getMoveLineList().add(newDebitMoveLine);
				ms.validateMove(newMove);
				newMove.save();
				
				//Création de la réconciliation
				Reconcile newReconcile = this.createReconcile(debitMoveLine, newCreditMoveLine, debitAmountRemaining);
				this.confirmReconcile(newReconcile);
				newReconcile.save();
			}
		}
		
		reconcile.setCanBeZeroBalanceOk(false);
		LOG.debug("Fin de la gestion des écarts de règlement");
	}
		
	
	/**
	 * Solder le trop-perçu si il respect les règles de seuil
	 * @param creditMoveLine
	 * @param company
	 * @throws AxelorException
	 */
	public void balanceCredit(MoveLine creditMoveLine, Company company) throws AxelorException  {
		if(creditMoveLine != null)  {
			BigDecimal creditAmountRemaining = creditMoveLine.getAmountRemaining();
			LOG.debug("Montant à payer / à lettrer au crédit : {}", creditAmountRemaining);

			if(creditAmountRemaining.compareTo(BigDecimal.ZERO) > 0)  {
				if(creditAmountRemaining.plus().compareTo(company.getThresholdDistanceFromRegulation()) < 0)  {

					LOG.debug("Seuil respecté");

					Partner partner = creditMoveLine.getPartner();
					Account account = creditMoveLine.getAccount();
					
					if(company.getMiscOperationJournal() == null)  {
						throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des O.D. pour la société %s",
								GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
					}
					
					Move newMove = ms.createMove(company.getMiscOperationJournal(), company, null, partner, null, false);
					
					
					// Création de la ligne au crédit
					MoveLine newCreditMoveLine = mls.createMoveLine(newMove, partner, company.getCashPositionVariationAccount(), creditAmountRemaining, false, false, 
							today, 2, false, false, false, null);
					
					// Création de la ligne au débit
					MoveLine newDebitMoveLine = mls.createMoveLine(newMove, partner, account, creditAmountRemaining, true, false, 
							today, 1, false, false, false, null);
					
					newMove.getMoveLineList().add(newCreditMoveLine);
					newMove.getMoveLineList().add(newDebitMoveLine);
					ms.validateMove(newMove);
					newMove.save();
					
					//Création de la réconciliation
					Reconcile newReconcile = this.createReconcile(newDebitMoveLine, creditMoveLine, creditAmountRemaining);
					this.confirmReconcile(newReconcile);
					newReconcile.save();
				}
			}
		}
	}
	
}
