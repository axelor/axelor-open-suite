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
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveAdjustementService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReconcileService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveToolService moveToolService;
	protected AccountCustomerService accountCustomerService;
	protected AccountConfigService accountConfigService;
	protected ReconcileRepository reconcileRepository;
	protected MoveAdjustementService moveAdjustementService;
	
	@Inject
	public ReconcileService(MoveToolService moveToolService, AccountCustomerService accountCustomerService, AccountConfigService accountConfigService,
			ReconcileRepository reconcileRepository, MoveAdjustementService moveAdjustementService)  {
		
		this.moveToolService = moveToolService;
		this.accountCustomerService = accountCustomerService;
		this.accountConfigService = accountConfigService;
		this.reconcileRepository = reconcileRepository;
		this.moveAdjustementService = moveAdjustementService;

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
	public Reconcile createReconcile(MoveLine debitMoveLine, MoveLine creditMoveLine, BigDecimal amount, boolean canBeZeroBalanceOk)  {

		log.debug("Create Reconcile (Debit MoveLine : {}, Credit MoveLine : {}, Amount : {}, Can be zero balance ? {} ",
				new Object[]{debitMoveLine.getName(), creditMoveLine.getName(), amount, canBeZeroBalanceOk});

		Reconcile reconcile =  new Reconcile(
				amount.setScale(2, RoundingMode.HALF_EVEN),
				debitMoveLine, creditMoveLine,
				ReconcileRepository.STATUS_DRAFT,
				canBeZeroBalanceOk);

		if(!moveToolService.isDebitMoveLine(debitMoveLine))  {

			reconcile.setDebitMoveLine(creditMoveLine);
			reconcile.setCreditMoveLine(debitMoveLine);
		}

		return reconcileRepository.save(reconcile);

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
	public int confirmReconcile(Reconcile reconcile) throws AxelorException  {

		this.reconcilePreconditions(reconcile);

		MoveLine debitMoveLine = reconcile.getDebitMoveLine();
		MoveLine creditMoveLine = reconcile.getCreditMoveLine();

		//Add the reconciled amount to the reconciled amount in the move line
		creditMoveLine.setAmountPaid(creditMoveLine.getAmountPaid().add(reconcile.getAmount()));
		debitMoveLine.setAmountPaid(debitMoveLine.getAmountPaid().add(reconcile.getAmount()));

		this.updatePartnerAccountingSituation(reconcile);
		this.updateInvoiceRemainingAmount(reconcile);

		reconcile.setStatusSelect(ReconcileRepository.STATUS_CONFIRMED);

		if(reconcile.getCanBeZeroBalanceOk())  {
			// Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte transitoire si le seuil est respecté
			canBeZeroBalance(reconcile);
		}

		reconcileRepository.save(reconcile);

		return reconcile.getStatusSelect();
	}



	public void reconcilePreconditions(Reconcile reconcile) throws AxelorException  {

		MoveLine debitMoveLine = reconcile.getDebitMoveLine();
		MoveLine creditMoveLine = reconcile.getCreditMoveLine();

		if (debitMoveLine == null || creditMoveLine == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECONCILE_1),
					GeneralServiceImpl.EXCEPTION), IException.CONFIGURATION_ERROR);
		}

		// Check if move lines accounts are the same (debit and credit)
		if (!creditMoveLine.getAccount().equals(debitMoveLine.getAccount())){
			log.debug("Compte ligne de credit : {} , Compte ligne de debit : {}", creditMoveLine.getAccount(), debitMoveLine.getAccount());
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECONCILE_2)+" " +
					I18n.get(IExceptionMessage.RECONCILE_3),
					GeneralServiceImpl.EXCEPTION, debitMoveLine.getName(), debitMoveLine.getAccount().getLabel(),
					creditMoveLine.getName(), creditMoveLine.getAccount().getLabel()), IException.CONFIGURATION_ERROR);
		}

		// Check if the amount to reconcile is != zero
		if (reconcile.getAmount() == null || reconcile.getAmount().compareTo(BigDecimal.ZERO) == 0)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECONCILE_4),
					GeneralServiceImpl.EXCEPTION, reconcile.getId(), debitMoveLine.getName(), debitMoveLine.getAccount().getLabel(),
					creditMoveLine.getName(), creditMoveLine.getAccount().getLabel()), IException.INCONSISTENCY);

		}

		if ((reconcile.getAmount().compareTo(creditMoveLine.getCredit().subtract(creditMoveLine.getAmountPaid())) > 0
				|| (reconcile.getAmount().compareTo(debitMoveLine.getDebit().subtract(debitMoveLine.getAmountPaid())) > 0))){
			throw new AxelorException(
					String.format(I18n.get(IExceptionMessage.RECONCILE_5)+" " +
							I18n.get(IExceptionMessage.RECONCILE_3),
							GeneralServiceImpl.EXCEPTION, reconcile.getId(), debitMoveLine.getName(), debitMoveLine.getAccount().getLabel(),
					creditMoveLine.getName(), creditMoveLine.getAccount().getLabel()), IException.INCONSISTENCY);

		}

	}


	public void updatePartnerAccountingSituation(Reconcile reconcile)  {
		
		List<Partner> partnerList = this.getPartners(reconcile);

		if(partnerList != null && !partnerList.isEmpty())  {
			
			Company company = reconcile.getDebitMoveLine().getMove().getCompany();
			
			if(AccountingService.getUpdateCustomerAccount())  {
				accountCustomerService.updatePartnerAccountingSituation(partnerList, company, true, true, false);
			}
			else  {
				accountCustomerService.flagPartners(partnerList, company);
			}
		}
	}
	
	
	protected List<Partner> getPartners(Reconcile reconcile)  {
		
		List<Partner> partnerList = Lists.newArrayList();
		Partner debitPartner = reconcile.getDebitMoveLine().getPartner();
		Partner creditPartner = reconcile.getCreditMoveLine().getPartner();
		if(debitPartner != null && creditPartner != null && debitPartner.equals(creditPartner))  {	
			partnerList.add(debitPartner);
		}
		else if(debitPartner != null)  { partnerList.add(debitPartner); }
		else if(creditPartner != null)  { partnerList.add(creditPartner);	}
		
		return partnerList;
	} 


	protected void updateInvoiceRemainingAmount(Reconcile reconcile) throws AxelorException  {

		Invoice debitInvoice = reconcile.getDebitMoveLine().getMove().getInvoice();
		Invoice creditInvoice = reconcile.getCreditMoveLine().getMove().getInvoice();

		// Update amount remaining on invoice or refund
		if(debitInvoice != null)  {
			debitInvoice.setCompanyInTaxTotalRemaining(  moveToolService.getInTaxTotalRemaining(debitInvoice)  );
		}
		if(creditInvoice != null)  {
			creditInvoice.setCompanyInTaxTotalRemaining(  moveToolService.getInTaxTotalRemaining(creditInvoice)  );
		}

	}


	/**
	 * Méthode permettant de lettrer une écriture au débit avec une écriture au crédit
	 * @param debitMoveLine
	 * @param creditMoveLine
	 * @throws AxelorException
	 */
	public Reconcile reconcile(MoveLine debitMoveLine, MoveLine creditMoveLine, boolean canBeZeroBalanceOk) throws AxelorException  {

		BigDecimal amount = debitMoveLine.getAmountRemaining().min(creditMoveLine.getAmountRemaining());
		Reconcile reconcile = this.createReconcile(debitMoveLine, creditMoveLine, amount, canBeZeroBalanceOk);
		
		this.confirmReconcile(reconcile);
		
		return reconcile;
		
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
	public void unreconcile(Reconcile reconcile) throws AxelorException  {

		MoveLine debitMoveLine = reconcile.getDebitMoveLine();
		MoveLine creditMoveLine = reconcile.getCreditMoveLine();

		// Change the state
		reconcile.setStatusSelect(ReconcileRepository.STATUS_CANCELED);
		//Add the reconciled amount to the reconciled amount in the move line
		creditMoveLine.setAmountPaid(creditMoveLine.getAmountPaid().subtract(reconcile.getAmount()));
		debitMoveLine.setAmountPaid(debitMoveLine.getAmountPaid().subtract(reconcile.getAmount()));

		// Update amount remaining on invoice or refund
		this.updatePartnerAccountingSituation(reconcile);
		this.updateInvoiceRemainingAmount(reconcile);

		reconcileRepository.save(reconcile);

	}



	/**
	 * Procédure permettant de gérer les écarts de règlement, check sur la case à cocher 'Peut être soldé'
	 *  Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte transitoire si le seuil est respecté
	 * @param reconcile
	 * 			Une reconciliation
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	protected void canBeZeroBalance(Reconcile reconcile) throws AxelorException  {

		MoveLine debitMoveLine = reconcile.getDebitMoveLine();

		BigDecimal debitAmountRemaining = debitMoveLine.getAmountRemaining();
		log.debug("Montant à payer / à lettrer au débit : {}", debitAmountRemaining);
		if(debitAmountRemaining.compareTo(BigDecimal.ZERO) > 0)  {
			Company company = reconcile.getDebitMoveLine().getMove().getCompany();

			AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

			if(debitAmountRemaining.plus().compareTo(accountConfig.getThresholdDistanceFromRegulation()) < 0 || reconcile.getMustBeZeroBalanceOk())  {

				log.debug("Seuil respecté");

				MoveLine creditAdjustMoveLine = moveAdjustementService.createAdjustmentCreditMove(debitMoveLine);
				
				//Création de la réconciliation
				Reconcile newReconcile = this.createReconcile(debitMoveLine, creditAdjustMoveLine, debitAmountRemaining, false);
				this.confirmReconcile(newReconcile);
				reconcileRepository.save(newReconcile);
			}
		}

		reconcile.setCanBeZeroBalanceOk(false);
		log.debug("Fin de la gestion des écarts de règlement");
	}


	/**
	 * Solder le trop-perçu si il respect les règles de seuil
	 * @param creditMoveLine
	 * @param company
	 * @throws AxelorException
	 */
	public void balanceCredit(MoveLine creditMoveLine) throws AxelorException  {
		if(creditMoveLine != null)  {
			BigDecimal creditAmountRemaining = creditMoveLine.getAmountRemaining();
			log.debug("Montant à payer / à lettrer au crédit : {}", creditAmountRemaining);

			if(creditAmountRemaining.compareTo(BigDecimal.ZERO) > 0)  {
				AccountConfig accountConfig = accountConfigService.getAccountConfig(creditMoveLine.getMove().getCompany());

				if(creditAmountRemaining.plus().compareTo(accountConfig.getThresholdDistanceFromRegulation()) < 0)  {

					log.debug("Seuil respecté");

					MoveLine debitAdjustmentMoveLine = moveAdjustementService.createAdjustmentCreditMove(creditMoveLine);
					
					//Création de la réconciliation
					Reconcile newReconcile = this.createReconcile(debitAdjustmentMoveLine, creditMoveLine, creditAmountRemaining, false);
					this.confirmReconcile(newReconcile);
					reconcileRepository.save(newReconcile);
				}
			}
		}
	}

	
	public List<Reconcile> getReconciles(MoveLine moveLine)  {
		
		List<Reconcile> debitReconcileList = moveLine.getDebitReconcileList();
		List<Reconcile> creditReconcileList = moveLine.getCreditReconcileList();
		
		if(moveToolService.isDebitMoveLine(moveLine))  {
			return debitReconcileList;
		}
		else if(debitReconcileList != null && !creditReconcileList.isEmpty()) {
			return creditReconcileList;
		}
		return Lists.newArrayList();
	}
	
}
