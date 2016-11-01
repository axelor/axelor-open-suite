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
package com.axelor.apps.account.service.payment.paymentvoucher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoice;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentInvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentInvoiceToPayRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentVoucherLoadService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected CurrencyService currencyService;
	protected PaymentVoucherSequenceService paymentVoucherSequenceService;
	protected PaymentVoucherToolService paymentVoucherToolService;
	protected PaymentInvoiceRepository paymentInvoiceRepo;
	protected PaymentInvoiceToPayService paymentInvoiceToPayService;
	protected PaymentVoucherRepository paymentVoucherRepository;
	protected PaymentInvoiceToPayRepository paymentInvoiceToPayRepository;
	
	@Inject
	public PaymentVoucherLoadService(CurrencyService currencyService, PaymentVoucherSequenceService paymentVoucherSequenceService, PaymentVoucherToolService paymentVoucherToolService,
			PaymentInvoiceRepository paymentInvoiceRepo, PaymentInvoiceToPayService paymentInvoiceToPayService, PaymentVoucherRepository paymentVoucherRepository,
			PaymentInvoiceToPayRepository paymentInvoiceToPayRepository)  {
		
		this.currencyService = currencyService;
		this.paymentVoucherSequenceService = paymentVoucherSequenceService;
		this.paymentVoucherToolService = paymentVoucherToolService;
		this.paymentInvoiceRepo = paymentInvoiceRepo;
		this.paymentInvoiceToPayService = paymentInvoiceToPayService;
		this.paymentVoucherRepository = paymentVoucherRepository;
		this.paymentInvoiceToPayRepository = paymentInvoiceToPayRepository;
		
	}

	/**
	 * Searching move lines to pay
	 * @param pv paymentVoucher
	 * @param mlToIgnore moveLine list to ignore
	 * @return moveLines a list of moveLines
	 * @throws AxelorException
	 */
	public List<MoveLine> getMoveLines(PaymentVoucher paymentVoucher) throws AxelorException {
		
		MoveLineRepository moveLineRepo = Beans.get(MoveLineRepository.class);
		
		List<? extends MoveLine> moveLines = null;

		String query = "self.partner = ?1 " +
				"and self.account.reconcileOk = 't' " +
				"and self.amountRemaining > 0 " +
				"and self.move.statusSelect = ?3 " +
				"and self.move.ignoreInReminderOk = 'f' " +
				"and self.move.company = ?2 ";

		if(paymentVoucherToolService.isDebitToPay(paymentVoucher))  {
			query += " and self.debit > 0 ";
		}
		else  {
			query += " and self.credit > 0 ";
		}

		moveLines = moveLineRepo.all().filter(query, paymentVoucher.getPartner(), paymentVoucher.getCompany(), MoveRepository.STATUS_VALIDATED).fetch();

		return (List<MoveLine>) moveLines;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void searchDueElements(PaymentVoucher paymentVoucher) throws AxelorException  {

		if (paymentVoucher.getPaymentInvoiceList() != null)  {
			paymentVoucher.getPaymentInvoiceList().clear();
		}
		
		for (MoveLine moveLine : this.getMoveLines(paymentVoucher))  {
			
			paymentVoucher.addPaymentInvoiceListItem(this.createPaymentInvoice(moveLine));
			
		}

		paymentVoucherRepository.save(paymentVoucher);

	}
	

	public PaymentInvoice createPaymentInvoice(MoveLine moveLine) throws AxelorException  {
		
		Move move = moveLine.getMove();
		
		PaymentInvoice paymentInvoice = new PaymentInvoice();
		
		paymentInvoice.setMoveLine(moveLine);

		paymentInvoice.setDueAmount(moveLine.getCurrencyAmount());
		
		BigDecimal paidAmountInElementCurrency = currencyService.getAmountCurrencyConvertedAtDate(
				move.getCompanyCurrency(), move.getCurrency(), moveLine.getAmountPaid(), moveLine.getDate()).setScale(2, RoundingMode.HALF_EVEN);
		
		paymentInvoice.setPaidAmount(paidAmountInElementCurrency);
		
		paymentInvoice.setAmountRemaining(paymentInvoice.getDueAmount().subtract(paymentInvoice.getPaidAmount()));
		
		paymentInvoice.setCurrency(move.getCurrency());
		
		return paymentInvoice;
	}
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void loadSelectedLines(PaymentVoucher paymentVoucher, PaymentVoucher paymentVoucherContext) throws AxelorException  {
		
		if (paymentVoucherContext.getPaymentInvoiceList() != null)  {  

			if (paymentVoucherContext.getPaidAmount() == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_VOUCHER_LOAD_1), GeneralServiceImpl.EXCEPTION), IException.MISSING_FIELD);
			}
			
			paymentVoucher.setPaidAmount(paymentVoucherContext.getPaidAmount());
			
			this.completeElementToPay(paymentVoucher, paymentVoucherContext);
		
		}
		
		paymentVoucher.setPaidAmount(paymentVoucherContext.getPaidAmount());
		
		paymentVoucherRepository.save(paymentVoucher);
		
	}
	
	
	/**
	 * Allows to load selected lines (from 1st 02M) to the 2nd O2M
	 * and dispatching amounts according to amountRemainnig for the loaded move and the paid amount remaining of the paymentVoucher
	 * @param paymentVoucher
	 * @param paymentVoucherContext
	 * @return
	 * @return
	 * @return values Map of data
	 * @throws AxelorException
	 */
	public void completeElementToPay(PaymentVoucher paymentVoucher, PaymentVoucher paymentVoucherContext) throws AxelorException {

		int sequence = paymentVoucher.getPaymentInvoiceToPayList().size() + 1;
		
		for (PaymentInvoice paymentInvoiceContext : paymentVoucherContext.getPaymentInvoiceList())  {
			PaymentInvoice paymentInvoice = paymentInvoiceRepo.find(paymentInvoiceContext.getId());

			if (paymentInvoiceContext.isSelected()){
				
				paymentVoucher.addPaymentInvoiceToPayListItem(this.createPaymentInvoiceToPay(paymentInvoice, sequence++));
				
				// Remove the line from the due elements lists
				paymentVoucher.removePaymentInvoiceListItem(paymentInvoice);
				
			}
		}
		
	}
	
	public PaymentInvoiceToPay createPaymentInvoiceToPay(PaymentInvoice paymentInvoice, int sequence) throws AxelorException  {
		
		PaymentVoucher paymentVoucher = paymentInvoice.getPaymentVoucher();
		BigDecimal amountRemaining = paymentVoucher.getRemainingAmount();
		LocalDate paymentDate = paymentVoucher.getPaymentDateTime().toLocalDate();
		
		PaymentInvoiceToPay paymentInvoiceToPay = new PaymentInvoiceToPay();

		paymentInvoiceToPay.setSequence(sequence);
		paymentInvoiceToPay.setMoveLine(paymentInvoice.getMoveLine());
		paymentInvoiceToPay.setTotalAmount(paymentInvoice.getDueAmount());
		paymentInvoiceToPay.setRemainingAmount(paymentInvoice.getAmountRemaining());
		paymentInvoiceToPay.setCurrency(paymentInvoice.getCurrency());

		BigDecimal amountRemainingInElementCurrency = currencyService.getAmountCurrencyConvertedAtDate(
				paymentVoucher.getCurrency(), paymentInvoiceToPay.getCurrency(), amountRemaining, paymentDate).setScale(2, RoundingMode.HALF_EVEN);

		BigDecimal amountImputedInElementCurrency = amountRemainingInElementCurrency.min(paymentInvoiceToPay.getRemainingAmount());
		
		BigDecimal amountImputedInPayVouchCurrency = currencyService.getAmountCurrencyConvertedAtDate(
				paymentInvoiceToPay.getCurrency(), paymentVoucher.getCurrency(), amountImputedInElementCurrency, paymentDate).setScale(2, RoundingMode.HALF_EVEN);
		
		paymentInvoiceToPay.setAmountToPay(amountImputedInElementCurrency);
		paymentInvoiceToPay.setAmountToPayCurrency(amountImputedInPayVouchCurrency);
		paymentInvoiceToPay.setRemainingAmountAfterPayment(paymentInvoiceToPay.getRemainingAmount().subtract(amountImputedInElementCurrency));

		return paymentInvoiceToPay;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void resetImputation(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		paymentVoucher.getPaymentInvoiceToPayList().clear();

		this.searchDueElements(paymentVoucher);
		
	}
	
	
	/**
	 * Fonction vérifiant si l'ensemble des lignes à payer ont le même compte et que ce compte est le même que celui du trop-perçu
	 * @param paymentInvoiceToPayList
	 * 			La liste des lignes à payer
	 * @param moveLine
	 * 			Le trop-perçu à utiliser
	 * @return
	 */
	public boolean checkIfSameAccount(List<PaymentInvoiceToPay> paymentInvoiceToPayList, MoveLine moveLine)  {
		if(moveLine != null)  {
			Account account = moveLine.getAccount();
			for (PaymentInvoiceToPay paymentInvoiceToPay : paymentInvoiceToPayList)  {
				if(!paymentInvoiceToPay.getMoveLine().getAccount().equals(account))  {
					return false;
				}
			}
			return true;
		}
		return false;
	}


	public boolean mustBeBalanced(MoveLine moveLineToPay, PaymentVoucher paymentVoucher, BigDecimal amountToPay)  {

		Invoice invoice = moveLineToPay.getMove().getInvoice();

		Currency invoiceCurrency = invoice.getCurrency();

		Currency paymentVoucherCurrency = paymentVoucher.getCurrency();

		// Si la devise de paiement est la même que le devise de la facture,
		// Et que le montant déjà payé en devise sur la facture, plus le montant réglé par la nouvelle saisie paiement en devise, est égale au montant total en devise de la facture
		// Alors on solde la facture
		if(paymentVoucherCurrency.equals(invoiceCurrency) && invoice.getAmountPaid().add(amountToPay).compareTo(invoice.getInTaxTotal()) == 0)  {
			//SOLDER
			return true;
		}

		return false;

	}


	/**
	 *
	 * @param moveLineInvoiceToPay
	 * 				Les lignes de factures récupérées depuis l'échéance
	 * @param paymentInvoiceToPay
	 * 				La Ligne de saisie paiement
	 * @return
	 */
	public List<MoveLine> assignMaxAmountToReconcile (List<MoveLine> moveLineInvoiceToPay, BigDecimal amountToPay)  {
		List<MoveLine>  debitMoveLines = new ArrayList<MoveLine>();
		if(moveLineInvoiceToPay != null && moveLineInvoiceToPay.size()!=0)  {
			// Récupération du montant imputé sur l'échéance, et assignation de la valeur dans la moveLine (champ temporaire)
			BigDecimal maxAmountToPayRemaining = amountToPay;
			for(MoveLine moveLine : moveLineInvoiceToPay)  {
				if(maxAmountToPayRemaining.compareTo(BigDecimal.ZERO) > 0)  {
					BigDecimal amountPay = maxAmountToPayRemaining.min(moveLine.getAmountRemaining());
					moveLine.setMaxAmountToReconcile(amountPay);
					debitMoveLines.add(moveLine);
					maxAmountToPayRemaining = maxAmountToPayRemaining.subtract(amountPay);
				}
			}
		}
		return debitMoveLines;
	}


}
