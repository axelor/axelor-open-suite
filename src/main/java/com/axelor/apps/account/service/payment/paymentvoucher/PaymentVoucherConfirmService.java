/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
package com.axelor.apps.account.service.payment.paymentvoucher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.IPaymentVoucher;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentVoucherConfirmService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherConfirmService.class); 
	
	private ReconcileService reconcileService;

	@Inject
	private MoveLineService moveLineService;

	@Inject
	private MoveService moveService;

	@Inject
	private PaymentService paymentService;

	@Inject
	private PaymentScheduleService paymentScheduleService;

	@Inject
	private PaymentModeService paymentModeService;

	@Inject
	private PaymentVoucherSequenceService paymentVoucherSequenceService;
	
	@Inject
	private PaymentVoucherControlService paymentVoucherControlService;
	
	@Inject
	private PaymentVoucherToolService paymentVoucherToolService;
	
	@Inject
	private CurrencyService currencyService;
	
	
	/**
	 * Confirms the payment voucher
	 * if the selected lines PiToPay 2nd O2M belongs to different companies -> error
	 * I - Payment with an amount
	 * 		If we pay a classical moveLine (invoice, reject ..) -> just create a payment
	 * 		If we pay a schedule 2 payments are created 1st reconciled with the invoice and the second reconciled with the schedule
	 * II - Payment with an excess Payment
	 * 		If we pay a moveLine having the same account, we just reconcile
	 * 		If we pay a with different account -> 1- switch money to the good account 2- reconcile then
	 * @param paymentVoucher
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirmPaymentVoucher(PaymentVoucher paymentVoucher, boolean updateCustomerAccount)  throws AxelorException {
		LOG.debug("In confirmPaymentVoucherService ....");
		paymentVoucherSequenceService.setReference(paymentVoucher);

		Partner payerPartner = paymentVoucher.getPartner();
		PaymentMode paymentMode = paymentVoucher.getPaymentMode();
		Company company = paymentVoucher.getCompany();
		Journal journal = paymentMode.getBankJournal();
		LocalDate paymentDate = paymentVoucher.getPaymentDateTime().toLocalDate();
		
		boolean scheduleToBePaid = false;
		Account paymentModeAccount = paymentModeService.getCompanyAccount(paymentMode, company);
		
		paymentVoucherControlService.checkPaymentVoucherField(paymentVoucher, company, paymentModeAccount, journal);	
		
		if(paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0 && !journal.getExcessPaymentOk())  {
			throw new AxelorException(String.format("%s :\n Attention - Vous ne pouvez pas régler un montant supérieur aux factures selectionnées.", 
					GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
		}
			
		if(paymentVoucher.getPayboxPaidOk())  {
			paymentVoucherControlService.checkPayboxAmount(paymentVoucher);
		}
		
		// TODO VEIRIFER QUE LES ELEMENTS A PAYER NE CONCERNE QU'UNE SEULE DEVISE
		
		// TODO RECUPERER DEVISE DE LA PREMIERE DETTE
		Currency currencyToPay = null;
			
		// If paid by a moveline check if all the lines selected have the same account + company
		// Excess payment
		boolean allRight = paymentVoucherControlService.checkIfSameAccount(paymentVoucher.getPaymentInvoiceToPayList(), paymentVoucher.getMoveLine());
		//Check if allright=true (means companies and accounts in lines are all the same and same as in move line selected for paying
		LOG.debug("allRight : {}", allRight);
		
		if (allRight){	scheduleToBePaid = this.toPayWithExcessPayment(paymentVoucher.getPaymentInvoiceToPayList(), paymentVoucher.getMoveLine(), scheduleToBePaid, paymentDate); }
		
		if(paymentVoucher.getMoveLine() == null || (paymentVoucher.getMoveLine() != null && !allRight) || (scheduleToBePaid && !allRight && paymentVoucher.getMoveLine() != null))  {
		
			PaymentScheduleLine lastPaymentScheduleLine = null;
			
			//Manage all the cases in the same way. As if a move line (Excess payment) is selected, we cancel it first
			Move move = moveService.createMove(paymentVoucher.getPaymentMode().getBankJournal(),company,null,payerPartner, paymentDate, paymentMode,false, paymentVoucher.getCashRegister());
			
			move.setPaymentVoucher(paymentVoucher);
			
			paymentVoucher.setGeneratedMove(move);
			// Create move lines for payment lines
			BigDecimal paidLineTotal = BigDecimal.ZERO;
			int moveLineNo=1;
			
			boolean isDebitToPay = paymentVoucherToolService.isDebitToPay(paymentVoucher);
			
			for (PaymentInvoiceToPay paymentInvoiceToPay : this.getPaymentInvoiceToPayList(paymentVoucher))  {
				MoveLine moveLineToPay = paymentInvoiceToPay.getMoveLine();
				LOG.debug("PV moveLineToPay debit : {}", moveLineToPay.getDebit());
				LOG.debug("PV moveLineToPay amountPaid : {}", moveLineToPay.getAmountPaid());
//				BigDecimal amountToPay = paymentInvoiceToPay.getAmountToPay();
				
				BigDecimal amountToPay = this.getAmountCurrencyConverted(moveLineToPay, paymentVoucher, paymentInvoiceToPay.getAmountToPay());
				
				if (amountToPay.compareTo(BigDecimal.ZERO) > 0)  {								
					
					paidLineTotal = paidLineTotal.add(amountToPay);
						
					this.payMoveLine(move, moveLineNo, payerPartner, moveLineToPay, amountToPay, paymentInvoiceToPay, isDebitToPay, paymentDate, updateCustomerAccount);
					
					moveLineNo +=1;
					
				}
			}
			// Create move line for the payment amount
			MoveLine moveLine = null;
			
			// cancelling the moveLine (excess payment) by creating the balance of all the payments
			// on the same account as the moveLine (excess payment)
			// in the else case we create a classical balance on the bank account of the payment mode
			if (paymentVoucher.getMoveLine() != null){
				moveLine = moveLineService.createMoveLine(move,paymentVoucher.getPartner(),paymentVoucher.getMoveLine().getAccount(), 
						paymentVoucher.getPaidAmount(), isDebitToPay, false, paymentDate, moveLineNo, null);
				
				Reconcile reconcile = reconcileService.createReconcile(moveLine,paymentVoucher.getMoveLine(),moveLine.getDebit(), !isDebitToPay);
				reconcileService.confirmReconcile(reconcile, updateCustomerAccount);
			}
			else{
				moveLine = moveLineService.createMoveLine(move, payerPartner, paymentModeAccount, paymentVoucher.getPaidAmount(), isDebitToPay, false, paymentDate, moveLineNo, null);
			}
			move.getMoveLineList().add(moveLine);
			// Check if the paid amount is > paid lines total
			// Then Use Excess payment on old invoices / moveLines
			if (paymentVoucher.getPaidAmount().compareTo(paidLineTotal) > 0){
				BigDecimal remainingPaidAmount = paymentVoucher.getRemainingAmount();
				
				moveLine = moveLineService.createMoveLine(move,paymentVoucher.getPartner(), company.getAccountConfig().getCustomerAccount(),
						remainingPaidAmount,!isDebitToPay, false, paymentDate, moveLineNo++, null);
				move.getMoveLineList().add(moveLine);
				
				if(lastPaymentScheduleLine == null || paymentScheduleService.isLastSchedule(lastPaymentScheduleLine))  {
					if(isDebitToPay)  {
						reconcileService.balanceCredit(moveLine, company, updateCustomerAccount);
					}
				}
				
			}
			moveService.validateMove(move);
			paymentVoucher.setGeneratedMove(move);
		}
		paymentVoucher.setStateSelect(IPaymentVoucher.STATE_CONFIRMED);
		paymentVoucherSequenceService.setReceiptNo(paymentVoucher, company, journal);
		paymentVoucher.save();
	}
	
	
	/**
	 * Récupérer les éléments à payer dans le bon ordre
	 * @return
	 */
	public List<PaymentInvoiceToPay>  getPaymentInvoiceToPayList(PaymentVoucher paymentVoucher)  {
		
		return PaymentInvoiceToPay.	filter("self.paymentVoucher = ?1 ORDER by self.sequence ASC", paymentVoucher).fetch();
		
	}
	
	

	/**
	 * 	 If paid by a moveline check if all the lines selected have the same account + company
	 *	 Excess payment
	 *	Check if allright=true (means companies and accounts in lines are all the same and same as in move line selected for paying
	 * @param paymentInvoiceToPayList
	 * 		  		Liste des paiement a réaliser
	 * @param creditMoveLine
	 * 				Le trop-perçu
	 * @param scheduleToBePaid
	 * @return
	 * 				Une échéance doit-elle être payée?
	 * @throws AxelorException
	 */
	public boolean toPayWithExcessPayment(List<PaymentInvoiceToPay> paymentInvoiceToPayList, MoveLine creditMoveLine, boolean scheduleToBePaid, LocalDate paymentDate) throws AxelorException  {
		boolean scheduleToBePaid2 = scheduleToBePaid;
		
		List<MoveLine> debitMoveLines = new ArrayList<MoveLine>();
		for (PaymentInvoiceToPay paymentInvoiceToPay : paymentInvoiceToPayList)  {
			
			debitMoveLines.add(paymentInvoiceToPay.getMoveLine());
		}	
		List<MoveLine> creditMoveLines = new ArrayList<MoveLine>();
		creditMoveLines.add(creditMoveLine);
		paymentService.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLines);
		return scheduleToBePaid2;
	}
	
	
	/**
	 * 
	 * @param paymentMove
	 * @param moveLineSeq
	 * @param payerPartner
	 * @param moveLineToPay
	 * @param amountToPay
	 * @param paymentInvoiceToPay
	 * @return
	 * @throws AxelorException
	 */
	public MoveLine payMoveLine(Move paymentMove, int moveLineSeq, Partner payerPartner, MoveLine moveLineToPay, BigDecimal amountToPay, PaymentInvoiceToPay paymentInvoiceToPay,
			boolean isDebitToPay, LocalDate paymentDate, boolean updateCustomerAccount) throws AxelorException  {
		String invoiceName = "";
		if(moveLineToPay.getMove().getInvoice()!=null)  {
			invoiceName = moveLineToPay.getMove().getInvoice().getInvoiceId();
		}
		else  {
			invoiceName = paymentInvoiceToPay.getPaymentVoucher().getRef();
		}
		MoveLine moveLine = moveLineService.createMoveLine(
				paymentMove,
				payerPartner,
				moveLineToPay.getAccount(),
				amountToPay,
				!isDebitToPay,
				false,
				paymentDate,
				moveLineSeq,
				invoiceName);
		
		paymentMove.getMoveLineList().add(moveLine);
		paymentInvoiceToPay.setMoveLineGenerated(moveLine);
		
		Reconcile reconcile = reconcileService.createGenericReconcile(moveLineToPay,moveLine,amountToPay,true, false, !isDebitToPay);
		LOG.debug("Reconcile : : : {}", reconcile);
		reconcileService.confirmReconcile(reconcile, updateCustomerAccount);
		return moveLine;
	}
	
	
	public BigDecimal getAmountCurrencyConverted(MoveLine moveLineToPay, PaymentVoucher paymentVoucher, BigDecimal amountToPay) throws AxelorException  {
		
		Currency moveCurrency = moveLineToPay.getMove().getCurrency();
		
		Currency paymentVoucherCurrency = paymentVoucher.getCurrency();
		
		LocalDate paymentVoucherDate = paymentVoucher.getPaymentDateTime().toLocalDate();
		
		return currencyService.getAmountCurrencyConverted(paymentVoucherCurrency, moveCurrency, amountToPay, paymentVoucherDate);
		
	}
	
}
