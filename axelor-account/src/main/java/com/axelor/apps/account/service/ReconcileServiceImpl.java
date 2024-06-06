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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveAdjustementService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class ReconcileServiceImpl implements ReconcileService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final int ALTERNATIVE_SCALE = 5;

  protected MoveToolService moveToolService;
  protected AccountCustomerService accountCustomerService;
  protected AccountConfigService accountConfigService;
  protected ReconcileRepository reconcileRepository;
  protected MoveAdjustementService moveAdjustementService;
  protected ReconcileSequenceService reconcileSequenceService;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoicePaymentCancelService invoicePaymentCancelService;
  protected InvoiceTermService invoiceTermService;
  protected MoveLineTaxService moveLineTaxService;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected AppBaseService appBaseService;
  protected PaymentMoveLineDistributionService paymentMoveLineDistributionService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoiceTermPaymentRepository invoiceTermPaymentRepo;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected MoveLineControlService moveLineControlService;
  protected MoveLineRepository moveLineRepo;
  protected SubrogationReleaseWorkflowService subrogationReleaseWorkflowService;
  protected InvoiceTermPfpService invoiceTermPfpService;

  @Inject
  public ReconcileServiceImpl(
      MoveToolService moveToolService,
      AccountCustomerService accountCustomerService,
      AccountConfigService accountConfigService,
      ReconcileRepository reconcileRepository,
      MoveAdjustementService moveAdjustementService,
      ReconcileSequenceService reconcileSequenceService,
      InvoicePaymentCancelService invoicePaymentCancelService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      MoveLineTaxService moveLineTaxService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermService invoiceTermService,
      AppBaseService appBaseService,
      PaymentMoveLineDistributionService paymentMoveLineDistributionService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermPaymentRepository invoiceTermPaymentRepo,
      InvoicePaymentToolService invoicePaymentToolService,
      MoveLineControlService moveLineControlService,
      MoveLineRepository moveLineRepo,
      SubrogationReleaseWorkflowService subrogationReleaseWorkflowService,
      InvoiceTermPfpService invoiceTermPfpService) {

    this.moveToolService = moveToolService;
    this.accountCustomerService = accountCustomerService;
    this.accountConfigService = accountConfigService;
    this.reconcileRepository = reconcileRepository;
    this.moveAdjustementService = moveAdjustementService;
    this.reconcileSequenceService = reconcileSequenceService;
    this.invoicePaymentCancelService = invoicePaymentCancelService;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.moveLineTaxService = moveLineTaxService;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.invoiceTermService = invoiceTermService;
    this.appBaseService = appBaseService;
    this.paymentMoveLineDistributionService = paymentMoveLineDistributionService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoiceTermPaymentRepo = invoiceTermPaymentRepo;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.moveLineControlService = moveLineControlService;
    this.moveLineRepo = moveLineRepo;
    this.subrogationReleaseWorkflowService = subrogationReleaseWorkflowService;
    this.invoiceTermPfpService = invoiceTermPfpService;
  }

  /**
   * Permet de créer une réconciliation en passant les paramètres qu'il faut
   *
   * @param debitMoveLine Une ligne d'écriture au débit
   * @param creditMoveLine Une ligne d'écriture au crédit
   * @param amount Le montant à reconciler
   * @param canBeZeroBalanceOk Peut être soldé?
   * @return Une reconciliation
   */
  @Override
  @Transactional
  public Reconcile createReconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      BigDecimal amount,
      boolean canBeZeroBalanceOk)
      throws AxelorException {

    if (ReconcileService.isReconcilable(debitMoveLine, creditMoveLine)
        && amount.compareTo(BigDecimal.ZERO) > 0) {

      log.debug(
          "Create Reconcile (Company : {}, Debit MoveLine : {}, Credit MoveLine : {}, Amount : {}, Can be zero balance ? {} )",
          debitMoveLine.getMove().getCompany(),
          debitMoveLine.getName(),
          creditMoveLine.getName(),
          amount,
          canBeZeroBalanceOk);

      Reconcile reconcile =
          new Reconcile(
              debitMoveLine.getMove().getCompany(),
              amount.setScale(2, RoundingMode.HALF_UP),
              debitMoveLine,
              creditMoveLine,
              ReconcileRepository.STATUS_DRAFT,
              canBeZeroBalanceOk);

      if (!moveToolService.isDebitMoveLine(debitMoveLine)) {

        reconcile.setDebitMoveLine(creditMoveLine);
        reconcile.setCreditMoveLine(debitMoveLine);
      }

      return reconcileRepository.save(reconcile);
    }
    return null;
  }

  /**
   * Permet de confirmer une réconciliation On ne peut réconcilier que des moveLine ayant le même
   * compte
   *
   * @param reconcile Une reconciliation
   * @return L'etat de la reconciliation
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Reconcile confirmReconcile(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException {

    reconcile = initReconcileConfirmation(reconcile, updateInvoicePayments, updateInvoiceTerms);

    if (updateInvoicePayments) {
      this.updatePayments(reconcile, updateInvoiceTerms);
    }
    this.addToReconcileGroup(reconcile);

    return reconcileRepository.save(reconcile);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Reconcile initReconcileConfirmation(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerm)
      throws AxelorException {

    this.reconcilePreconditions(reconcile, updateInvoicePayments, updateInvoiceTerm);

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    // Add the reconciled amount to the reconciled amount in the move line
    creditMoveLine.setAmountPaid(creditMoveLine.getAmountPaid().add(reconcile.getAmount()));
    debitMoveLine.setAmountPaid(debitMoveLine.getAmountPaid().add(reconcile.getAmount()));

    reconcile = reconcileRepository.save(reconcile);

    reconcile.setStatusSelect(ReconcileRepository.STATUS_CONFIRMED);

    if (reconcile.getCanBeZeroBalanceOk()) {
      // Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte
      // transitoire si le seuil est respecté
      canBeZeroBalance(reconcile);
    }

    reconcile.setReconciliationDateTime(
        appBaseService.getTodayDateTime(reconcile.getCompany()).toLocalDateTime());

    reconcileSequenceService.setSequence(reconcile);

    this.updatePartnerAccountingSituation(reconcile);
    this.updateInvoiceCompanyInTaxTotalRemaining(reconcile);
    this.updatePaymentTax(reconcile);
    this.updatePaymentMoveLineDistribution(reconcile);
    return reconcile;
  }

  @Override
  public void addToReconcileGroup(Reconcile reconcile) throws AxelorException {
    ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);

    ReconcileGroup reconcileGroup = reconcileGroupService.findOrCreateGroup(reconcile);

    reconcileGroupService.addAndValidate(reconcileGroup, reconcile);
  }

  @Override
  public void reconcilePreconditions(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException {

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    if (debitMoveLine == null || creditMoveLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.RECONCILE_1),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    // Check if move lines companies are the same as the reconcile company
    Company reconcileCompany = reconcile.getCompany();
    Company debitMoveLineCompany = debitMoveLine.getMove().getCompany();
    Company creditMoveLineCompany = creditMoveLine.getMove().getCompany();
    if (!debitMoveLineCompany.equals(reconcileCompany)
        && !creditMoveLineCompany.equals(reconcileCompany)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(AccountExceptionMessage.RECONCILE_7),
              I18n.get(BaseExceptionMessage.EXCEPTION),
              debitMoveLineCompany,
              creditMoveLineCompany,
              reconcileCompany));
    }

    // Check if move lines accounts are the same (debit and credit)
    if (!creditMoveLine.getAccount().equals(debitMoveLine.getAccount())) {
      log.debug(
          "Credit move line account : {} , Debit move line account : {}",
          creditMoveLine.getAccount(),
          debitMoveLine.getAccount());

      // Check if move lines accounts are compatible
      if (!debitMoveLine
          .getAccount()
          .getCompatibleAccountSet()
          .contains(creditMoveLine.getAccount())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.RECONCILE_2),
            I18n.get(BaseExceptionMessage.EXCEPTION));
      }
    }

    // Check if the amount to reconcile is != zero
    if (reconcile.getAmount() == null || reconcile.getAmount().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_4),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          reconcile.getReconcileSeq(),
          debitMoveLine.getName(),
          debitMoveLine.getAccount().getLabel(),
          creditMoveLine.getName(),
          creditMoveLine.getAccount().getLabel());
    }

    if (reconcile
                .getAmount()
                .compareTo(creditMoveLine.getCredit().subtract(creditMoveLine.getAmountPaid()))
            > 0
        || reconcile
                .getAmount()
                .compareTo(debitMoveLine.getDebit().subtract(debitMoveLine.getAmountPaid()))
            > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_5)
              + " "
              + I18n.get(AccountExceptionMessage.RECONCILE_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          reconcile.getReconcileSeq(),
          reconcile.getAmount(),
          debitMoveLine.getName(),
          debitMoveLine.getAccount().getLabel(),
          debitMoveLine.getDebit().subtract(debitMoveLine.getAmountPaid()),
          creditMoveLine.getName(),
          creditMoveLine.getAccount().getLabel(),
          creditMoveLine.getCredit().subtract(creditMoveLine.getAmountPaid()));
    }

    // Check tax lines
    this.taxLinePrecondition(creditMoveLine.getMove());
    this.taxLinePrecondition(debitMoveLine.getMove());

    if (updateInvoiceTerms && updateInvoicePayments) {
      invoiceTermPfpService.validatePfpValidatedAmount(
          debitMoveLine, creditMoveLine, reconcile.getAmount(), reconcile.getCompany());
    }
  }

  protected void taxLinePrecondition(Move move) throws AxelorException {
    if (move.getMoveLineList().stream()
        .anyMatch(
            it ->
                it.getTaxLine() == null
                    && it.getAccount()
                        .getAccountType()
                        .getTechnicalTypeSelect()
                        .equals(AccountTypeRepository.TYPE_TAX)
                    && it.getAccount().getIsTaxAuthorizedOnMoveLine())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          AccountExceptionMessage.RECONCILE_MISSING_TAX,
          move.getReference());
    }
  }

  @Override
  public void updatePartnerAccountingSituation(Reconcile reconcile) throws AxelorException {

    List<Partner> partnerList = this.getPartners(reconcile);

    if (partnerList != null && !partnerList.isEmpty()) {

      Company company = reconcile.getDebitMoveLine().getMove().getCompany();

      if (AccountingService.getUpdateCustomerAccount()) {
        accountCustomerService.updatePartnerAccountingSituation(
            partnerList, company, true, true, false);
      } else {
        accountCustomerService.flagPartners(partnerList, company);
      }
    }
  }

  @Override
  public List<Partner> getPartners(Reconcile reconcile) {

    List<Partner> partnerList = Lists.newArrayList();
    Partner debitPartner = reconcile.getDebitMoveLine().getPartner();
    Partner creditPartner = reconcile.getCreditMoveLine().getPartner();
    if (debitPartner != null && creditPartner != null && debitPartner.equals(creditPartner)) {
      partnerList.add(debitPartner);
    } else if (debitPartner != null) {
      partnerList.add(debitPartner);
    } else if (creditPartner != null) {
      partnerList.add(creditPartner);
    }

    return partnerList;
  }

  public void updateInvoiceCompanyInTaxTotalRemaining(Reconcile reconcile) throws AxelorException {

    Invoice debitInvoice = reconcile.getDebitMoveLine().getMove().getInvoice();
    Invoice creditInvoice = reconcile.getCreditMoveLine().getMove().getInvoice();

    // Update amount remaining on invoice or refund
    if (debitInvoice != null) {

      debitInvoice.setCompanyInTaxTotalRemaining(
          moveToolService.getInTaxTotalRemaining(debitInvoice));
    }
    if (creditInvoice != null) {

      creditInvoice.setCompanyInTaxTotalRemaining(
          moveToolService.getInTaxTotalRemaining(creditInvoice));
    }
  }

  public void updatePayments(Reconcile reconcile, boolean updateInvoiceTerms)
      throws AxelorException {
    InvoiceRepository invoiceRepository = Beans.get(InvoiceRepository.class);

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();
    Move debitMove = debitMoveLine.getMove();
    Move creditMove = creditMoveLine.getMove();
    Invoice debitInvoice = invoiceRepository.findByMove(debitMove);
    if (debitInvoice == null) {
      debitInvoice = invoiceRepository.findByOldMove(debitMove);
      debitInvoice =
          debitInvoice != null ? (debitInvoice.getLcrAccounted() ? debitInvoice : null) : null;
    }
    Invoice creditInvoice = invoiceRepository.findByMove(creditMove);
    if (creditInvoice == null) {
      creditInvoice = invoiceRepository.findByOldMove(creditMove);
      creditInvoice =
          creditInvoice != null ? (creditInvoice.getLcrAccounted() ? creditInvoice : null) : null;
    }
    BigDecimal amount = reconcile.getAmount();

    this.checkCurrencies(debitMoveLine, creditMoveLine);

    this.updatePayment(
        reconcile,
        debitMoveLine,
        creditMoveLine,
        debitInvoice,
        debitMove,
        creditMove,
        amount,
        updateInvoiceTerms);
    this.updatePayment(
        reconcile,
        creditMoveLine,
        debitMoveLine,
        creditInvoice,
        creditMove,
        debitMove,
        amount,
        updateInvoiceTerms);
  }

  protected void checkCurrencies(MoveLine debitMoveLine, MoveLine creditMoveLine)
      throws AxelorException {
    Currency debitCurrency = debitMoveLine.getMove().getCurrency();
    Currency creditCurrency = creditMoveLine.getMove().getCurrency();
    Currency companyCurrency = debitMoveLine.getMove().getCompanyCurrency();

    if (!Objects.equals(debitCurrency, creditCurrency)
        && !Objects.equals(debitCurrency, companyCurrency)
        && !Objects.equals(creditCurrency, companyCurrency)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_WRONG_CURRENCY));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void updatePayment(
      Reconcile reconcile,
      MoveLine moveLine,
      MoveLine otherMoveLine,
      Invoice invoice,
      Move move,
      Move otherMove,
      BigDecimal amount,
      boolean updateInvoiceTerms)
      throws AxelorException {
    InvoicePayment invoicePayment = null;
    if (invoice != null
        && otherMove.getFunctionalOriginSelect()
            != MoveRepository.FUNCTIONAL_ORIGIN_DOUBTFUL_CUSTOMER) {
      if (otherMove.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_IRRECOVERABLE) {
        invoicePayment = invoicePaymentRepo.findByReconcileAndInvoice(reconcile, invoice);

        if (invoicePayment == null) {
          invoicePayment = this.getExistingInvoicePayment(invoice, otherMove);
        }
      }

      if (!this.isCompanyCurrency(reconcile, invoicePayment, otherMove)) {
        amount = this.getTotal(moveLine, otherMoveLine, amount, invoicePayment != null);
      }

      if (invoicePayment == null
          && moveLine.getAccount().getUseForPartnerBalance()
          && otherMoveLine.getAccount().getUseForPartnerBalance()) {
        invoicePayment =
            invoicePaymentCreateService.createInvoicePayment(invoice, amount, otherMove);
        invoicePayment.setReconcile(reconcile);
      }
    } else if (!this.isCompanyCurrency(reconcile, invoicePayment, otherMove)) {
      amount = this.getTotal(moveLine, otherMoveLine, amount, false);
    }

    List<InvoiceTermPayment> invoiceTermPaymentList = null;
    if (moveLine.getAccount().getUseForPartnerBalance() && updateInvoiceTerms) {
      List<InvoiceTerm> invoiceTermList = this.getInvoiceTermsToPay(invoice, otherMove, moveLine);
      invoiceTermPaymentList =
          this.updateInvoiceTerms(invoiceTermList, invoicePayment, amount, reconcile);
    }

    if (invoicePayment != null) {
      invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
      invoicePaymentRepo.save(invoicePayment);
    } else if (!ObjectUtils.isEmpty(invoiceTermPaymentList)) {
      invoiceTermPaymentList.forEach(it -> invoiceTermPaymentRepo.save(it));
    }
  }

  protected boolean isCompanyCurrency(
      Reconcile reconcile, InvoicePayment invoicePayment, Move otherMove) {
    Currency currency;
    if (invoicePayment != null) {
      currency = invoicePayment.getCurrency();
    } else {
      currency = otherMove.getCurrency();
      if (currency == null) {
        currency = otherMove.getCompanyCurrency();
      }
    }

    return currency.equals(reconcile.getCompany().getCurrency());
  }

  protected BigDecimal getTotal(
      MoveLine moveLine, MoveLine otherMoveLine, BigDecimal amount, boolean isInvoicePayment) {
    BigDecimal total;
    BigDecimal moveLineAmount = moveLine.getCredit().add(moveLine.getDebit());
    BigDecimal rate = moveLine.getCurrencyRate();
    BigDecimal invoiceAmount =
        moveLine.getAmountPaid().add(moveLineAmount.subtract(moveLine.getAmountPaid()));
    BigDecimal computedAmount =
        moveLineAmount
            .divide(rate, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)
            .multiply(rate);

    // Recompute currency rate to avoid rounding issue
    total = amount.divide(rate, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    if (total.stripTrailingZeros().scale() > AppBaseService.DEFAULT_NB_DECIMAL_DIGITS) {
      total =
          computePaidRatio(moveLineAmount, amount, invoiceAmount, computedAmount, isInvoicePayment)
              .multiply(moveLine.getCurrencyAmount().abs());
    }

    total = total.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    if (amount.compareTo(otherMoveLine.getCredit().max(otherMoveLine.getDebit())) == 0
        && total.compareTo(otherMoveLine.getCurrencyAmount()) != 0) {
      total = otherMoveLine.getCurrencyAmount();
    }

    return total;
  }

  protected BigDecimal computePaidRatio(
      BigDecimal moveLineAmount,
      BigDecimal amountToPay,
      BigDecimal invoiceAmount,
      BigDecimal computedAmount,
      boolean isInvoicePayment) {
    BigDecimal ratioPaid = BigDecimal.ONE;
    int scale = AppBaseService.DEFAULT_NB_DECIMAL_DIGITS;
    BigDecimal percentage = amountToPay.divide(computedAmount, scale, RoundingMode.HALF_UP);

    if (isInvoicePayment) {
      // ReCompute percentage paid when it's partial payment with invoice payment
      percentage =
          amountToPay.divide(
              invoiceAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    } else if (moveLineAmount
            .multiply(percentage)
            .setScale(scale, RoundingMode.HALF_UP)
            .compareTo(amountToPay)
        != 0) {
      // Compute ratio paid when it's invoice term partial payment
      if (amountToPay.compareTo(invoiceAmount) != 0) {
        percentage = invoiceAmount.divide(computedAmount, scale, RoundingMode.HALF_UP);
      } else {
        percentage =
            invoiceAmount.divide(
                computedAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
      }
      ratioPaid =
          amountToPay.divide(
              invoiceAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    }

    return ratioPaid.multiply(percentage);
  }

  @Override
  public List<InvoiceTermPayment> updateInvoiceTerms(
      List<InvoiceTerm> invoiceTermList,
      InvoicePayment invoicePayment,
      BigDecimal amount,
      Reconcile reconcile)
      throws AxelorException {
    List<InvoiceTermPayment> invoiceTermPaymentList = new ArrayList<>();
    if (invoiceTermList != null) {
      invoiceTermPaymentList =
          invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
              invoicePayment, invoiceTermList, amount, reconcile.getAmount());

      for (InvoiceTermPayment invoiceTermPayment : invoiceTermPaymentList) {
        invoiceTermService.updateInvoiceTermsPaidAmount(
            invoicePayment, invoiceTermPayment.getInvoiceTerm(), invoiceTermPayment);

        if (invoicePayment == null) {
          invoiceTermPayment.addReconcileListItem(reconcile);
        }
      }
    }
    return invoiceTermPaymentList;
  }

  @Override
  public void checkReconcile(Reconcile reconcile) throws AxelorException {
    this.checkMoveLine(reconcile, reconcile.getCreditMoveLine());
    this.checkMoveLine(reconcile, reconcile.getDebitMoveLine());
  }

  protected void checkMoveLine(Reconcile reconcile, MoveLine moveLine) throws AxelorException {
    LocalDate reconciliationDateTime =
        Optional.ofNullable(reconcile.getReconciliationDateTime())
            .map(LocalDateTime::toLocalDate)
            .orElse(null);
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())
        && !invoiceTermService.isEnoughAmountToPay(
            moveLine.getInvoiceTermList(), reconcile.getAmount(), reconciliationDateTime)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_NOT_ENOUGH_AMOUNT));
    }
  }

  protected List<InvoiceTerm> getInvoiceTermsToPay(Invoice invoice, Move move, MoveLine moveLine)
      throws AxelorException {
    if (move != null
        && move.getPaymentVoucher() != null
        && CollectionUtils.isNotEmpty(move.getPaymentVoucher().getPayVoucherElementToPayList())) {
      return move.getPaymentVoucher().getPayVoucherElementToPayList().stream()
          .filter(it -> it.getMoveLine().equals(moveLine) && !it.getInvoiceTerm().getIsPaid())
          .sorted(Comparator.comparing(PayVoucherElementToPay::getSequence))
          .map(PayVoucherElementToPay::getInvoiceTerm)
          .collect(Collectors.toList());
    } else {
      List<InvoiceTerm> invoiceTermsToPay = null;
      if (invoice != null && CollectionUtils.isNotEmpty(invoice.getInvoiceTermList())) {
        invoiceTermsToPay =
            invoiceTermService.getUnpaidInvoiceTermsFilteredWithoutPfpCheck(invoice);

      } else if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
        invoiceTermsToPay = this.getInvoiceTermsFromMoveLine(moveLine.getInvoiceTermList());

      } else {
        return null;
      }
      if (CollectionUtils.isNotEmpty(invoiceTermsToPay)
          && move != null
          && move.getPaymentSession() != null) {
        return invoiceTermsToPay.stream()
            .filter(
                it ->
                    it.getPaymentSession() != null
                        && it.getPaymentSession().equals(move.getPaymentSession()))
            .collect(Collectors.toList());
      } else {
        return invoiceTermsToPay;
      }
    }
  }

  protected List<InvoiceTerm> getInvoiceTermsFromMoveLine(List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream()
        .filter(it -> !it.getIsPaid())
        .sorted(this::compareInvoiceTerm)
        .collect(Collectors.toList());
  }

  protected int compareInvoiceTerm(InvoiceTerm invoiceTerm1, InvoiceTerm invoiceTerm2) {
    LocalDate date1, date2;

    if (invoiceTerm1.getEstimatedPaymentDate() != null
        && invoiceTerm2.getEstimatedPaymentDate() != null) {
      date1 = invoiceTerm1.getEstimatedPaymentDate();
      date2 = invoiceTerm2.getEstimatedPaymentDate();
    } else {
      date1 = invoiceTerm1.getDueDate();
      date2 = invoiceTerm2.getDueDate();
    }

    return date1.compareTo(date2);
  }

  protected InvoicePayment getExistingInvoicePayment(Invoice invoice, Move move) {
    return invoice.getInvoicePaymentList().stream()
        .filter(
            it -> (it.getMove() != null && it.getMove().equals(move) && it.getReconcile() == null))
        .findFirst()
        .orElse(null);
  }

  /**
   * @deprecated use {@link #updatePaymentTax(Reconcile)} instead
   * @param reconcile
   * @throws AxelorException
   */
  @Deprecated
  protected void udpatePaymentTax(Reconcile reconcile) throws AxelorException {
    updatePaymentTax(reconcile);
  }

  protected void updatePaymentTax(Reconcile reconcile) throws AxelorException {
    Move debitMove = reconcile.getDebitMoveLine().getMove();
    Move creditMove = reconcile.getCreditMoveLine().getMove();

    if (debitMove.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
      moveLineTaxService.generateTaxPaymentMoveLineList(
          reconcile.getDebitMoveLine(), reconcile.getCreditMoveLine(), reconcile);
    }
    if (creditMove.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
      moveLineTaxService.generateTaxPaymentMoveLineList(
          reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine(), reconcile);
    }
  }

  /**
   * Méthode permettant de lettrer une écriture au débit avec une écriture au crédit
   *
   * @param debitMoveLine
   * @param creditMoveLine
   * @throws AxelorException
   */
  @Override
  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments,
      InvoicePayment invoicePayment)
      throws AxelorException {
    return this.reconcile(
        debitMoveLine, creditMoveLine, null, canBeZeroBalanceOk, updateInvoicePayments);
  }

  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      InvoicePayment invoicePayment,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments)
      throws AxelorException {
    BigDecimal amount = debitMoveLine.getAmountRemaining().min(creditMoveLine.getAmountRemaining());
    Reconcile reconcile =
        this.createReconcile(debitMoveLine, creditMoveLine, amount, canBeZeroBalanceOk);

    if (reconcile != null) {
      if (invoicePayment != null) {
        invoicePayment.setReconcile(reconcile);
      }

      this.confirmReconcile(reconcile, updateInvoicePayments, true);
      return reconcile;
    }

    return null;
  }

  /**
   * Méthode permettant de lettrer une écriture au débit avec une écriture au crédit
   *
   * @param debitMoveLine
   * @param creditMoveLine
   * @throws AxelorException
   */
  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments)
      throws AxelorException {
    return this.reconcile(
        debitMoveLine, creditMoveLine, canBeZeroBalanceOk, updateInvoicePayments, null);
  }

  /**
   * Permet de déréconcilier
   *
   * @param reconcile Une reconciliation
   * @return L'etat de la réconciliation
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void unreconcile(Reconcile reconcile) throws AxelorException {

    log.debug("unreconcile : reconcile : {}", reconcile);

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();
    Invoice invoice = debitMoveLine.getMove().getInvoice();

    // Change the state
    reconcile.setStatusSelect(ReconcileRepository.STATUS_CANCELED);
    reconcile.setReconciliationCancelDateTime(
        appBaseService.getTodayDateTime(reconcile.getCompany()).toLocalDateTime());
    // Add the reconciled amount to the reconciled amount in the move line
    creditMoveLine.setAmountPaid(creditMoveLine.getAmountPaid().subtract(reconcile.getAmount()));
    debitMoveLine.setAmountPaid(debitMoveLine.getAmountPaid().subtract(reconcile.getAmount()));

    reconcileRepository.save(reconcile);

    // Update amount remaining on invoice or refund
    this.updatePartnerAccountingSituation(reconcile);
    this.updateInvoiceCompanyInTaxTotalRemaining(reconcile);
    this.updateInvoiceTermsAmountRemaining(reconcile);
    this.updateInvoicePaymentsCanceled(reconcile);
    this.reverseTaxPaymentMoveLines(reconcile);
    this.reversePaymentMoveLineDistributionLines(reconcile);
    if (invoice != null
        && invoice.getSubrogationRelease() != null
        && invoice.getSubrogationRelease().getStatusSelect()
            != SubrogationReleaseRepository.STATUS_ACCOUNTED) {
      subrogationReleaseWorkflowService.goBackToAccounted(invoice.getSubrogationRelease());
    }
    // Update reconcile group
    Beans.get(ReconcileGroupService.class).remove(reconcile);
  }

  protected void reverseTaxPaymentMoveLines(Reconcile reconcile) throws AxelorException {
    Move debitMove = reconcile.getDebitMoveLine().getMove();
    Move creditMove = reconcile.getCreditMoveLine().getMove();
    Invoice debitInvoice = debitMove.getInvoice();
    Invoice creditInvoice = creditMove.getInvoice();
    if (debitInvoice == null) {
      moveLineTaxService.reverseTaxPaymentMoveLines(reconcile.getDebitMoveLine(), reconcile);
    }
    if (creditInvoice == null) {
      moveLineTaxService.reverseTaxPaymentMoveLines(reconcile.getCreditMoveLine(), reconcile);
    }
  }

  /** @param reconcile */
  protected void updatePaymentMoveLineDistribution(Reconcile reconcile) {
    // FIXME This feature will manage at a first step only reconcile of purchase (journal type of
    // type purchase)
    Move purchaseMove = reconcile.getCreditMoveLine().getMove();
    if (!purchaseMove.getJournal().getJournalType().getCode().equals("ACH")
        || purchaseMove.getPartner() == null) {
      return;
    }
    paymentMoveLineDistributionService.generatePaymentMoveLineDistributionList(
        purchaseMove, reconcile);
  }

  protected void reversePaymentMoveLineDistributionLines(Reconcile reconcile) {
    // FIXME This feature will manage at a first step only reconcile of purchase (journal type of
    // type purchase)
    Move purchaseMove = reconcile.getCreditMoveLine().getMove();
    if (!purchaseMove.getJournal().getJournalType().getCode().equals("ACH")
        || CollectionUtils.isEmpty(reconcile.getPaymentMoveLineDistributionList())) {
      return;
    }
    paymentMoveLineDistributionService.reversePaymentMoveLineDistributionList(reconcile);
  }

  public void updateInvoicePaymentsCanceled(Reconcile reconcile) throws AxelorException {

    log.debug("updateInvoicePaymentsCanceled : reconcile : {}", reconcile);
    for (InvoicePayment invoicePayment : invoicePaymentRepo.findByReconcile(reconcile).fetch()) {
      invoicePaymentCancelService.updateCancelStatus(invoicePayment);
    }

    invoiceTermPaymentRepo
        .findByReconcileId(reconcile.getId())
        .fetch()
        .forEach(it -> it.setInvoiceTerm(null));
  }

  public void updateInvoiceTermsAmountRemaining(Reconcile reconcile) throws AxelorException {

    log.debug("updateInvoiceTermsAmountRemaining : reconcile : {}", reconcile);

    List<InvoicePayment> invoicePaymentList = invoicePaymentRepo.findByReconcile(reconcile).fetch();

    if (!invoicePaymentList.isEmpty()) {
      for (InvoicePayment invoicePayment : invoicePaymentList) {
        invoiceTermService.updateInvoiceTermsAmountRemaining(invoicePayment);
      }
    }

    List<InvoiceTermPayment> invoiceTermPaymentList =
        invoiceTermPaymentRepo.findByReconcileId(reconcile.getId()).fetch();

    if (CollectionUtils.isNotEmpty(invoiceTermPaymentList)) {
      invoiceTermService.updateInvoiceTermsAmountRemaining(invoiceTermPaymentList);
    }
  }

  /**
   * Procédure permettant de gérer les écarts de règlement, check sur la case à cocher 'Peut être
   * soldé' Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte
   * transitoire si le seuil est respecté
   *
   * @param reconcile Une reconciliation
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void canBeZeroBalance(Reconcile reconcile) throws AxelorException {

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();

    BigDecimal debitAmountRemaining = debitMoveLine.getAmountRemaining();
    log.debug("Debit amount to pay / to reconcile: {}", debitAmountRemaining);
    if (debitAmountRemaining.compareTo(BigDecimal.ZERO) > 0) {
      Company company = reconcile.getDebitMoveLine().getMove().getCompany();

      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      if (debitAmountRemaining.plus().compareTo(accountConfig.getThresholdDistanceFromRegulation())
              < 0
          || reconcile.getMustBeZeroBalanceOk()) {

        log.debug("Threshold respected");

        MoveLine creditAdjustMoveLine =
            moveAdjustementService.createAdjustmentCreditMove(debitMoveLine);

        // Création de la réconciliation
        Reconcile newReconcile =
            this.createReconcile(debitMoveLine, creditAdjustMoveLine, debitAmountRemaining, false);
        if (newReconcile != null) {
          this.confirmReconcile(newReconcile, true, true);
          reconcileRepository.save(newReconcile);
        }
      }
    }

    reconcile.setCanBeZeroBalanceOk(false);
    log.debug("End of payment difference management");
  }

  /**
   * Solder le trop-perçu si il respect les règles de seuil
   *
   * @param creditMoveLine
   * @throws AxelorException
   */
  @Override
  public void balanceCredit(MoveLine creditMoveLine) throws AxelorException {
    if (creditMoveLine != null) {
      BigDecimal creditAmountRemaining = creditMoveLine.getAmountRemaining();
      log.debug("Credit amount to pay / to reconcile: {}", creditAmountRemaining);

      if (creditAmountRemaining.compareTo(BigDecimal.ZERO) > 0) {
        AccountConfig accountConfig =
            accountConfigService.getAccountConfig(creditMoveLine.getMove().getCompany());

        if (creditAmountRemaining
                .plus()
                .compareTo(accountConfig.getThresholdDistanceFromRegulation())
            < 0) {

          log.debug("Threshold respected");

          MoveLine debitAdjustmentMoveLine =
              moveAdjustementService.createAdjustmentCreditMove(creditMoveLine);

          // Création de la réconciliation
          Reconcile newReconcile =
              this.createReconcile(
                  debitAdjustmentMoveLine, creditMoveLine, creditAmountRemaining, false);
          if (newReconcile != null) {
            this.confirmReconcile(newReconcile, true, true);
            reconcileRepository.save(newReconcile);
          }
        }
      }
    }
  }

  @Override
  public List<Reconcile> getReconciles(MoveLine moveLine) {

    List<Reconcile> debitReconcileList = moveLine.getDebitReconcileList();
    List<Reconcile> creditReconcileList = moveLine.getCreditReconcileList();

    if (moveToolService.isDebitMoveLine(moveLine)) {
      return debitReconcileList;
    } else if (debitReconcileList != null && !creditReconcileList.isEmpty()) {
      return creditReconcileList;
    }
    return Lists.newArrayList();
  }

  @Override
  public String getStringAllowedCreditMoveLines(Reconcile reconcile) {
    return getAllowedCreditMoveLines(reconcile).stream()
        .map(Objects::toString)
        .collect(Collectors.joining(","));
  }

  @Override
  public String getStringAllowedDebitMoveLines(Reconcile reconcile) {
    return getAllowedDebitMoveLines(reconcile).stream()
        .map(Objects::toString)
        .collect(Collectors.joining(","));
  }

  @Override
  public List<Long> getAllowedCreditMoveLines(Reconcile reconcile) {
    StringBuilder moveLineQueryStr = new StringBuilder("self.credit > 0");
    return getMoveLineIds(reconcile, moveLineQueryStr, reconcile.getDebitMoveLine());
  }

  @Override
  public List<Long> getAllowedDebitMoveLines(Reconcile reconcile) {

    StringBuilder moveLineQueryStr = new StringBuilder("self.debit > 0");
    return getMoveLineIds(reconcile, moveLineQueryStr, reconcile.getCreditMoveLine());
  }

  protected List<Long> getMoveLineIds(
      Reconcile reconcile, StringBuilder moveLineQueryStr, MoveLine otherMoveLine) {
    computeMoveLineDomain(moveLineQueryStr, otherMoveLine);

    Query<MoveLine> moveLineQuery = getMoveLineQuery(reconcile, moveLineQueryStr);

    moveLineQuery = setQueryBindings(moveLineQuery, otherMoveLine);

    return moveLineQuery.fetch().stream()
        .filter(moveLineControlService::canReconcile)
        .map(MoveLine::getId)
        .collect(Collectors.toList());
  }

  protected Query<MoveLine> getMoveLineQuery(Reconcile reconcile, StringBuilder moveLineQueryStr) {
    return moveLineRepo
        .all()
        .filter(moveLineQueryStr.toString())
        .bind("company", reconcile.getCompany());
  }

  protected void computeMoveLineDomain(StringBuilder moveLineQueryStr, MoveLine otherMoveLine) {

    moveLineQueryStr.append(
        " AND self.move.statusSelect IN (2,3) AND self.move.company = :company AND self.account.reconcileOk IS TRUE");

    if (otherMoveLine == null) {
      return;
    }

    moveLineQueryStr.append(" AND self.account = :account");

    if (otherMoveLine.getAccount().getUseForPartnerBalance()
        && otherMoveLine.getPartner() != null) {
      moveLineQueryStr.append(" AND self.partner = :partner");
    }
  }

  protected Query<MoveLine> setQueryBindings(
      Query<MoveLine> moveLineQuery, MoveLine otherMoveLine) {

    Account account = null;
    Partner partner = null;

    if (otherMoveLine != null) {
      account = otherMoveLine.getAccount();
      partner = otherMoveLine.getPartner();
    }

    if (account != null) {
      moveLineQuery = moveLineQuery.bind("account", account);

      if (account.getUseForPartnerBalance() && partner != null) {
        moveLineQuery.bind("partner", partner);
      }
    }

    return moveLineQuery;
  }
}
