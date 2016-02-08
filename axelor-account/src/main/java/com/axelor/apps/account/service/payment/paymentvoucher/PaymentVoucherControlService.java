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
import java.util.List;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class PaymentVoucherControlService  {

	protected PaymentVoucherSequenceService paymentVoucherSequenceService;
	
	@Inject
	public PaymentVoucherControlService(PaymentVoucherSequenceService paymentVoucherSequenceService)  {
		
		this.paymentVoucherSequenceService = paymentVoucherSequenceService;
		
	}


	/**
	 * Procédure permettant de vérifier le remplissage et le bon contenu des champs de la saisie paiement et de la société
	 * @param paymentVoucher
	 * 			Une saisie paiement
	 * @param company
	 * 			Une société
	 * @param paymentModeAccount
	 * 			Le compte de trésoreie du mode de règlement
	 * @throws AxelorException
	 */
	public void checkPaymentVoucherField(PaymentVoucher paymentVoucher, Company company, Account paymentModeAccount, Journal journal) throws AxelorException  {
		if(paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_VOUCHER_CONTROL_1),
					GeneralServiceImpl.EXCEPTION, paymentVoucher.getRef()), IException.INCONSISTENCY);
		}

		// Si on a des lignes à payer (dans le deuxième tableau)
		if(!paymentVoucher.getHasAutoInput() && (paymentVoucher.getPaymentInvoiceToPayList() == null || paymentVoucher.getPaymentInvoiceToPayList().size() == 0))  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_VOUCHER_CONTROL_2),  GeneralServiceImpl.EXCEPTION), IException.INCONSISTENCY);
		}

		if(journal == null || paymentModeAccount == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_VOUCHER_CONTROL_3),
					GeneralServiceImpl.EXCEPTION), IException.CONFIGURATION_ERROR);
		}

		if(journal.getEditReceiptOk())  {
			paymentVoucherSequenceService.checkReceipt(paymentVoucher);
		}
	}


	public void checkPayboxAmount(PaymentVoucher paymentVoucher) throws AxelorException  {
		if(paymentVoucher.getPayboxAmountPaid() != null && paymentVoucher.getPayboxAmountPaid().compareTo(paymentVoucher.getPaidAmount()) != 0)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_VOUCHER_CONTROL_4),
						GeneralServiceImpl.EXCEPTION,paymentVoucher.getPaidAmount(),paymentVoucher.getPayboxAmountPaid()), IException.INCONSISTENCY);
		}
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



}
