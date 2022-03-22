/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PayVoucherDueElementRepository;
import com.axelor.apps.account.db.repo.PayVoucherElementToPayRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PaymentVoucherLoadService {

  protected CurrencyService currencyService;
  protected PaymentVoucherToolService paymentVoucherToolService;
  protected PayVoucherDueElementRepository payVoucherDueElementRepo;
  protected PaymentVoucherRepository paymentVoucherRepository;
  protected PayVoucherDueElementService payVoucherDueElementService;
  protected PayVoucherElementToPayService payVoucherElementToPayService;
  protected PayVoucherElementToPayRepository payVoucherElementToPayRepo;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public PaymentVoucherLoadService(
      CurrencyService currencyService,
      PaymentVoucherToolService paymentVoucherToolService,
      PayVoucherDueElementRepository payVoucherDueElementRepo,
      PaymentVoucherRepository paymentVoucherRepository,
      PayVoucherDueElementService payVoucherDueElementService,
      PayVoucherElementToPayService payVoucherElementToPayService,
      PayVoucherElementToPayRepository payVoucherElementToPayRepo,
      InvoiceTermService invoiceTermService) {

    this.currencyService = currencyService;
    this.paymentVoucherToolService = paymentVoucherToolService;
    this.payVoucherDueElementRepo = payVoucherDueElementRepo;
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.payVoucherDueElementService = payVoucherDueElementService;
    this.payVoucherElementToPayService = payVoucherElementToPayService;
    this.payVoucherElementToPayRepo = payVoucherElementToPayRepo;
    this.invoiceTermService = invoiceTermService;
  }

  /**
   * Searching invoice terms to pay
   *
   * @param paymentVoucher paymentVoucher
   * @return invoiceTerms a list of invoiceTerms
   * @throws AxelorException
   */
  public List<InvoiceTerm> getInvoiceTerms(PaymentVoucher paymentVoucher) throws AxelorException {

    InvoiceTermRepository invoiceTermRepo = Beans.get(InvoiceTermRepository.class);

    String query =
        "(self.moveLine.partner = :partner OR self.invoice.partner = :partner) "
            + "and (self.isPaid = FALSE OR self.amountRemaining > 0) "
            + "and (self.moveLine.move.company = :company OR self.invoice.company = :company) "
            + "and self.moveLine.account.useForPartnerBalance = 't' "
            + "and self.moveLine.move.ignoreInDebtRecoveryOk = 'f' "
            + "and (self.moveLine.move.statusSelect = :statusDaybook OR self.moveLine.move.statusSelect = :statusAccounted) "
            + "and (self.moveLine.move.tradingName = :tradingName OR self.invoice.tradingName = :tradingName OR (self.moveLine.move.tradingName = NULL AND self.invoice.tradingName = NULL)) "
            + "and (self.invoice = null or self.invoice.operationTypeSelect = :operationTypeSelect)";

    if (Beans.get(AccountConfigService.class)
                .getAccountConfig(paymentVoucher.getCompany())
                .getIsManagePassedForPayment()
            && paymentVoucher.getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || paymentVoucher.getOperationTypeSelect()
            == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
      query +=
          "and (self.pfpValidateStatusSelect != :pfpStatusAwaiting OR self.pfpValidateStatusSelect != :pfpStatusLitigation) ";
    }

    if (paymentVoucherToolService.isDebitToPay(paymentVoucher)) {
      query += " and self.moveLine.debit > 0 ";
    } else {
      query += " and self.moveLine.credit > 0 ";
    }

    return invoiceTermRepo
        .all()
        .filter(query)
        .bind("partner", paymentVoucher.getPartner())
        .bind("company", paymentVoucher.getCompany())
        .bind("statusDaybook", MoveRepository.STATUS_DAYBOOK)
        .bind("statusAccounted", MoveRepository.STATUS_ACCOUNTED)
        .bind("tradingName", paymentVoucher.getTradingName())
        .bind("operationTypeSelect", paymentVoucher.getOperationTypeSelect())
        .bind("pfpStatusAwaiting", InvoiceRepository.PFP_STATUS_AWAITING)
        .bind("pfpStatusLitigation", InvoiceRepository.PFP_STATUS_LITIGATION)
        .fetch();
  }

  public List<PayVoucherDueElement> searchDueElements(PaymentVoucher paymentVoucher)
      throws AxelorException {

    if (paymentVoucher.getPayVoucherElementToPayList() != null) {
      paymentVoucher.getPayVoucherElementToPayList().clear();
    }

    if (paymentVoucher.getPayVoucherDueElementList() != null) {
      paymentVoucher.getPayVoucherDueElementList().clear();
    }

    int sequence = 0;
    for (InvoiceTerm invoiceTerm : this.getInvoiceTerms(paymentVoucher)) {
      PayVoucherDueElement payVoucherDueElement =
          this.createPayVoucherDueElement(paymentVoucher, invoiceTerm);
      if (payVoucherDueElement != null) {
        payVoucherDueElement.setSequence(sequence++);
        paymentVoucher.addPayVoucherDueElementListItem(payVoucherDueElement);
      }
    }

    return paymentVoucher.getPayVoucherDueElementList();
  }

  public PayVoucherDueElement createPayVoucherDueElement(
      PaymentVoucher paymentVoucher, InvoiceTerm invoiceTerm) throws AxelorException {

    if (invoiceTerm.getMoveLine() == null || invoiceTerm.getMoveLine().getMove() == null) {
      return null;
    }

    PayVoucherDueElement payVoucherDueElement = new PayVoucherDueElement();

    payVoucherDueElement.setInvoiceTerm(invoiceTerm);

    payVoucherDueElement.setMoveLine(invoiceTerm.getMoveLine());

    payVoucherDueElement.setDueAmount(invoiceTerm.getAmount());

    payVoucherDueElement.setAmountRemaining(this.getAmountRemaining(paymentVoucher, invoiceTerm));

    payVoucherDueElement.setCurrency(
        invoiceTerm.getMoveLine().getMove().getCurrency() != null
            ? invoiceTerm.getMoveLine().getMove().getCurrency()
            : invoiceTerm.getInvoice().getCurrency());

    payVoucherDueElementService.updateDueElementWithFinancialDiscount(
        payVoucherDueElement, paymentVoucher);

    return payVoucherDueElement;
  }

  protected BigDecimal getAmountRemaining(PaymentVoucher paymentVoucher, InvoiceTerm invoiceTerm) {
    return payVoucherDueElementService.applyFinancialDiscount(invoiceTerm, paymentVoucher)
        ? invoiceTerm.getAmountRemainingAfterFinDiscount()
        : invoiceTerm.getAmountRemaining();
  }

  public boolean loadSelectedLines(PaymentVoucher paymentVoucher) throws AxelorException {
    boolean generateAll = true;

    if (paymentVoucher.getPayVoucherElementToPayList() != null) {

      if (paymentVoucher.getPaidAmount() == null) {
        throw new AxelorException(
            paymentVoucher,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.PAYMENT_VOUCHER_LOAD_1),
            I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION));
      }

      generateAll = this.completeElementToPay(paymentVoucher);
    }

    return generateAll;
  }

  /**
   * Allows to load selected lines (from 1st 02M) to the 2nd O2M and dispatching amounts according
   * to amountRemainnig for the loaded move and the paid amount remaining of the paymentVoucher
   *
   * @param paymentVoucher
   * @return
   * @return
   * @return values Map of data
   * @throws AxelorException
   */
  public boolean completeElementToPay(PaymentVoucher paymentVoucher) throws AxelorException {

    int sequence = paymentVoucher.getPayVoucherElementToPayList().size() + 1;
    boolean generateAll = true;

    paymentVoucher
        .getPayVoucherDueElementList()
        .sort(Comparator.comparing(PayVoucherDueElement::getSequence));
    List<PayVoucherDueElement> toRemove = new ArrayList<>();

    for (PayVoucherDueElement payVoucherDueElement : paymentVoucher.getPayVoucherDueElementList()) {

      if (payVoucherDueElement.isSelected()) {

        PayVoucherElementToPay payVoucherElementToPay =
            this.createPayVoucherElementToPay(paymentVoucher, payVoucherDueElement, sequence++);

        if (payVoucherElementToPay != null
            && payVoucherElementToPay.getAmountToPay().signum() > 0) {
          paymentVoucher.addPayVoucherElementToPayListItem(payVoucherElementToPay);

          paymentVoucher.setRemainingAmount(
              paymentVoucher
                  .getRemainingAmount()
                  .subtract(payVoucherElementToPay.getAmountToPay()));

          // Remove the line from the due elements lists
          toRemove.add(payVoucherDueElement);
        } else {
          generateAll = false;
        }
      }
    }
    for (PayVoucherDueElement payVoucherDueElement : toRemove) {
      paymentVoucher.removePayVoucherDueElementListItem(payVoucherDueElement);
    }

    this.sortElementsToPay(paymentVoucher);

    return generateAll;
  }

  public PayVoucherElementToPay createPayVoucherElementToPay(
      PaymentVoucher paymentVoucher, PayVoucherDueElement payVoucherDueElement, int sequence)
      throws AxelorException {

    BigDecimal amountRemaining = paymentVoucher.getRemainingAmount();
    LocalDate paymentDate = paymentVoucher.getPaymentDate();

    PayVoucherElementToPay payVoucherElementToPay = new PayVoucherElementToPay();

    payVoucherElementToPay.setPaymentVoucher(paymentVoucher);
    payVoucherElementToPay.setSequence(sequence);
    payVoucherElementToPay.setInvoiceTerm(payVoucherDueElement.getInvoiceTerm());
    payVoucherElementToPay.setMoveLine(payVoucherDueElement.getMoveLine());
    payVoucherElementToPay.setTotalAmount(payVoucherDueElement.getDueAmount());
    payVoucherElementToPay.setRemainingAmount(payVoucherDueElement.getAmountRemaining());
    payVoucherElementToPay.setCurrency(payVoucherDueElement.getCurrency());

    BigDecimal amountRemainingInElementCurrency =
        currencyService
            .getAmountCurrencyConvertedAtDate(
                paymentVoucher.getCurrency(),
                payVoucherElementToPay.getCurrency(),
                amountRemaining,
                paymentDate)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal amountImputedInElementCurrency =
        amountRemainingInElementCurrency.min(payVoucherElementToPay.getRemainingAmount());

    BigDecimal amountImputedInPayVouchCurrency =
        currencyService
            .getAmountCurrencyConvertedAtDate(
                payVoucherElementToPay.getCurrency(),
                paymentVoucher.getCurrency(),
                amountImputedInElementCurrency,
                paymentDate)
            .setScale(2, RoundingMode.HALF_UP);

    payVoucherElementToPay.setAmountToPay(amountImputedInElementCurrency);
    payVoucherElementToPay.setAmountToPayCurrency(amountImputedInPayVouchCurrency);
    payVoucherElementToPay.setRemainingAmountAfterPayment(
        payVoucherElementToPay.getRemainingAmount().subtract(amountImputedInElementCurrency));

    payVoucherElementToPayService.updateElementToPayWithFinancialDiscount(
        payVoucherElementToPay, payVoucherDueElement, paymentVoucher);

    return payVoucherElementToPay;
  }

  public void resetImputation(PaymentVoucher paymentVoucher) throws AxelorException {
    paymentVoucher.getPayVoucherElementToPayList().clear();

    paymentVoucher.setPayVoucherDueElementList(searchDueElements(paymentVoucher));

    this.computeFinancialDiscount(paymentVoucher);
  }

  /**
   * Fonction vérifiant si l'ensemble des lignes à payer ont le même compte et que ce compte est le
   * même que celui du trop-perçu
   *
   * @param payVoucherElementToPayList La liste des lignes à payer
   * @param moveLine Le trop-perçu à utiliser
   * @return
   */
  public boolean checkIfSameAccount(
      List<PayVoucherElementToPay> payVoucherElementToPayList, MoveLine moveLine) {
    if (moveLine != null) {
      Account account = moveLine.getAccount();
      for (PayVoucherElementToPay payVoucherElementToPay : payVoucherElementToPayList) {
        if (!payVoucherElementToPay.getMoveLine().getAccount().equals(account)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public boolean mustBeBalanced(
      MoveLine moveLineToPay, PaymentVoucher paymentVoucher, BigDecimal amountToPay) {

    Invoice invoice = moveLineToPay.getMove().getInvoice();

    Currency invoiceCurrency = invoice.getCurrency();

    Currency paymentVoucherCurrency = paymentVoucher.getCurrency();

    // Si la devise de paiement est la même que le devise de la facture,
    // Et que le montant déjà payé en devise sur la facture, plus le montant réglé
    // par la nouvelle saisie paiement en devise, est égale au montant total en
    // devise de la facture
    // Alors on solde la facture
    if (paymentVoucherCurrency.equals(invoiceCurrency)
        && invoice.getAmountPaid().add(amountToPay).compareTo(invoice.getInTaxTotal()) == 0) {
      // SOLDER
      return true;
    }

    return false;
  }

  /**
   * @param moveLineInvoiceToPay Invoice lines fetched from invoice
   * @param amountToPay Amount of the payment
   * @return
   */
  public List<MoveLine> assignMaxAmountToReconcile(
      List<MoveLine> moveLineInvoiceToPay, BigDecimal amountToPay) {
    List<MoveLine> debitMoveLines = new ArrayList<>();
    if (moveLineInvoiceToPay != null && moveLineInvoiceToPay.size() != 0) {
      // Récupération du montant imputé sur l'échéance, et assignation de la valeur
      // dans la moveLine (champ temporaire)
      BigDecimal maxAmountToPayRemaining = amountToPay;
      for (MoveLine moveLine : moveLineInvoiceToPay) {
        if (maxAmountToPayRemaining.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal amountPay = maxAmountToPayRemaining.min(moveLine.getAmountRemaining());
          moveLine.setMaxAmountToReconcile(amountPay);
          debitMoveLines.add(moveLine);
          maxAmountToPayRemaining = maxAmountToPayRemaining.subtract(amountPay);
        }
      }
    }
    return debitMoveLines;
  }

  /**
   * Initialize a payment voucher from an invoice.
   *
   * @param paymentVoucher
   * @param invoice
   * @throws AxelorException
   */
  public void initFromInvoice(PaymentVoucher paymentVoucher, Invoice invoice)
      throws AxelorException {
    paymentVoucher.setOperationTypeSelect(invoice.getOperationTypeSelect());
    paymentVoucher.setPartner(invoice.getPartner());
    paymentVoucher.setPaymentMode(invoice.getPaymentMode());
    paymentVoucher.setCurrency(invoice.getCurrency());
    paymentVoucher.clearPayVoucherDueElementList();
    paymentVoucher.clearPayVoucherElementToPayList();
    paymentVoucher.setCompany(invoice.getCompany());
    BankDetails companyBankDetails;

    if (invoice.getCompanyBankDetails() != null) {
      companyBankDetails = invoice.getCompanyBankDetails();
    } else {
      companyBankDetails =
          Beans.get(BankDetailsService.class)
              .getDefaultCompanyBankDetails(
                  invoice.getCompany(), invoice.getPaymentMode(), invoice.getPartner(), null);
    }

    paymentVoucher.setCompanyBankDetails(companyBankDetails);
    BigDecimal amount = BigDecimal.ZERO;
    List<InvoiceTerm> invoiceTermList = getInvoiceTerms(paymentVoucher);

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      PayVoucherDueElement payVoucherDueElement =
          createPayVoucherDueElement(paymentVoucher, invoiceTerm);
      paymentVoucher.addPayVoucherDueElementListItem(payVoucherDueElement);

      if (invoice.equals(payVoucherDueElement.getMoveLine().getMove().getInvoice())) {
        amount = amount.add(payVoucherDueElement.getAmountRemaining());
      }
    }

    paymentVoucher.setPaidAmount(amount);
    paymentVoucher.clearPayVoucherDueElementList();

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      paymentVoucher.addPayVoucherDueElementListItem(
          createPayVoucherDueElement(paymentVoucher, invoiceTerm));
    }

    if (paymentVoucher.getPayVoucherDueElementList() == null) {
      return;
    }

    int sequence = 0;

    for (Iterator<PayVoucherDueElement> it =
            paymentVoucher.getPayVoucherDueElementList().iterator();
        it.hasNext(); ) {
      PayVoucherDueElement payVoucherDueElement = it.next();

      if (invoice.equals(payVoucherDueElement.getMoveLine().getMove().getInvoice())
          && paymentVoucher.getCurrency().equals(payVoucherDueElement.getCurrency())) {
        paymentVoucher.addPayVoucherElementToPayListItem(
            createPayVoucherElementToPay(paymentVoucher, payVoucherDueElement, ++sequence));
        it.remove();
      }
    }

    this.sortElementsToPay(paymentVoucher);
  }

  @Transactional
  public void reloadElementToPayList(
      PaymentVoucher paymentVoucher, PaymentVoucher paymentVoucherContext) {

    List<PayVoucherElementToPay> listToKeep = paymentVoucherContext.getPayVoucherElementToPayList();
    paymentVoucher.clearPayVoucherElementToPayList();

    listToKeep.forEach(
        elementToPay ->
            paymentVoucher.addPayVoucherElementToPayListItem(
                payVoucherElementToPayRepo.find(elementToPay.getId())));

    this.sortElementsToPay(paymentVoucher);

    paymentVoucherRepository.save(paymentVoucher);
  }

  public void computeFinancialDiscount(PaymentVoucher paymentVoucher) throws AxelorException {
    if (paymentVoucher != null
        && !CollectionUtils.isEmpty(paymentVoucher.getPayVoucherDueElementList())
        && paymentVoucher.getPartner() != null
        && paymentVoucher.getPartner().getFinancialDiscount() != null) {
      for (PayVoucherDueElement payVoucherDueElement :
          paymentVoucher.getPayVoucherDueElementList()) {
        payVoucherDueElement =
            payVoucherDueElementService.updateDueElementWithFinancialDiscount(
                payVoucherDueElement, paymentVoucher);
      }
    }
  }

  protected void sortElementsToPay(PaymentVoucher paymentVoucher) {
    Comparator<PayVoucherElementToPay> comparator =
        Comparator.comparing(t -> t.getInvoiceTerm().getDueDate());
    comparator = comparator.thenComparing(t -> t.getInvoiceTerm().getInvoice().getInvoiceDate());

    paymentVoucher.getPayVoucherElementToPayList().sort(comparator);

    int seq = 1;
    for (PayVoucherElementToPay payVoucherElementToPay :
        paymentVoucher.getPayVoucherElementToPayList()) {
      payVoucherElementToPay.setSequence(seq++);
    }
  }
}
