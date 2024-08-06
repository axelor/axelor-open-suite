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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.apps.sale.service.AdvancePaymentServiceImpl;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancePaymentServiceSupplychainImpl extends AdvancePaymentServiceImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected PaymentModeService paymentModeService;

  @Inject protected MoveCreateService moveCreateService;

  @Inject protected MoveValidateService moveValidateService;
  @Inject protected MoveLineCreateService moveLineCreateService;

  @Inject protected CurrencyService currencyService;

  @Inject protected AccountConfigService accountConfigService;

  @Inject protected InvoicePaymentRepository invoicePaymentRepository;

  @Inject protected MoveCancelService moveCancelService;

  @Inject protected AppSupplychainService appSupplychainService;

  @Transactional(rollbackOn = {Exception.class})
  public void validate(AdvancePayment advancePayment) throws AxelorException {

    if (advancePayment.getStatusSelect() != AdvancePaymentRepository.STATUS_DRAFT) {
      return;
    }

    advancePayment.setStatusSelect(AdvancePaymentRepository.STATUS_VALIDATED);

    Company company = advancePayment.getSaleOrder().getCompany();

    if (accountConfigService.getAccountConfig(company).getGenerateMoveForAdvancePayment()
        && advancePayment.getAmount().compareTo(BigDecimal.ZERO) != 0) {
      this.createMoveForAdvancePayment(advancePayment);
    }

    advancePaymentRepository.save(advancePayment);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void cancel(AdvancePayment advancePayment) throws AxelorException {
    Move move = advancePayment.getMove();
    if (move.getStatusSelect() == MoveRepository.STATUS_NEW) {
      advancePayment.setMove(null);
    }
    moveCancelService.cancel(move);
    advancePayment.setStatusSelect(AdvancePaymentRepository.STATUS_CANCELED);
    advancePaymentRepository.save(advancePayment);
  }

  public void createInvoicePayments(Invoice invoice, SaleOrder saleOrder) {
    if (saleOrder.getAdvancePaymentList() == null || saleOrder.getAdvancePaymentList().isEmpty()) {
      return;
    }

    BigDecimal total = saleOrder.getInTaxTotal();

    //		for (AdvancePayment advancePayment : saleOrder.getAdvancePaymentList())  {
    //
    //			if(advancePayment.getAmountRemainingToUse().compareTo(BigDecimal.ZERO) != 0 &&
    // total.compareTo(BigDecimal.ZERO) != 0)  {
    //				if(total.max(advancePayment.getAmountRemainingToUse()) == total)  {
    //					total = total.subtract(advancePayment.getAmountRemainingToUse());
    //					InvoicePayment invoicePayment = createInvoicePayment(advancePayment, invoice,
    // advancePayment.getAmountRemainingToUse(), saleOrder);
    //					invoice.addInvoicePaymentListItem(invoicePayment);
    //					advancePayment.setAmountRemainingToUse(BigDecimal.ZERO);
    //				}
    //				else  {
    //
    //	advancePayment.setAmountRemainingToUse(advancePayment.getAmountRemainingToUse().subtract(total));
    //					InvoicePayment invoicePayment = createInvoicePayment(advancePayment, invoice, total,
    // saleOrder);
    //					invoicePayment.setInvoice(invoice);
    //					invoice.addInvoicePaymentListItem(invoicePayment);
    //					total = BigDecimal.ZERO;
    //				}
    //			}
    //		}
  }

  @Transactional(rollbackOn = {Exception.class})
  public Move createMoveForAdvancePayment(AdvancePayment advancePayment) throws AxelorException {

    SaleOrder saleOrder = advancePayment.getSaleOrder();
    Company company = saleOrder.getCompany();
    PaymentMode paymentMode = advancePayment.getPaymentMode();
    Partner clientPartner = saleOrder.getClientPartner();
    LocalDate advancePaymentDate = advancePayment.getAdvancePaymentDate();
    BankDetails bankDetails = saleOrder.getCompanyBankDetails();
    String origin = saleOrder.getSaleOrderSeq();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    if (bankDetails == null && appSupplychainService.getAppBase().getManageMultiBanks()) {
      throw new AxelorException(
          paymentMode,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_BANK_DETAILS_MISSING),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName(),
          paymentMode.getName(),
          saleOrder.getSaleOrderSeq());
    }
    Journal journal = paymentModeService.getPaymentModeJournal(paymentMode, company, bankDetails);

    Move move =
        moveCreateService.createMove(
            journal,
            company,
            advancePayment.getCurrency(),
            clientPartner,
            advancePaymentDate,
            advancePaymentDate,
            paymentMode,
            saleOrder.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            null,
            bankDetails);

    BigDecimal amountConverted =
        currencyService.getAmountCurrencyConvertedAtDate(
            advancePayment.getCurrency(),
            saleOrder.getCurrency(),
            advancePayment.getAmount(),
            advancePaymentDate);

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            clientPartner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, bankDetails),
            amountConverted,
            true,
            advancePaymentDate,
            null,
            1,
            origin,
            null));

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            clientPartner,
            accountConfigService.getAdvancePaymentAccount(accountConfig),
            amountConverted,
            false,
            advancePaymentDate,
            null,
            2,
            origin,
            null));

    moveValidateService.accounting(move);

    advancePayment.setMove(move);
    advancePaymentRepository.save(advancePayment);

    return move;
  }

  @Transactional(rollbackOn = {Exception.class})
  public InvoicePayment createInvoicePayment(
      AdvancePayment advancePayment, Invoice invoice, BigDecimal amount) {

    log.debug("Creating InvoicePayment from SaleOrder AdvancePayment");
    InvoicePayment invoicePayment = new InvoicePayment();

    invoicePayment.setAmount(amount);
    invoicePayment.setPaymentDate(advancePayment.getAdvancePaymentDate());
    invoicePayment.setCurrency(advancePayment.getCurrency());
    invoicePayment.setInvoice(invoice);
    invoicePayment.setPaymentMode(advancePayment.getPaymentMode());
    invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_ADVANCEPAYMENT);
    invoicePayment.setMove(advancePayment.getMove());

    invoicePaymentRepository.save(invoicePayment);

    invoice.addInvoicePaymentListItem(invoicePayment);

    return invoicePayment;
  }
}
