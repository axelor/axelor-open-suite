/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.DirectDebitManagementRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountBlockingService;
import com.axelor.apps.account.service.PaymentScheduleExportService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.bankorder.file.cfonb.CfonbExportService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;

public class BankPayPaymentScheduleExportService extends PaymentScheduleExportService {
	
	protected CfonbExportService cfonbExportService;
	
	public BankPayPaymentScheduleExportService(MoveService moveService,
			MoveRepository moveRepo, MoveLineService moveLineServices,
			MoveLineRepository moveLineRepo, ReconcileService reconcileService,
			SequenceService sequenceService,
			PaymentModeService paymentModeService,
			PaymentService paymentService,
			AccountBlockingService blockingService,
			AccountConfigService accountConfigService,
			PaymentScheduleLineRepository paymentScheduleLineRepo,
			DirectDebitManagementRepository directDebitManagementRepo,
			InvoiceService invoiceService, InvoiceRepository invoiceRepo,
			CfonbExportService cfonbExportService, 
			GeneralService generalService, PartnerService partnerService) {
		super(moveService, moveRepo, moveLineServices, moveLineRepo, reconcileService,
				sequenceService, paymentModeService, paymentService, blockingService,
				accountConfigService, paymentScheduleLineRepo,
				directDebitManagementRepo, invoiceService, invoiceRepo, generalService,
				partnerService);
		this.cfonbExportService = cfonbExportService;
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentScheduleLine generateExportMensu (PaymentScheduleLine paymentScheduleLine, List<PaymentScheduleLine> paymentScheduleLineList, Company company) throws AxelorException  {
		
		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
		
		this.testBankDetails(paymentSchedule);
		
		return super.generateExportMensu(paymentScheduleLine, paymentScheduleLineList, company);
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice exportInvoice(MoveLine moveLine, List<MoveLine> moveLineList, Company company, long directDebitManagementMaxId) throws AxelorException  {

		this.testBankDetails(moveLine.getPartner());
		
		return super.exportInvoice(moveLine, moveLineList, company, directDebitManagementMaxId);
		
	}
	
	@Override
	public Invoice updateInvoice(MoveLine moveLine, Move paymentMove, List<MoveLine> mlList, BigDecimal amountExported, long directDebitManagementMaxId) throws AxelorException  {

		Invoice invoice = invoiceService.getInvoice(moveLine);

		this.testBankDetails(invoice);
		
		return super.updateInvoice(moveLine, paymentMove, mlList, amountExported, directDebitManagementMaxId);
	}

		
	public void testBankDetails(PaymentSchedule paymentSchedule) throws AxelorException  {
		Partner partner = paymentSchedule.getPartner();
		BankDetails bankDetails = paymentSchedule.getBankDetails();
		if(bankDetails == null)  {
			bankDetails = partnerService.getDefaultBankDetails(partner);
		}
		if(bankDetails == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_1),
					GeneralServiceImpl.EXCEPTION,paymentSchedule.getScheduleId()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cfonbExportService.testBankDetailsField(bankDetails);
		}
	}


	public void testBankDetails(Invoice invoice) throws AxelorException  {
		BankDetails bankDetails = partnerService.getDefaultBankDetails(invoice.getPartner());

		if(bankDetails == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_2),
					GeneralServiceImpl.EXCEPTION, invoice.getPartner().getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cfonbExportService.testBankDetailsField(bankDetails);
		}
	}


	public void testBankDetails(Partner partner) throws AxelorException  {
		BankDetails bankDetails = partnerService.getDefaultBankDetails(partner);

		if(bankDetails == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_2),
					GeneralServiceImpl.EXCEPTION, partner.getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cfonbExportService.testBankDetailsField(bankDetails);
		}
	}



}