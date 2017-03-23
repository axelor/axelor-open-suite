package com.axelor.apps.hr.service.bankorder;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.hr.db.Expense;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BankOrderCreateServiceHr extends BankOrderCreateService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	public BankOrderCreateServiceHr(BankOrderRepository bankOrderRepo, AccountConfigService accountConfigService, BankOrderLineService bankOrderLineService)  {
		super(bankOrderRepo, accountConfigService, bankOrderLineService);
	}

	
	/**
	 * Method to create a bank order for an expense
	 * 
	 * 
	 * @param expense
	 * 			An expense
	 * 
	 * @throws AxelorException
	 * 		
	 */
	public BankOrder createBankOrder(Expense expense) throws AxelorException {
		Company company = expense.getCompany();
		Partner partner = expense.getUser().getPartner();
		PaymentMode paymentMode = partner.getOutPaymentMode();
		BigDecimal amount = expense.getInTaxTotal().subtract(expense.getAdvanceAmount()).subtract(expense.getWithdrawnCash()).subtract(expense.getPersonalExpenseAmount());
		Currency currency = company.getCurrency();
		LocalDate paymentDate =  LocalDate.now();

		BankOrder bankOrder = super.createBankOrder( 
								paymentMode,
								BankOrderRepository.PARTNER_TYPE_EMPLOYEE,
								paymentDate,
								company,
								company.getDefaultBankDetails(),
								currency,
								expense.getFullName(),
								expense.getFullName());
		
		bankOrder.addBankOrderLineListItem(bankOrderLineService.createBankOrderLine(paymentMode.getBankOrderFileFormat(), partner, amount, currency, paymentDate, expense.getFullName(), expense.getFullName()));
		
		return bankOrder;
	}
}
