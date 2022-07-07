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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.account.service.move.MoveCreateFromInvoiceService;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class VentilateState extends WorkflowInvoice {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected SequenceService sequenceService;

  protected MoveCreateFromInvoiceService moveCreateFromInvoiceService;

  protected AccountConfigService accountConfigService;

  protected AppAccountService appAccountService;

  protected InvoiceRepository invoiceRepo;

  protected WorkflowVentilationService workflowService;

  protected UserService userService;

  protected FixedAssetService fixedAssetService;

  @Inject
  public VentilateState(
      SequenceService sequenceService,
      MoveCreateFromInvoiceService moveCreateFromInvoiceService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      InvoiceRepository invoiceRepo,
      WorkflowVentilationService workflowService,
      UserService userService,
      FixedAssetService fixedAssetService) {
    this.sequenceService = sequenceService;
    this.moveCreateFromInvoiceService = moveCreateFromInvoiceService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.invoiceRepo = invoiceRepo;
    this.workflowService = workflowService;
    this.userService = userService;
    this.fixedAssetService = fixedAssetService;
  }

  @Override
  public void init(Invoice invoice) {
    this.invoice = invoice;
  }

  @Override
  public void process() throws AxelorException {

    Preconditions.checkNotNull(invoice.getPartner());

    setDate();
    setJournal();
    setPartnerAccount();
    setInvoiceId();
    updatePaymentSchedule();
    setMove();
    generateFixedAsset();
    setStatus();
    setVentilatedLog();

    workflowService.afterVentilation(invoice);
  }

  protected void setVentilatedLog() {
    invoice.setVentilatedDate(appAccountService.getTodayDate(invoice.getCompany()));
    invoice.setVentilatedByUser(userService.getUser());
  }

  protected void updatePaymentSchedule() {

    if (invoice.getPaymentSchedule() != null) {
      invoice.getPaymentSchedule().addInvoiceSetItem(invoice);
    }
  }

  protected void setPartnerAccount() throws AxelorException {
    // Partner account is actually set upon validation but we keep this for backward compatibility
    if (invoice.getPartnerAccount() == null) {
      Account account = Beans.get(InvoiceService.class).getPartnerAccount(invoice);

      if (account == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.VENTILATE_STATE_5));
      }

      if (invoice.getPartner() != null) {
        FiscalPosition fiscalPosition = invoice.getFiscalPosition();
        account = Beans.get(FiscalPositionAccountService.class).getAccount(fiscalPosition, account);
      }
      invoice.setPartnerAccount(account);
    }
    Account partnerAccount = invoice.getPartnerAccount();
    if (!partnerAccount.getReconcileOk() || !partnerAccount.getUseForPartnerBalance()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ACCOUNT_RECONCILABLE_USE_FOR_PARTNER_BALANCE));
    }
  }

  protected void setJournal() throws AxelorException {
    // Journal is actually set upon validation but we keep this for backward compatibility
    if (invoice.getJournal() == null) {
      invoice.setJournal(Beans.get(InvoiceService.class).getJournal(invoice));
    }
  }

  protected void setDate() throws AxelorException {

    LocalDate todayDate = appAccountService.getTodayDate(invoice.getCompany());

    if (invoice.getInvoiceDate() == null) {
      invoice.setInvoiceDate(todayDate);
    } else if (invoice.getInvoiceDate().isAfter(todayDate)) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.VENTILATE_STATE_FUTURE_DATE),
          invoice.getInvoiceId());
    }

    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    if (isPurchase && invoice.getOriginDate() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.VENTILATE_STATE_MISSING_ORIGIN_DATE));
    }
    if (isPurchase && invoice.getOriginDate().isAfter(todayDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.VENTILATE_STATE_FUTURE_ORIGIN_DATE));
    }

    if ((invoice.getPaymentCondition() != null && !invoice.getPaymentCondition().getIsFree())
        || invoice.getDueDate() == null) {
      invoice.setDueDate(this.getDueDate());
    }
  }

  /**
   * - Without reset : assure that he doesn't exist invoice with an invoice date greater than the
   * current invoice date. - With monthly reset : determine the sequence using the Max number stored
   * on ventilated invoice on the same month. - With year reset : determine the sequence using the
   * Max number stored on ventilated invoice on the same year.
   *
   * @param sequence
   * @throws AxelorException
   */
  protected void checkInvoiceDate(Sequence sequence) throws AxelorException {

    String query =
        "self.statusSelect = :ventilated AND self.invoiceDate > :invoiceDate AND self.operationTypeSelect = :operationTypeSelect AND self.company = :company ";
    Map<String, Object> params = new HashMap<>();
    params.put("ventilated", InvoiceRepository.STATUS_VENTILATED);
    params.put("invoiceDate", invoice.getInvoiceDate());
    params.put("operationTypeSelect", invoice.getOperationTypeSelect());
    params.put("company", invoice.getCompany());

    if (sequence.getMonthlyResetOk()) {

      query += "AND EXTRACT (month from self.invoiceDate) = :month ";
      params.put("month", invoice.getInvoiceDate().getMonthValue());
    }
    if (sequence.getYearlyResetOk()) {

      query += "AND EXTRACT (year from self.invoiceDate) = :year ";
      params.put("year", invoice.getInvoiceDate().getYear());
    }
    if (invoiceRepo.all().filter(query).bind(params).count() > 0) {
      Invoice lastInvoice =
          invoiceRepo.all().filter(query).bind(params).order("invoiceDate").fetchOne();
      if (sequence.getMonthlyResetOk()) {
        throw new AxelorException(
            sequence,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.VENTILATE_STATE_2),
            lastInvoice.getInvoiceDate().getMonth().toString());
      }
      if (sequence.getYearlyResetOk()) {
        throw new AxelorException(
            sequence,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.VENTILATE_STATE_3),
            Integer.toString(lastInvoice.getInvoiceDate().getYear()));
      }
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.VENTILATE_STATE_1),
          lastInvoice.getInvoiceDate().toString());
    }
  }

  protected LocalDate getDueDate() throws AxelorException {

    if (InvoiceToolService.isPurchase(invoice)) {

      return InvoiceToolService.getDueDate(invoice.getPaymentCondition(), invoice.getOriginDate());
    }

    return InvoiceToolService.getDueDate(invoice.getPaymentCondition(), invoice.getInvoiceDate());
  }

  protected void setMove() throws AxelorException {

    if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    log.debug("In Set Move");
    // Création de l'écriture comptable
    Move move = moveCreateFromInvoiceService.createMove(invoice);
    if (move != null) {

      moveCreateFromInvoiceService.createMoveUseExcessPaymentOrDue(invoice);
    }
  }

  protected void generateFixedAsset() throws AxelorException {

    if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    log.debug("Generate fixed asset");
    // Create fixed asset
    fixedAssetService.createFixedAssets(invoice);
  }

  /**
   * Détermine le numéro de facture
   *
   * @throws AxelorException
   */
  protected void setStatus() {
    invoice.setStatusSelect(InvoiceRepository.STATUS_VENTILATED);
  }

  /**
   * Détermine le numéro de facture
   *
   * @throws AxelorException
   */
  protected void setInvoiceId() throws AxelorException {

    if (!sequenceService.isEmptyOrDraftSequenceNumber(invoice.getInvoiceId())) {
      return;
    }

    Sequence sequence = this.getSequence();

    if (!InvoiceToolService.isPurchase(invoice)) {
      this.checkInvoiceDate(sequence);
    }

    invoice.setInvoiceId(sequenceService.getSequenceNumber(sequence, invoice.getInvoiceDate()));

    if (invoice.getInvoiceId() != null) {
      return;
    }

    throw new AxelorException(
        invoice,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.VENTILATE_STATE_4),
        invoice.getCompany().getName());
  }

  protected Sequence getSequence() throws AxelorException {

    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());

    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        return accountConfigService.getSuppInvSequence(accountConfig);

      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        return accountConfigService.getSuppRefSequence(accountConfig);

      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        return accountConfigService.getCustInvSequence(accountConfig);

      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        return accountConfigService.getCustRefSequence(accountConfig);

      default:
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.JOURNAL_1),
            invoice.getInvoiceId());
    }
  }
}
