/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.bankorder;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankOrderCreateServiceHr extends BankOrderCreateService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public BankOrderCreateServiceHr(
      BankOrderRepository bankOrderRepo,
      BankOrderService bankOrderService,
      BankOrderLineService bankOrderLineService,
      InvoiceService invoiceService) {
    super(bankOrderRepo, bankOrderService, bankOrderLineService, invoiceService);
  }

  /**
   * Method to create a bank order for an expense
   *
   * @param expense An expense
   * @throws AxelorException
   */
  public BankOrder createBankOrder(Expense expense, BankDetails bankDetails)
      throws AxelorException {
    Company company = expense.getCompany();
    Partner partner = expense.getEmployee().getContactPartner();
    PaymentMode paymentMode = expense.getPaymentMode();
    BigDecimal amount =
        expense
            .getInTaxTotal()
            .subtract(expense.getAdvanceAmount())
            .subtract(expense.getWithdrawnCash())
            .subtract(expense.getPersonalExpenseAmount());
    Currency currency = company.getCurrency();
    LocalDate paymentDate =
        expense.getPaymentDate() != null
            ? expense.getPaymentDate()
            : Beans.get(AppBaseService.class)
                .getTodayDate(
                    company); // Take into consideration today's date if paymentDate is null

    BankOrder bankOrder =
        super.createBankOrder(
            paymentMode,
            BankOrderRepository.PARTNER_TYPE_EMPLOYEE,
            paymentDate,
            company,
            bankDetails,
            currency,
            expense.getFullName(),
            expense.getFullName(),
            BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            BankOrderRepository.FUNCTIONAL_ORIGIN_EXPENSE,
            PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE);

    bankOrder.addBankOrderLineListItem(
        bankOrderLineService.createBankOrderLine(
            paymentMode.getBankOrderFileFormat(),
            partner,
            amount,
            currency,
            paymentDate,
            expense.getExpenseSeq(),
            expense.getFullName(),
            expense));
    bankOrder = bankOrderRepo.save(bankOrder);

    return bankOrder;
  }

  public BankOrder createBankOrder(Expense expense) throws AxelorException {
    return createBankOrder(expense, expense.getCompany().getDefaultBankDetails());
  }
}
