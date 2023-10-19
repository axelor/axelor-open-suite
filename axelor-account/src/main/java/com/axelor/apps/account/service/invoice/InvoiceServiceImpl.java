/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.invoice.RefundInvoice;
import com.axelor.apps.account.service.invoice.print.InvoicePrintService;
import com.axelor.apps.account.service.invoice.print.InvoiceProductStatementService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.AxelorMessageException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** InvoiceService est une classe implémentant l'ensemble des services de facturation. */
public class InvoiceServiceImpl extends InvoiceRepository implements InvoiceService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ValidateFactory validateFactory;
  protected VentilateFactory ventilateFactory;
  protected CancelFactory cancelFactory;
  protected InvoiceRepository invoiceRepo;
  protected AppAccountService appAccountService;
  protected PartnerService partnerService;
  protected InvoiceLineService invoiceLineService;
  protected AccountConfigService accountConfigService;
  protected MoveToolService moveToolService;
  protected AppBaseService appBaseService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermPfpService invoiceTermPfpService;
  protected TaxService taxService;
  protected InvoiceProductStatementService invoiceProductStatementService;
  protected TemplateMessageService templateMessageService;

  @Inject
  public InvoiceServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService,
      MoveToolService moveToolService,
      InvoiceTermService invoiceTermService,
      InvoiceTermPfpService invoiceTermPfpService,
      AppBaseService appBaseService,
      TaxService taxService,
      InvoiceProductStatementService invoiceProductStatementService,
      TemplateMessageService templateMessageService) {

    this.validateFactory = validateFactory;
    this.ventilateFactory = ventilateFactory;
    this.cancelFactory = cancelFactory;
    this.invoiceRepo = invoiceRepo;
    this.appAccountService = appAccountService;
    this.partnerService = partnerService;
    this.invoiceLineService = invoiceLineService;
    this.accountConfigService = accountConfigService;
    this.moveToolService = moveToolService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermPfpService = invoiceTermPfpService;
    this.appBaseService = appBaseService;
    this.taxService = taxService;
    this.invoiceProductStatementService = invoiceProductStatementService;
    this.templateMessageService = templateMessageService;
  }

  // WKF

  public Journal getJournal(Invoice invoice) throws AxelorException {

    Company company = invoice.getCompany();
    if (company == null) return null;

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // Taken from legacy JournalService but negative cases seem rather strange
    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getSupplierCreditNoteJournal(accountConfig)
            : accountConfigService.getSupplierPurchaseJournal(accountConfig);
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getSupplierPurchaseJournal(accountConfig)
            : accountConfigService.getSupplierCreditNoteJournal(accountConfig);
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getCustomerCreditNoteJournal(accountConfig)
            : accountConfigService.getCustomerSalesJournal(accountConfig);
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getCustomerSalesJournal(accountConfig)
            : accountConfigService.getCustomerCreditNoteJournal(accountConfig);
      default:
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.JOURNAL_1),
            invoice.getInvoiceId());
    }
  }

  /**
   * Fonction permettant de calculer l'intégralité d'une facture :
   *
   * <ul>
   *   <li>Détermine les taxes;
   *   <li>Détermine la TVA;
   *   <li>Détermine les totaux.
   * </ul>
   *
   * (Transaction)
   *
   * @param invoice Une facture.
   * @throws AxelorException
   */
  @Override
  public Invoice compute(final Invoice invoice) throws AxelorException {

    log.debug("Invoice computation");

    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator() {

          @Override
          public Invoice generate() throws AxelorException {

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            if (invoice.getInvoiceLineList() != null) {
              invoiceLines.addAll(invoice.getInvoiceLineList());
            }

            populate(invoice, invoiceLines);

            return invoice;
          }
        };

    Invoice invoice1 = invoiceGenerator.generate();
    invoice1.setAdvancePaymentInvoiceSet(this.getDefaultAdvancePaymentInvoice(invoice1));
    invoice1.setInvoiceProductStatement(
        invoiceProductStatementService.getInvoiceProductStatement(invoice1));
    return invoice1;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateAndVentilate(Invoice invoice) throws AxelorException {
    validate(invoice);
    ventilate(invoice);
  }

  /**
   * Validation d'une facture. (Transaction)
   *
   * @param invoice Une facture.
   * @throws AxelorException
   */
  @Override
  public void validate(Invoice invoice) throws AxelorException {
    validateProcess(invoice);
    if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE
        && invoice.getInvoiceAutomaticMailOnValidate()) {
      sendMail(invoice, invoice.getInvoiceMessageTemplateOnValidate());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void validateProcess(Invoice invoice) throws AxelorException {
    log.debug("Invoice validation");

    compute(invoice);

    validateFactory.getValidator(invoice).process();

    // if the invoice is an advance payment invoice, we also "ventilate" it
    // without creating the move
    if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      ventilate(invoice);
    }
  }

  /**
   * Ventilation comptable d'une facture. (Transaction)
   *
   * @param invoice Une facture.
   * @throws AxelorException
   */
  @Override
  public void ventilate(Invoice invoice) throws AxelorException {
    ventilateProcess(invoice);
    if (invoice.getInvoiceAutomaticMail()) {
      sendMail(invoice, invoice.getInvoiceMessageTemplate());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void ventilateProcess(Invoice invoice) throws AxelorException {
    if (invoice.getPaymentCondition() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.INVOICE_GENERATOR_3),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    if (invoice.getPaymentMode() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.INVOICE_GENERATOR_4),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    if (CollectionUtils.isNotEmpty(invoice.getPaymentCondition().getPaymentConditionLineList())
        && invoice.getPaymentCondition().getPaymentConditionLineList().size() > 1
        && !appAccountService.getAppAccount().getAllowMultiInvoiceTerms()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_MULTIPLE_LINES_NO_MULTI));
    }

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      Account account = invoiceLine.getAccount();

      if (invoiceLine.getAccount() == null
          && (invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_NORMAL)
          && invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.VENTILATE_STATE_6),
            invoiceLine.getProductName());
      }

      if (account != null
          && !account.getAnalyticDistributionAuthorized()
          && (invoiceLine.getAnalyticDistributionTemplate() != null
              || !CollectionUtils.isEmpty(invoiceLine.getAnalyticMoveLineList()))) {
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.VENTILATE_STATE_7));
      }
    }

    if (invoice.getFinancialDiscount() != null && InvoiceToolService.isMultiCurrency(invoice)) {
      String message;

      if (InvoiceToolService.isPurchase(invoice)) {
        message = AccountExceptionMessage.INVOICE_MULTI_CURRENCY_FINANCIAL_DISCOUNT_PURCHASE;
      } else {
        message = AccountExceptionMessage.INVOICE_MULTI_CURRENCY_FINANCIAL_DISCOUNT_SALE;
      }

      throw new AxelorException(
          invoice, TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(message));
    }

    this.computeEstimatedPaymentDate(invoice);

    log.debug("Ventilation of the invoice {}", invoice.getInvoiceId());

    ventilateFactory.getVentilator(invoice).process();

    invoiceRepo.save(invoice);
    if (this.checkEnablePDFGenerationOnVentilation(invoice)) {
      Beans.get(InvoicePrintService.class)
          .printAndSave(
              invoice,
              InvoiceRepository.REPORT_TYPE_ORIGINAL_INVOICE,
              ReportSettings.FORMAT_PDF,
              null);
    }
  }

  /**
   * Annuler une facture. (Transaction)
   *
   * @param invoice Une facture.
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void cancel(Invoice invoice) throws AxelorException {
    log.debug("Canceling invoice {}", invoice.getInvoiceId());

    cancelFactory.getCanceller(invoice).process();

    invoiceRepo.save(invoice);
  }

  protected void sendMail(Invoice invoice, Template template) {
    // send message
    try {
      templateMessageService.generateAndSendMessage(invoice, template);
    } catch (Exception e) {
      TraceBackService.trace(
          new AxelorMessageException(e, invoice, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR));
    }
  }

  /**
   * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur l'écriture de
   * facture. (Transaction)
   *
   * @param invoice Une facture
   */
  @Override
  @Transactional
  public void usherProcess(Invoice invoice) {
    Move move = invoice.getMove();

    if (move != null) {
      if (invoice.getUsherPassageOk()) {
        for (MoveLine moveLine : move.getMoveLineList()) {
          moveLine.setUsherPassageOk(true);
        }
      } else {
        for (MoveLine moveLine : move.getMoveLineList()) {
          moveLine.setUsherPassageOk(false);
        }
      }

      Beans.get(MoveRepository.class).save(move);
    }
  }

  @Override
  public String checkNotImputedRefunds(Invoice invoice) throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());
    if (!accountConfig.getAutoReconcileOnInvoice()) {
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
        long clientRefundsAmount =
            getRefundsAmount(
                invoice.getPartner().getId(), InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);

        if (clientRefundsAmount > 0) {
          return I18n.get(AccountExceptionMessage.INVOICE_NOT_IMPUTED_CLIENT_REFUNDS);
        }
      }

      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {
        long supplierRefundsAmount =
            getRefundsAmount(
                invoice.getPartner().getId(), InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND);

        if (supplierRefundsAmount > 0) {
          return I18n.get(AccountExceptionMessage.INVOICE_NOT_IMPUTED_SUPPLIER_REFUNDS);
        }
      }
    }

    return null;
  }

  protected long getRefundsAmount(Long partnerId, int refundType) {
    return invoiceRepo
        .all()
        .filter(
            "self.partner.id = ?"
                + " AND self.operationTypeSelect = ?"
                + " AND self.statusSelect = ?"
                + " AND self.amountRemaining > 0",
            partnerId,
            refundType,
            InvoiceRepository.STATUS_VENTILATED)
        .count();
  }

  /**
   * Créer un avoir.
   *
   * <p>Un avoir est une facture "inversée". Tout le montant sont opposés à la facture originale.
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice createRefund(Invoice invoice) throws AxelorException {

    Invoice refund = new RefundInvoice(invoice).generate();
    invoice.addRefundInvoiceListItem(refund);
    invoiceRepo.save(invoice);

    return refund;
  }

  @Override
  public void setDraftSequence(Invoice invoice) throws AxelorException {

    if (invoice.getId() != null && Strings.isNullOrEmpty(invoice.getInvoiceId())) {
      invoice.setInvoiceId(Beans.get(SequenceService.class).getDraftSequenceNumber(invoice));
    }
  }

  public Invoice mergeInvoiceProcess(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException {
    Invoice invoiceMerged =
        mergeInvoice(
            invoiceList,
            company,
            currency,
            partner,
            contactPartner,
            priceList,
            paymentMode,
            paymentCondition,
            tradingName,
            fiscalPosition);
    deleteOldInvoices(invoiceList);
    return invoiceMerged;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice mergeInvoice(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException {
    StringBuilder numSeq = new StringBuilder();
    StringBuilder externalRef = new StringBuilder();
    for (Invoice invoiceLocal : invoiceList) {
      if (numSeq.length() > 0) {
        numSeq.append("-");
      }
      if (invoiceLocal.getInternalReference() != null) {
        numSeq.append(invoiceLocal.getInternalReference());
      }

      if (externalRef.length() > 0) {
        externalRef.append("|");
      }
      if (invoiceLocal.getExternalReference() != null) {
        externalRef.append(invoiceLocal.getExternalReference());
      }
    }

    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator(
            InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
            company,
            paymentCondition,
            paymentMode,
            partnerService.getInvoicingAddress(partner),
            partner,
            contactPartner,
            currency,
            priceList,
            numSeq.toString(),
            externalRef.toString(),
            null,
            null,
            tradingName,
            null) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };
    Invoice invoiceMerged = invoiceGenerator.generate();
    invoiceMerged.setFiscalPosition(fiscalPosition);
    List<InvoiceLine> invoiceLines = this.getInvoiceLinesFromInvoiceList(invoiceList);
    invoiceGenerator.populate(invoiceMerged, invoiceLines);
    this.setInvoiceForInvoiceLines(invoiceLines, invoiceMerged);
    invoiceRepo.save(invoiceMerged);
    return invoiceMerged;
  }

  public Invoice mergeInvoiceProcess(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate)
      throws AxelorException {
    Invoice invoiceMerged =
        mergeInvoice(
            invoiceList,
            company,
            currency,
            partner,
            contactPartner,
            priceList,
            paymentMode,
            paymentCondition,
            tradingName,
            fiscalPosition,
            supplierInvoiceNb,
            originDate);
    deleteOldInvoices(invoiceList);
    return invoiceMerged;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice mergeInvoice(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate)
      throws AxelorException {
    StringBuilder numSeq = new StringBuilder();
    StringBuilder externalRef = new StringBuilder();
    for (Invoice invoiceLocal : invoiceList) {
      if (numSeq.length() > 0) {
        numSeq.append("-");
      }
      if (invoiceLocal.getInternalReference() != null) {
        numSeq.append(invoiceLocal.getInternalReference());
      }

      if (externalRef.length() > 0) {
        externalRef.append("|");
      }
      if (invoiceLocal.getExternalReference() != null) {
        externalRef.append(invoiceLocal.getExternalReference());
      }
    }

    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator(
            InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE,
            company,
            paymentCondition,
            paymentMode,
            partnerService.getInvoicingAddress(partner),
            partner,
            contactPartner,
            currency,
            priceList,
            numSeq.toString(),
            externalRef.toString(),
            null,
            null,
            tradingName,
            null) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };
    Invoice invoiceMerged = invoiceGenerator.generate();
    invoiceMerged.setFiscalPosition(fiscalPosition);
    invoiceMerged.setSupplierInvoiceNb(supplierInvoiceNb);
    invoiceMerged.setOriginDate(originDate);
    List<InvoiceLine> invoiceLines = this.getInvoiceLinesFromInvoiceList(invoiceList);
    invoiceGenerator.populate(invoiceMerged, invoiceLines);
    this.setInvoiceForInvoiceLines(invoiceLines, invoiceMerged);
    invoiceTermService.computeInvoiceTerms(invoiceMerged);
    invoiceRepo.save(invoiceMerged);
    return invoiceMerged;
  }

  @Override
  @Transactional
  public void deleteOldInvoices(List<Invoice> invoiceList) {
    for (Invoice invoicetemp : invoiceList) {
      invoicetemp.setStatusSelect(InvoiceRepository.STATUS_CANCELED);
      invoiceRepo.remove(invoicetemp);
    }
  }

  @Override
  public List<InvoiceLine> getInvoiceLinesFromInvoiceList(List<Invoice> invoiceList) {
    List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
    for (Invoice invoice : invoiceList) {
      int countLine = 1;
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        invoiceLine.setSequence(countLine * 10);
        invoiceLines.add(invoiceLine);
        countLine++;
      }
    }
    return invoiceLines;
  }

  @Override
  public void setInvoiceForInvoiceLines(List<InvoiceLine> invoiceLines, Invoice invoice) {
    for (InvoiceLine invoiceLine : invoiceLines) {
      invoiceLine.setInvoice(invoice);
    }
  }

  /**
   * Méthode permettant de récupérer la facture depuis une ligne d'écriture de facture ou une ligne
   * d'écriture de rejet de facture
   *
   * @param moveLine Une ligne d'écriture de facture ou une ligne d'écriture de rejet de facture
   * @return La facture trouvée
   */
  @Override
  public Invoice getInvoice(MoveLine moveLine) {
    Invoice invoice = null;
    if (moveLine.getMove().getRejectOk()) {
      invoice = moveLine.getInvoiceReject();
    } else {
      invoice = moveLine.getMove().getInvoice();
    }
    return invoice;
  }

  @Override
  public String createAdvancePaymentInvoiceSetDomain(Invoice invoice) throws AxelorException {
    Set<Invoice> invoices = getDefaultAdvancePaymentInvoice(invoice);
    String domain = "self.id IN (" + StringHelper.getIdListString(invoices) + ")";

    return domain;
  }

  @Override
  public Set<Invoice> getDefaultAdvancePaymentInvoice(Invoice invoice) throws AxelorException {
    Set<Invoice> advancePaymentInvoices;

    Company company = invoice.getCompany();
    Currency currency = invoice.getCurrency();
    Partner partner = invoice.getPartner();
    if (company == null || currency == null || partner == null) {
      return new HashSet<>();
    }
    String filter = writeGeneralFilterForAdvancePayment();
    filter +=
        " AND self.partner = :_partner "
            + "AND self.currency = :_currency "
            + "AND self.operationTypeSelect = :_operationTypeSelect "
            + "AND self.internalReference IS NULL";
    advancePaymentInvoices =
        new HashSet<>(
            invoiceRepo
                .all()
                .filter(filter)
                .bind("_status", InvoiceRepository.STATUS_VALIDATED)
                .bind("_operationSubType", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
                .bind("_operationTypeSelect", invoice.getOperationTypeSelect())
                .bind("_partner", partner)
                .bind("_currency", currency)
                .fetch());
    filterAdvancePaymentInvoice(invoice, advancePaymentInvoices);
    return advancePaymentInvoices;
  }

  @Override
  public void filterAdvancePaymentInvoice(Invoice invoice, Set<Invoice> advancePaymentInvoices)
      throws AxelorException {
    Iterator<Invoice> advPaymentInvoiceIt = advancePaymentInvoices.iterator();
    while (advPaymentInvoiceIt.hasNext()) {
      Invoice candidateAdvancePayment = advPaymentInvoiceIt.next();
      if (removeBecauseOfTotalAmount(invoice, candidateAdvancePayment)
          || removeBecauseOfAmountRemaining(invoice, candidateAdvancePayment)) {
        advPaymentInvoiceIt.remove();
      }
    }
  }

  protected boolean removeBecauseOfTotalAmount(Invoice invoice, Invoice candidateAdvancePayment)
      throws AxelorException {
    if (accountConfigService
        .getAccountConfig(invoice.getCompany())
        .getGenerateMoveForInvoicePayment()) {
      return false;
    }
    BigDecimal invoiceTotal = invoice.getInTaxTotal();
    List<InvoicePayment> invoicePayments = candidateAdvancePayment.getInvoicePaymentList();
    if (invoicePayments == null) {
      return false;
    }
    BigDecimal totalAmount =
        invoicePayments.stream()
            .map(InvoicePayment::getAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    return totalAmount.compareTo(invoiceTotal) > 0;
  }

  protected boolean removeBecauseOfAmountRemaining(Invoice invoice, Invoice candidateAdvancePayment)
      throws AxelorException {
    List<InvoicePayment> invoicePayments = candidateAdvancePayment.getInvoicePaymentList();
    // no payment : remove the candidate invoice
    if (invoicePayments == null || invoicePayments.isEmpty()) {
      return true;
    }

    // if there is no move generated, we simply check if the payment was
    // imputed
    if (!accountConfigService
        .getAccountConfig(invoice.getCompany())
        .getGenerateMoveForInvoicePayment()) {
      for (InvoicePayment invoicePayment : invoicePayments) {
        if (invoicePayment.getImputedBy() == null) {
          return false;
        }
      }
      return true;
    }

    // else we check the remaining amount
    for (InvoicePayment invoicePayment : invoicePayments) {
      Move move = invoicePayment.getMove();
      if (move == null) {
        continue;
      }
      List<MoveLine> moveLineList = move.getMoveLineList();
      if (moveLineList == null || moveLineList.isEmpty()) {
        continue;
      }
      for (MoveLine moveLine : moveLineList) {
        BigDecimal amountRemaining = moveLine.getAmountRemaining().abs();
        if (amountRemaining != null && amountRemaining.compareTo(BigDecimal.ZERO) > 0) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public List<MoveLine> getMoveLinesFromAdvancePayments(Invoice invoice) throws AxelorException {
    if (appAccountService.getAppAccount().getManageAdvancePaymentInvoice()) {
      return getMoveLinesFromInvoiceAdvancePayments(invoice);
    } else {
      return getMoveLinesFromSOAdvancePayments(invoice);
    }
  }

  @Override
  public List<MoveLine> getMoveLinesFromInvoiceAdvancePayments(Invoice invoice)
      throws AxelorException {
    List<MoveLine> advancePaymentMoveLines = new ArrayList<>();

    Set<Invoice> advancePayments = invoice.getAdvancePaymentInvoiceSet();
    List<InvoicePayment> invoicePayments;
    if (advancePayments == null || advancePayments.isEmpty()) {
      return advancePaymentMoveLines;
    }
    InvoicePaymentToolService invoicePaymentToolService =
        Beans.get(InvoicePaymentToolService.class);
    for (Invoice advancePayment : advancePayments) {
      invoicePayments = advancePayment.getInvoicePaymentList();
      // Since purchase order can have advance payment we check if it is a purchase or not
      // If it is a purchase, we must add debit lines from payment and not credit line.
      if (moveToolService.isDebitCustomer(invoice, true)) {
        List<MoveLine> creditMoveLines =
            invoicePaymentToolService.getMoveLinesFromPayments(invoicePayments, true);
        advancePaymentMoveLines.addAll(creditMoveLines);
      } else {
        List<MoveLine> debitMoveLines =
            invoicePaymentToolService.getMoveLinesFromPayments(invoicePayments, false);
        advancePaymentMoveLines.addAll(debitMoveLines);
      }
    }
    return advancePaymentMoveLines;
  }

  @Override
  public List<MoveLine> getMoveLinesFromSOAdvancePayments(Invoice invoice) {
    return new ArrayList<>();
  }

  protected String writeGeneralFilterForAdvancePayment() {
    return "self.statusSelect = :_status" + " AND self.operationSubTypeSelect = :_operationSubType";
  }

  @Override
  public BankDetails getBankDetails(Invoice invoice) throws AxelorException {
    BankDetails bankDetails;

    if (invoice.getSchedulePaymentOk() && invoice.getPaymentSchedule() != null) {
      bankDetails = invoice.getPaymentSchedule().getBankDetails();
      if (bankDetails != null) {
        return bankDetails;
      }
    }

    bankDetails = invoice.getBankDetails();

    if (bankDetails != null) {
      return bankDetails;
    }

    Partner partner = invoice.getPartner();
    Preconditions.checkNotNull(partner);
    bankDetails = Beans.get(BankDetailsRepository.class).findDefaultByPartner(partner);

    if (bankDetails != null) {
      return bankDetails;
    }

    throw new AxelorException(
        invoice,
        TraceBackRepository.CATEGORY_MISSING_FIELD,
        I18n.get(AccountExceptionMessage.PARTNER_BANK_DETAILS_MISSING),
        partner.getName());
  }

  @Override
  public int getPurchaseTypeOrSaleType(Invoice invoice) {
    if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND) {
      return PriceListRepository.TYPE_SALE;
    } else if (invoice.getOperationTypeSelect()
            == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
      return PriceListRepository.TYPE_PURCHASE;
    }
    return -1;
  }

  @Override
  public Pair<Integer, Integer> massValidate(Collection<? extends Number> invoiceIds) {
    return massProcess(invoiceIds, this::validate, STATUS_DRAFT);
  }

  @Override
  public Pair<Integer, Integer> massValidateAndVentilate(Collection<? extends Number> invoiceIds) {
    return massProcess(invoiceIds, this::validateAndVentilate, STATUS_DRAFT);
  }

  @Override
  public Pair<Integer, Integer> massVentilate(Collection<? extends Number> invoiceIds) {
    return massProcess(invoiceIds, this::ventilate, STATUS_VALIDATED);
  }

  private Pair<Integer, Integer> massProcess(
      Collection<? extends Number> invoiceIds,
      ThrowConsumer<Invoice, Exception> consumer,
      int statusSelect) {
    IntCounter doneCounter = new IntCounter();

    int errorCount =
        ModelHelper.apply(
            Invoice.class,
            invoiceIds,
            new ThrowConsumer<Invoice, Exception>() {
              @Override
              public void accept(Invoice invoice) throws Exception {
                if (invoice.getStatusSelect() == statusSelect) {
                  consumer.accept(invoice);
                  doneCounter.increment();
                }
              }
            });

    return Pair.of(doneCounter.intValue(), errorCount);
  }

  private static class IntCounter extends Number {
    private static final long serialVersionUID = -5434353935712805399L;
    private int count = 0;

    public void increment() {
      ++count;
    }

    @Override
    public int intValue() {
      return count;
    }

    @Override
    public long longValue() {
      return Long.valueOf(count);
    }

    @Override
    public float floatValue() {
      return Float.valueOf(count);
    }

    @Override
    public double doubleValue() {
      return Double.valueOf(count);
    }
  }

  @Override
  public Boolean checkPartnerBankDetailsList(Invoice invoice) {
    PaymentMode paymentMode = invoice.getPaymentMode();
    Partner partner = invoice.getPartner();

    if (partner == null || paymentMode == null) {
      return true;
    }

    int paymentModeInOutSelect = paymentMode.getInOutSelect();
    int paymentModeTypeSelect = paymentMode.getTypeSelect();

    if ((paymentModeInOutSelect == PaymentModeRepository.IN
            && (paymentModeTypeSelect == PaymentModeRepository.TYPE_IPO
                || paymentModeTypeSelect == PaymentModeRepository.TYPE_IPO_CHEQUE
                || paymentModeTypeSelect == PaymentModeRepository.TYPE_DD))
        || (paymentModeInOutSelect == PaymentModeRepository.OUT
            && paymentModeTypeSelect == PaymentModeRepository.TYPE_TRANSFER)) {
      return partner.getBankDetailsList().stream().anyMatch(bankDetails -> bankDetails.getActive());
    }

    return true;
  }

  @Override
  @Transactional
  public void refusalToPay(
      Invoice invoice, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr) {

    User currentUser = AuthUtils.getUser();
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      invoiceTermPfpService.refusalToPay(
          invoiceTerm, reasonOfRefusalToPay, reasonOfRefusalToPayStr);
    }
    invoice.setPfpValidatorUser(currentUser);
    invoice.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_LITIGATION);
    invoice.setDecisionPfpTakenDateTime(
        appBaseService.getTodayDateTime(invoice.getCompany()).toLocalDateTime());
    invoice.setReasonOfRefusalToPay(reasonOfRefusalToPay);
    invoice.setReasonOfRefusalToPayStr(
        reasonOfRefusalToPayStr != null ? reasonOfRefusalToPayStr : reasonOfRefusalToPay.getName());

    invoiceRepo.save(invoice);
  }

  protected boolean checkEnablePDFGenerationOnVentilation(Invoice invoice) throws AxelorException {
    // isPurchase() = isSupplier()
    if (appAccountService.getAppInvoice().getAutoGenerateInvoicePrintingFileOnSaleInvoice()
        && !InvoiceToolService.isPurchase(invoice)) {
      return true;
    }
    if (appAccountService.getAppInvoice().getAutoGenerateInvoicePrintingFileOnPurchaseInvoice()
        && InvoiceToolService.isPurchase(invoice)) {
      return true;
    }
    return false;
  }

  @Override
  public String checkNotLetteredAdvancePaymentMoveLines(Invoice invoice) throws AxelorException {
    if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {
      long supplierNotLetteredAdvancePaymentMoveLinesAmount =
          getNotLetteredAdvancePaymentMoveLinesAmount(invoice.getPartner());

      if (supplierNotLetteredAdvancePaymentMoveLinesAmount > 0) {
        return I18n.get(AccountExceptionMessage.INVOICE_NOT_LETTERED_SUPPLIER_ADVANCE_MOVE_LINES);
      }
    }
    return null;
  }

  protected long getNotLetteredAdvancePaymentMoveLinesAmount(Partner partner) {
    return JPA.em()
        .createQuery(
            "Select moveLine.id "
                + "FROM  MoveLine moveLine "
                + "LEFT JOIN Move move on moveLine.move = move.id "
                + "LEFT JOIN Invoice invoice on move.id = invoice.move "
                + "LEFT JOIN Account account on moveLine.account = account.id "
                + "LEFT JOIN AccountType accountType on account.accountType = accountType.id "
                + "LEFT JOIN Partner partner on moveLine.partner = partner.id "
                + "WHERE invoice.move = null "
                + "AND moveLine.debit > 0 "
                + "AND moveLine.amountRemaining > 0 "
                + "AND accountType.technicalTypeSelect = ?1 "
                + "AND move.statusSelect in (?2,?3) "
                + "AND partner.id = ?4")
        .setParameter(1, AccountTypeRepository.TYPE_PAYABLE)
        .setParameter(2, MoveRepository.STATUS_DAYBOOK)
        .setParameter(3, MoveRepository.STATUS_ACCOUNTED)
        .setParameter(4, partner.getId())
        .getResultList()
        .size();
  }

  @Override
  public boolean checkInvoiceLinesAnalyticDistribution(Invoice invoice) {
    return CollectionUtils.isEmpty(invoice.getInvoiceLineList())
        || invoice.getInvoiceLineList().stream()
            .allMatch(invoiceLineService::checkAnalyticDistribution);
  }

  @Override
  public boolean checkInvoiceLinesCutOffDates(Invoice invoice) {
    return invoice.getInvoiceLineList() == null
        || invoice.getInvoiceLineList().stream().allMatch(invoiceLineService::checkCutOffDates);
  }

  @Override
  public boolean checkManageCutOffDates(Invoice invoice) {
    return CollectionUtils.isNotEmpty(invoice.getInvoiceLineList())
        && invoice.getInvoiceLineList().stream()
            .anyMatch(invoiceLine -> invoiceLineService.checkManageCutOffDates(invoiceLine));
  }

  @Override
  public void applyCutOffDates(
      Invoice invoice, LocalDate cutOffStartDate, LocalDate cutOffEndDate) {
    if (CollectionUtils.isNotEmpty(invoice.getInvoiceLineList())) {
      invoice
          .getInvoiceLineList()
          .forEach(
              invoiceLine ->
                  invoiceLineService.applyCutOffDates(
                      invoiceLine, invoice, cutOffStartDate, cutOffEndDate));
    }
  }

  @Override
  public boolean isSelectedPfpValidatorEqualsPartnerPfpValidator(Invoice invoice) {
    return invoice.getPfpValidatorUser() != null
        && invoice
            .getPfpValidatorUser()
            .equals(
                invoiceTermService.getPfpValidatorUser(invoice.getPartner(), invoice.getCompany()));
  }

  @Transactional
  public void validatePfp(Long invoiceId) {
    Invoice invoice = invoiceRepo.find(invoiceId);
    User pfpValidatorUser =
        invoice.getPfpValidatorUser() != null ? invoice.getPfpValidatorUser() : AuthUtils.getUser();

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      invoiceTermPfpService.validatePfp(invoiceTerm, pfpValidatorUser);
    }

    invoice.setPfpValidatorUser(pfpValidatorUser);
    invoice.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_VALIDATED);
    invoice.setDecisionPfpTakenDateTime(
        appBaseService.getTodayDateTime(invoice.getCompany()).toLocalDateTime());
  }

  @Override
  public void updateUnpaidInvoiceTerms(Invoice invoice) {
    invoice.getInvoiceTermList().stream()
        .filter(it -> !it.getIsPaid() && it.getAmount().equals(it.getAmountRemaining()))
        .forEach(it -> this.updateUnpaidInvoiceTerm(invoice, it));
  }

  protected void updateUnpaidInvoiceTerm(Invoice invoice, InvoiceTerm invoiceTerm) {
    invoiceTerm.setPaymentMode(invoice.getPaymentMode());
    invoiceTerm.setBankDetails(invoice.getBankDetails());
  }

  @Override
  public boolean checkInvoiceTerms(Invoice invoice) throws AxelorException {
    return CollectionUtils.isNotEmpty(invoiceTermService.getUnpaidInvoiceTerms(invoice));
  }

  @Override
  public LocalDate getFinancialDiscountDeadlineDate(
      Invoice invoice, FinancialDiscount financialDiscount) {
    int discountDelay =
        Optional.ofNullable(financialDiscount).map(FinancialDiscount::getDiscountDelay).orElse(0);

    LocalDate deadlineDate = invoice.getDueDate().minusDays(discountDelay);

    return deadlineDate.isBefore(invoice.getInvoiceDate())
        ? invoice.getInvoiceDate()
        : deadlineDate;
  }

  public void updateInvoiceTermsParentFields(Invoice invoice) {
    List<InvoiceTerm> invoiceTermList = invoice.getInvoiceTermList();
    if (CollectionUtils.isEmpty(invoiceTermList)) {
      return;
    }
    invoiceTermList.forEach(
        it ->
            invoiceTermService.setParentFields(
                it,
                Optional.ofNullable(it.getMoveLine()).map(MoveLine::getMove).orElse(null),
                it.getMoveLine(),
                invoice));
  }

  @Override
  public Invoice computeEstimatedPaymentDate(Invoice invoice) {
    if (CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return invoice;
    }
    if (invoice.getPartner() != null && invoice.getPartner().getPaymentDelay() != null) {

      int paymentDelay = invoice.getPartner().getPaymentDelay().intValue();

      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getDueDate() != null && invoiceTerm.getEstimatedPaymentDate() == null) {
          invoiceTerm.setEstimatedPaymentDate(invoiceTerm.getDueDate().plusDays(paymentDelay));
        }
      }
    } else {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getEstimatedPaymentDate() != null) {
          continue;
        }

        LocalDate estimatedPaymentDate = invoiceTerm.getDueDate();

        invoiceTerm.setEstimatedPaymentDate(estimatedPaymentDate);
      }
    }
    return invoice;
  }

  @Override
  public void updateSubrogationPartner(Invoice invoice) {
    if (CollectionUtils.isNotEmpty(invoice.getInvoiceTermList())) {
      invoice
          .getInvoiceTermList()
          .forEach(it -> it.setSubrogationPartner(invoice.getSubrogationPartner()));
    }
  }
}
