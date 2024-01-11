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
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Irrecoverable;
import com.axelor.apps.account.db.IrrecoverableCustomerLine;
import com.axelor.apps.account.db.IrrecoverableInvoiceLine;
import com.axelor.apps.account.db.IrrecoverablePaymentScheduleLineLine;
import com.axelor.apps.account.db.IrrecoverableReportLine;
import com.axelor.apps.account.db.ManagementObject;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.IrrecoverableCustomerLineRepository;
import com.axelor.apps.account.db.repo.IrrecoverableRepository;
import com.axelor.apps.account.db.repo.ManagementObjectRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrrecoverableService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected SequenceService sequenceService;
  protected MoveToolService moveToolService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineRepository moveLineRepo;
  protected ReconcileService reconcileService;
  protected TaxService taxService;
  protected TaxAccountService taxAccountService;
  protected PaymentScheduleService paymentScheduleService;
  protected PaymentScheduleRepository paymentScheduleRepo;
  protected PaymentScheduleLineRepository paymentScheduleLineRepo;
  protected AccountConfigService accountConfigService;
  protected IrrecoverableCustomerLineRepository irrecoverableCustomerLineRepo;
  protected InvoiceRepository invoiceRepo;
  protected ManagementObjectRepository managementObjectRepo;
  protected IrrecoverableRepository irrecoverableRepo;

  protected AppAccountService appAccountService;

  @Inject
  public IrrecoverableService(
      AppAccountService appAccountService,
      SequenceService sequenceService,
      MoveToolService moveToolService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      MoveLineRepository moveLineRepo,
      ReconcileService reconcileService,
      TaxService taxService,
      TaxAccountService taxAccountService,
      PaymentScheduleService paymentScheduleService,
      PaymentScheduleRepository paymentScheduleRepo,
      PaymentScheduleLineRepository paymentScheduleLineRepo,
      AccountConfigService accountConfigService,
      IrrecoverableCustomerLineRepository irrecoverableCustomerLineRepo,
      InvoiceRepository invoiceRepo,
      ManagementObjectRepository managementObjectRepo,
      IrrecoverableRepository irrecoverableRepo) {

    this.sequenceService = sequenceService;
    this.moveToolService = moveToolService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineRepo = moveLineRepo;
    this.reconcileService = reconcileService;
    this.taxService = taxService;
    this.taxAccountService = taxAccountService;
    this.paymentScheduleService = paymentScheduleService;
    this.paymentScheduleRepo = paymentScheduleRepo;
    this.paymentScheduleLineRepo = paymentScheduleLineRepo;
    this.accountConfigService = accountConfigService;
    this.irrecoverableCustomerLineRepo = irrecoverableCustomerLineRepo;
    this.invoiceRepo = invoiceRepo;
    this.managementObjectRepo = managementObjectRepo;
    this.irrecoverableRepo = irrecoverableRepo;

    this.appAccountService = appAccountService;
  }

  /**
   * Procedure to fill in the list of rejected invoices and payment schedules to pass into
   * irrecoverable of a company, as well to fill in the field name of the irrecoverable object
   *
   * @param irrecoverable an irrecoverable object
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void getIrrecoverable(Irrecoverable irrecoverable) throws AxelorException {

    Company company = irrecoverable.getCompany();

    this.testCompanyField(company);

    if (irrecoverable.getName() == null) {
      irrecoverable.setName(this.getSequence(company));
    }

    irrecoverable.setInvoiceSet(new HashSet<Invoice>());
    irrecoverable.getInvoiceSet().addAll(this.getInvoiceList(company));
    irrecoverable.getInvoiceSet().addAll(this.getRejectInvoiceList(company));

    irrecoverable.setPaymentScheduleLineSet(new HashSet<PaymentScheduleLine>());
    irrecoverable.getPaymentScheduleLineSet().addAll(this.getPaymentScheduleLineList(company));

    irrecoverableRepo.save(irrecoverable);
  }

  /**
   * Function to retrieve the list of partner payers associated with a list of invoices
   *
   * @param invoiceList an invoice list
   * @return the partner payer list
   */
  public List<Partner> getPayerPartnerList(Set<Invoice> invoiceList) {
    List<Partner> partnerList = new ArrayList<Partner>();

    for (Invoice invoice : invoiceList) {
      if (!partnerList.contains(invoice.getPartner())) {
        partnerList.add(invoice.getPartner());
      }
    }
    return partnerList;
  }

  /**
   * Function to retrieve the list of invoice to be passed as irrecoverable by a company
   *
   * @param company a company
   * @return the invoice list to be passed as irrecoverable
   */
  public List<Invoice> getInvoiceList(Company company) {
    return invoiceRepo
        .all()
        .filter(
            "self.irrecoverableStatusSelect = ?1 AND self.company = ?2 AND self.statusSelect = ?3 "
                + "AND self.companyInTaxTotalRemaining > 0 AND self.rejectMoveLine IS NULL",
            InvoiceRepository.IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE,
            company,
            InvoiceRepository.STATUS_VENTILATED)
        .order("dueDate")
        .fetch();
  }

  /**
   * Function to retrieve the list of rejected invoices to be passed as irrecoverable by a company
   *
   * @param company a company
   * @return the invoice list to be passed as irrecoverable
   */
  public List<Invoice> getRejectInvoiceList(Company company) {
    return invoiceRepo
        .all()
        .filter(
            "self.irrecoverableStatusSelect = ?1 AND self.company = ?2 AND self.statusSelect = ?3 "
                + "AND self.companyInTaxTotalRemaining = 0 AND self.rejectMoveLine IS NOT NULL",
            InvoiceRepository.IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE,
            company,
            InvoiceRepository.STATUS_VENTILATED)
        .order("dueDate")
        .fetch();
  }

  /**
   * Function to retrieve the list of rejected payment schedule to pass a irrecoverable by a company
   *
   * @param company a company
   * @return the payment schedule list to be passed as irrecoverable
   */
  public List<PaymentScheduleLine> getPaymentScheduleLineList(Company company) {
    return paymentScheduleLineRepo
        .all()
        .filter(
            "self.paymentSchedule.irrecoverableStatusSelect = ?1 AND self.paymentSchedule.company = ?2 "
                + "AND self.paymentSchedule.statusSelect = ?3 AND self.rejectMoveLine.amountRemaining > 0",
            PaymentScheduleRepository.IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE,
            company,
            PaymentScheduleRepository.STATUS_CONFIRMED)
        .order("scheduleDate")
        .fetch();
  }

  /**
   * Function to recover invoices to be irrecoverable from a partner
   *
   * @param partner a partner
   * @param allInvoiceList invoice list to be pass irrecoverable for the company
   * @return the invoice list to be passed as irrecoverable
   */
  public List<Invoice> getInvoiceList(Partner partner, Set<Invoice> allInvoiceList) {
    List<Invoice> invoiceList = new ArrayList<Invoice>();

    for (Invoice invoice : allInvoiceList) {
      if (invoice.getPartner().equals(partner)) {
        invoiceList.add(invoice);
      }
    }

    log.debug("Number of invoices to be irrecoverable for the partner : {}", invoiceList.size());

    return invoiceList;
  }

  /**
   * Function to recover the rejected payment schedules to pass into irrecoverable of a partner
   *
   * @param payerPartner a partner payer
   * @param allPaymentScheduleLineList La liste des échéances rejetées à passer en irrécouvrable de
   *     la société
   * @return the payment schedule list to be passed as irrecoverable
   */
  public List<PaymentScheduleLine> getPaymentScheduleLineList(
      Partner payerPartner, Set<PaymentScheduleLine> allPaymentScheduleLineList) {
    List<PaymentScheduleLine> paymentScheduleLineList = new ArrayList<PaymentScheduleLine>();

    for (PaymentScheduleLine paymentScheduleLine : allPaymentScheduleLineList) {
      if (paymentScheduleLine.getPaymentSchedule().getPartner().equals(payerPartner)) {
        paymentScheduleLineList.add(paymentScheduleLine);
      }
    }

    log.debug(
        "Number of payment schedules to be changed to irrecoverable for the partner: {}",
        paymentScheduleLineList.size());

    return paymentScheduleLineList;
  }

  /**
   * Procédure permettant de passer en irrécouvrables les factures et échéances rejetées récupéré
   * sur l'objet Irrécouvrable
   *
   * @param irrecoverable Un objet Irrécouvrable
   */
  @Transactional
  public void createIrrecoverableReport(Irrecoverable irrecoverable) {

    Set<Invoice> invoiceSet = irrecoverable.getInvoiceSet();
    Set<PaymentScheduleLine> paymentScheduleLineSet = irrecoverable.getPaymentScheduleLineSet();

    irrecoverable.setMoveSet(new HashSet<Move>());

    List<Partner> payerPartnerList = this.getPayerPartnerList(invoiceSet);

    EntityTransaction transaction = JPA.em().getTransaction();

    int i = 0;
    if (payerPartnerList != null && payerPartnerList.size() != 0) {
      for (Partner payerPartner : payerPartnerList) {

        if (!transaction.isActive()) {
          transaction.begin();
        }

        i++;
        try {
          log.debug("Partner : {}", payerPartner.getName());
          this.createIrrecoverableCustomerLine(
              irrecoverable,
              payerPartner,
              this.getInvoiceList(payerPartner, invoiceSet),
              this.getPaymentScheduleLineList(payerPartner, paymentScheduleLineSet));
          irrecoverableRepo.save(irrecoverable);
          transaction.commit();

          if (i % 50 == 0) {
            JPA.flush();
            JPA.clear();
          }

        } catch (Exception e) {
          TraceBackService.trace(e);
          log.error("Bug generated for the partner : {}", payerPartner.getName());

        } finally {
          if (!transaction.isActive()) {
            transaction.begin();
          }
        }
      }
    }
  }

  /**
   * Function to create a customer line
   *
   * @param irrecoverable an irrecoverable object
   * @param payerPartner a partner payer
   * @param invoiceList a partner payer invoice list
   * @param paymentScheduleLineList a partner payer payment schedule list
   * @return the irrecoverable customer line
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public IrrecoverableCustomerLine createIrrecoverableCustomerLine(
      Irrecoverable irrecoverable,
      Partner payerPartner,
      List<Invoice> invoiceList,
      List<PaymentScheduleLine> paymentScheduleLineList)
      throws AxelorException {
    IrrecoverableCustomerLine icl = new IrrecoverableCustomerLine();
    icl.setIrrecoverable(irrecoverable);
    irrecoverableCustomerLineRepo.save(icl);
    irrecoverable.getIrrecoverableCustomerLineList().add(icl);
    icl.setPartner(payerPartner);
    icl.setIrrecoverablePaymentScheduleLineLineList(
        this.createIrrecoverablePaymentScheduleLineLineList(icl, paymentScheduleLineList));
    icl.setIrrecoverableInvoiceLineList(this.createIrrecoverableInvoiceLineList(icl, invoiceList));

    log.debug("Customer line : {}", icl);

    return icl;
  }

  public Irrecoverable retrieveAndInit(Irrecoverable irrecoverable) {
    irrecoverable = irrecoverableRepo.find(irrecoverable.getId());
    if (irrecoverable.getMoveSet() == null) {
      irrecoverable.setMoveSet(Sets.newHashSet());
    }
    return irrecoverable;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void manageIrrecoverableInvoice(Irrecoverable irrecoverable, Invoice invoice)
      throws AxelorException {

    log.debug("Invoice : {}", invoice.getInvoiceId());
    irrecoverable = retrieveAndInit(irrecoverable);
    invoice = invoiceRepo.find(invoice.getId());
    this.createIrrecoverableInvoiceLineMove(irrecoverable, invoice);

    irrecoverableRepo.save(irrecoverable);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void manageIrrecoverablePaymentScheduleLine(
      Irrecoverable irrecoverable, PaymentScheduleLine paymentScheduleLine) throws AxelorException {

    log.debug("Payment schedule line : {}", paymentScheduleLine.getName());
    irrecoverable = retrieveAndInit(irrecoverable);
    paymentScheduleLine = paymentScheduleLineRepo.find(paymentScheduleLine.getId());
    this.createMoveForPaymentScheduleLineReject(irrecoverable, paymentScheduleLine);

    irrecoverableRepo.save(irrecoverable);
  }

  /**
   * Procédure permettant de
   *
   * @param irrecoverable
   * @throws AxelorException
   */
  public int passInIrrecoverable(Irrecoverable irrecoverable) throws AxelorException {

    int anomaly = 0;

    this.testCompanyField(irrecoverable.getCompany());

    if (irrecoverable.getInvoiceSet() != null && !irrecoverable.getInvoiceSet().isEmpty()) {
      for (Invoice invoice : irrecoverable.getInvoiceSet()) {
        try {
          this.manageIrrecoverableInvoice(irrecoverable, invoice);
        } catch (AxelorException e) {
          anomaly++;
          TraceBackService.trace(
              new AxelorException(
                  e, e.getCategory(), I18n.get("Invoice") + " %s", invoice.getInvoiceId()),
              ExceptionOriginRepository.IRRECOVERABLE,
              irrecoverable.getId());
          log.error("Bug generated for the invoice : {}", invoice.getInvoiceId());

        } catch (Exception e) {
          anomaly++;
          TraceBackService.trace(
              new Exception(String.format(I18n.get("Invoice") + " %s", invoice.getInvoiceId()), e),
              ExceptionOriginRepository.IRRECOVERABLE,
              irrecoverable.getId());
          log.error("Bug generated for the invoice : {}", invoice.getInvoiceId());
        }
      }
    }
    irrecoverable = this.retrieveAndInit(irrecoverable);
    if (irrecoverable.getPaymentScheduleLineSet() != null
        && irrecoverable.getPaymentScheduleLineSet().size() != 0) {
      for (PaymentScheduleLine paymentScheduleLine : irrecoverable.getPaymentScheduleLineSet()) {

        try {
          this.manageIrrecoverablePaymentScheduleLine(irrecoverable, paymentScheduleLine);
        } catch (AxelorException e) {
          anomaly++;
          TraceBackService.trace(
              new AxelorException(
                  e,
                  e.getCategory(),
                  I18n.get(AccountExceptionMessage.IRRECOVERABLE_1),
                  paymentScheduleLine.getName()),
              ExceptionOriginRepository.IRRECOVERABLE,
              irrecoverable.getId());
          log.error("Bug generated for the payment schedule : {}", paymentScheduleLine.getName());

        } catch (Exception e) {
          anomaly++;
          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(AccountExceptionMessage.IRRECOVERABLE_1),
                      paymentScheduleLine.getName()),
                  e),
              ExceptionOriginRepository.IRRECOVERABLE,
              irrecoverable.getId());
          log.error(
              "Bug generated for the payment schedule line : {}", paymentScheduleLine.getName());
        }
      }
    }
    irrecoverable = this.retrieveAndInit(irrecoverable);
    if (irrecoverable != null
        && irrecoverable.getMoveSet() != null
        && !irrecoverable.getMoveSet().isEmpty()) {
      EntityTransaction transaction = JPA.em().getTransaction();
      if (!transaction.isActive()) {
        transaction.begin();
      }

      irrecoverable.setStatusSelect(IrrecoverableRepository.STATUS_VALIDATED);
      irrecoverableRepo.save(irrecoverable);
      transaction.commit();
    }

    return anomaly;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createMoveForPaymentScheduleLineReject(
      Irrecoverable irrecoverable, PaymentScheduleLine paymentScheduleLine) throws AxelorException {

    Move move =
        this.createIrrecoverableMove(
            paymentScheduleLine.getRejectMoveLine(), irrecoverable.getName());
    if (move == null) {
      throw new AxelorException(
          irrecoverable,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IRRECOVERABLE_2),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }
    moveValidateService.accounting(move);
    irrecoverable.getMoveSet().add(move);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createIrrecoverableInvoiceLineMove(Irrecoverable irrecoverable, Invoice invoice)
      throws AxelorException {

    BigDecimal prorataRate = this.getProrataRate(invoice, invoice.getRejectMoveLine() != null);

    // Getting customer MoveLine from Facture
    MoveLine customerMoveLine = moveToolService.getCustomerMoveLineByQuery(invoice);

    List<Reconcile> reconcileList = new ArrayList<Reconcile>();
    // Ajout de l'écriture générée
    Move move =
        this.createIrrecoverableMove(
            invoice,
            prorataRate,
            invoice.getRejectMoveLine() != null,
            irrecoverable.getName(),
            reconcileList);
    if (move == null) {
      throw new AxelorException(
          irrecoverable,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IRRECOVERABLE_2),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }
    moveValidateService.accounting(move);

    if (!ObjectUtils.isEmpty(reconcileList)) {
      for (Reconcile reconcile : reconcileList) {
        reconcileService.confirmReconcile(reconcile, true, true);
      }
    }

    irrecoverable.getMoveSet().add(move);

    customerMoveLine.setIrrecoverableStatusSelect(
        MoveLineRepository.IRRECOVERABLE_STATUS_PASSED_IN_IRRECOUVRABLE);

    invoice.setIrrecoverableStatusSelect(
        InvoiceRepository.IRRECOVERABLE_STATUS_PASSED_IN_IRRECOUVRABLE);

    if (invoice.getCanceledPaymentSchedule() != null
        && this.isAllInvoicePassedInIrrecoverable(invoice.getCanceledPaymentSchedule())) {
      invoice
          .getCanceledPaymentSchedule()
          .setIrrecoverableStatusSelect(
              PaymentScheduleRepository.IRRECOVERABLE_STATUS_PASSED_IN_IRRECOUVRABLE);
    }
  }

  /**
   * Function to create an irrecoverable invoice line list
   *
   * @param icl a customer line
   * @param invoiceList a partner payer invoice list
   * @return the created irrecoverable invoice line list
   * @throws AxelorException
   */
  public List<IrrecoverableInvoiceLine> createIrrecoverableInvoiceLineList(
      IrrecoverableCustomerLine icl, List<Invoice> invoiceList) throws AxelorException {
    int seq = 1;
    List<IrrecoverableInvoiceLine> iilList = new ArrayList<IrrecoverableInvoiceLine>();
    for (Invoice invoice : invoiceList) {
      iilList.add(this.createIrrecoverableInvoiceLine(icl, invoice, seq));
      seq++;
    }
    return iilList;
  }

  /**
   * Function to create a rejected payment schedule line list
   *
   * @param icl a customer line
   * @param invoiceList a partner payer rejected payment schedule line list
   * @return the created rejected payment schedule line list
   * @throws AxelorException
   */
  public List<IrrecoverablePaymentScheduleLineLine> createIrrecoverablePaymentScheduleLineLineList(
      IrrecoverableCustomerLine icl, List<PaymentScheduleLine> paymentScheduleLineList)
      throws AxelorException {
    int seq = 1;
    List<IrrecoverablePaymentScheduleLineLine> ipsllList =
        new ArrayList<IrrecoverablePaymentScheduleLineLine>();
    for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
      ipsllList.add(this.createIrrecoverablePaymentScheduleLineLine(icl, paymentScheduleLine, seq));
      seq++;
    }
    return ipsllList;
  }

  /**
   * Function to create a irrecoverable invoice line
   *
   * @param icl a customer line
   * @param invoice an invoice
   * @param seq a sequence number
   * @return the created irrecoverable invoice line
   * @throws AxelorException
   */
  public IrrecoverableInvoiceLine createIrrecoverableInvoiceLine(
      IrrecoverableCustomerLine icl, Invoice invoice, int seq) throws AxelorException {
    IrrecoverableInvoiceLine iil = new IrrecoverableInvoiceLine();
    iil.setInvoice(invoice);
    iil.setInvoiceLineSeq(seq);
    iil.setIrrecoverableCustomerLine(icl);

    BigDecimal prorataRate = this.getProrataRate(invoice, invoice.getRejectMoveLine() != null);

    iil.setIrrecoverableReportLineList(
        this.createIrrecoverableReportLineList(iil, invoice, prorataRate));

    log.debug("Invoice line : {}", iil);

    return iil;
  }

  /**
   * Function to create a rejected payment schedule line
   *
   * @param icl a customer line
   * @param paymentScheduleLine a rejected payment schedule
   * @param seq a sequence number
   * @return the created rejected payment schedule line
   * @throws AxelorException
   */
  public IrrecoverablePaymentScheduleLineLine createIrrecoverablePaymentScheduleLineLine(
      IrrecoverableCustomerLine icl, PaymentScheduleLine paymentScheduleLine, int seq)
      throws AxelorException {
    IrrecoverablePaymentScheduleLineLine ipsll = new IrrecoverablePaymentScheduleLineLine();
    ipsll.setPaymentScheduleLine(paymentScheduleLine);
    ipsll.setIrrecoverableCustomerLine(icl);

    Company company = paymentScheduleLine.getPaymentSchedule().getCompany();

    Tax tax =
        accountConfigService.getIrrecoverableStandardRateTax(
            accountConfigService.getAccountConfig(company));

    ipsll.setIrrecoverableReportLineList(
        this.createIrrecoverableReportLineList(ipsll, paymentScheduleLine, tax));

    log.debug("Irrecoverable payment schedule line : {}", ipsll);

    return ipsll;
  }

  /**
   * Function to determine whether all invoices to be irrecoverable from a schedule to be
   * irrecoverable have been passed into irrecoverable
   *
   * @param paymentSchedule a payment schedule
   * @return is is all invoice passed in irrecoverable
   */
  public boolean isAllInvoicePassedInIrrecoverable(PaymentSchedule paymentSchedule) {
    for (Invoice invoiceScheduled : paymentSchedule.getInvoiceSet()) {
      if (invoiceScheduled
          .getIrrecoverableStatusSelect()
          .equals(InvoiceRepository.IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Function to create a list of reporting lines for an invoice line
   *
   * @param iil an invoice line
   * @param invoice an invoice
   * @param prorataRate a remaining invoice rate
   * @return the created reporting line list
   */
  public List<IrrecoverableReportLine> createIrrecoverableReportLineList(
      IrrecoverableInvoiceLine iil, Invoice invoice, BigDecimal prorataRate) {
    int seq = 1;
    List<IrrecoverableReportLine> irlList = new ArrayList<IrrecoverableReportLine>();

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {

      irlList.add(
          this.createIrrecoverableReportLine(
              iil,
              invoiceLine.getName(),
              invoiceLine
                  .getExTaxTotal()
                  .multiply(prorataRate)
                  .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP),
              seq));
      seq++;
    }
    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
      irlList.add(
          this.createIrrecoverableReportLine(
              iil,
              invoiceLineTax.getTaxLine().getTax().getName(),
              invoiceLineTax
                  .getTaxTotal()
                  .multiply(prorataRate)
                  .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP),
              seq));
      seq++;
    }
    // Afin de ne pas modifier les valeurs des lignes de factures, on les recharges depuis la base
    invoiceRepo.refresh(invoice);
    return irlList;
  }

  /**
   * Function to create a list of reporting lines for a rejected payment schedule line
   *
   * @param iil a rejected payment schedule line
   * @param paymentScheduleLine a rejected payment schedule
   * @param prorataRate a remaining payment schedule rate
   * @return the created irrecoverable reporting line
   * @throws AxelorException
   */
  public List<IrrecoverableReportLine> createIrrecoverableReportLineList(
      IrrecoverablePaymentScheduleLineLine ipsll, PaymentScheduleLine paymentScheduleLine, Tax tax)
      throws AxelorException {
    List<IrrecoverableReportLine> irlList = new ArrayList<IrrecoverableReportLine>();

    BigDecimal taxRate =
        taxService.getTaxRate(
            tax,
            appAccountService.getTodayDate(
                paymentScheduleLine.getPaymentSchedule() != null
                    ? paymentScheduleLine.getPaymentSchedule().getCompany()
                    : Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null)));

    BigDecimal amount = paymentScheduleLine.getInTaxAmount();

    BigDecimal divid = taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE);

    // Montant hors-Taxe
    BigDecimal irrecoverableAmount =
        amount
            .divide(divid, 6, RoundingMode.HALF_UP)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    // Montant Tax
    BigDecimal taxAmount = amount.subtract(irrecoverableAmount);

    irlList.add(this.createIrrecoverableReportLine(ipsll, "HT", irrecoverableAmount, 1));

    irlList.add(this.createIrrecoverableReportLine(ipsll, tax.getName(), taxAmount, 2));

    return irlList;
  }

  /**
   * Function to create a reporting line
   *
   * @param iil an invoice line
   * @param label a label
   * @param value a value
   * @param seq a sequence number
   * @return the created reporting line
   */
  public IrrecoverableReportLine createIrrecoverableReportLine(
      IrrecoverableInvoiceLine iil, String label, BigDecimal value, int seq) {
    IrrecoverableReportLine irl = new IrrecoverableReportLine();
    irl.setReportLineSeq(seq);
    irl.setLabel(label);
    irl.setValue(value);
    irl.setIrrecoverableInvoiceLine(iil);

    log.debug("Irrecoverable report line : {}", irl);

    return irl;
  }

  /**
   * Function to create a reporting line
   *
   * @param iil a rejected payment schedule line
   * @param label a label
   * @param value a value
   * @param seq a sequence number
   * @return the created irrecoverable reporting line
   */
  public IrrecoverableReportLine createIrrecoverableReportLine(
      IrrecoverablePaymentScheduleLineLine ipsll, String label, BigDecimal value, int seq) {
    IrrecoverableReportLine irl = new IrrecoverableReportLine();
    irl.setReportLineSeq(seq);
    irl.setLabel(label);
    irl.setValue(value);
    irl.setIrrecoverablePaymentScheduleLineLine(ipsll);

    log.debug("Irrecoverable report line : {}", irl);

    return irl;
  }

  /**
   * Function to calculate the remaining invoice rate
   *
   * @param invoice an invoice
   * @param isInvoiceReject is invoice rejected?
   * @return the remaining invoice rate
   */
  public BigDecimal getProrataRate(Invoice invoice, boolean isInvoiceReject) {
    BigDecimal prorataRate = null;
    if (isInvoiceReject) {
      prorataRate =
          (invoice.getRejectMoveLine().getAmountRemaining())
              .divide(invoice.getCompanyInTaxTotal(), 6, RoundingMode.HALF_UP);
    } else {
      prorataRate =
          invoice
              .getCompanyInTaxTotalRemaining()
              .divide(invoice.getCompanyInTaxTotal(), 6, RoundingMode.HALF_UP);
    }

    log.debug("Prorata rate for the invoice {} : {}", invoice.getInvoiceId(), prorataRate);

    return prorataRate;
  }

  /**
   * Function to create the pass move to irrecoverable of an invoice
   *
   * @param invoice an invoice
   * @param prorataRate the remaining invoice rate
   * @param isInvoiceReject is the invoice rejected?
   * @return the pass move
   * @throws AxelorException
   */
  public Move createIrrecoverableMove(
      Invoice invoice,
      BigDecimal prorataRate,
      boolean isInvoiceReject,
      String irrecoverableName,
      List<Reconcile> reconcileList)
      throws AxelorException {
    Company company = invoice.getCompany();
    Partner payerPartner = invoice.getPartner();

    AccountConfig accountConfig = company.getAccountConfig();

    Move invoiceMove = invoice.getMove();
    // Move
    Move move =
        moveCreateService.createMove(
            accountConfig.getIrrecoverableJournal(),
            company,
            null,
            payerPartner,
            null,
            invoice.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_IRRECOVERABLE,
            invoice.getMove().getOrigin() + ":" + irrecoverableName,
            invoice.getInvoiceId(),
            invoice.getCompanyBankDetails());
    move.setOriginDate(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : null);
    int seq = 1;

    BigDecimal amount = BigDecimal.ZERO;
    MoveLine debitMoveLine = null;
    MoveLine lastDebitMoveLine = null;
    BigDecimal creditAmount = null;

    BigDecimal debitAmount = BigDecimal.ZERO;
    String originStr = null;
    if (invoice.getMove().getOrigin() != null && irrecoverableName != null) {
      originStr = invoice.getMove().getOrigin() + ":" + irrecoverableName;
    } else if (invoice.getMove().getOrigin() == null && irrecoverableName != null) {
      originStr = irrecoverableName;
    } else if (invoice.getMove().getOrigin() != null && irrecoverableName == null) {
      originStr = invoice.getMove().getOrigin();
    }

    if (isInvoiceReject) {
      creditAmount = invoice.getRejectMoveLine().getAmountRemaining();
    } else {
      creditAmount = invoice.getCompanyInTaxTotalRemaining();
    }

    // Debits MoveLines Tva
    for (MoveLine invoiceMoveLine : invoiceMove.getMoveLineList()) {

      if (invoiceMoveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
        amount = invoiceMoveLine.getAmountRemaining();

        // Credit MoveLine Customer account (411, 416, ...)
        MoveLine creditMoveLine =
            moveLineCreateService.createMoveLine(
                move,
                payerPartner,
                invoiceMoveLine.getAccount(),
                amount,
                false,
                appAccountService.getTodayDate(company),
                seq,
                originStr,
                invoice.getInvoiceId());
        move.getMoveLineList().add(creditMoveLine);

        Reconcile reconcile =
            reconcileService.createReconcile(invoiceMoveLine, creditMoveLine, amount, false);
        if (reconcile != null) {
          reconcileList.add(reconcile);
        }
      } else {
        amount =
            invoiceMoveLine.getCredit().multiply(prorataRate).setScale(2, RoundingMode.HALF_UP);
        if (AccountTypeRepository.TYPE_TAX.equals(
            invoiceMoveLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
          if (invoiceMoveLine.getVatSystemSelect() == null
              || invoiceMoveLine.getVatSystemSelect() == 0) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_INVOICE_TAX);
          }
          debitMoveLine =
              moveLineCreateService.createMoveLine(
                  move,
                  payerPartner,
                  invoiceMoveLine.getAccount(),
                  amount,
                  true,
                  invoiceMoveLine.getTaxLine(),
                  appAccountService.getTodayDate(company),
                  seq,
                  originStr,
                  invoice.getInvoiceId());

          debitMoveLine.setVatSystemSelect(invoiceMoveLine.getVatSystemSelect());

        } else {
          // Debit MoveLine 654 (irrecoverable account)
          debitMoveLine =
              moveLineCreateService.createMoveLine(
                  move,
                  payerPartner,
                  accountConfig.getIrrecoverableAccount(),
                  amount,
                  true,
                  invoiceMoveLine.getTaxLine(),
                  appAccountService.getTodayDate(company),
                  seq,
                  originStr,
                  invoice.getInvoiceId());
        }
        move.getMoveLineList().add(debitMoveLine);
        seq++;

        debitAmount = debitAmount.add(amount);
        lastDebitMoveLine = debitMoveLine;
      }
    }

    if (debitAmount != null
        && debitAmount.compareTo(creditAmount) != 0
        && lastDebitMoveLine != null) {
      lastDebitMoveLine.setDebit(
          lastDebitMoveLine.getDebit().add(creditAmount.subtract(debitAmount)));
    }

    return move;
  }

  /**
   * Function to create the pass move to irrecoverable of a payment schedule
   *
   * @param moveLine a payment schedule move line
   * @return the irrecoverable move
   * @throws AxelorException
   */
  public Move createIrrecoverableMove(MoveLine moveLine, String irrecoverableName)
      throws AxelorException {

    Company company = moveLine.getMove().getCompany();
    Partner payerPartner = moveLine.getPartner();
    BigDecimal amount = moveLine.getAmountRemaining();

    AccountConfig accountConfig = company.getAccountConfig();

    String originStr = null;
    if (moveLine.getMove().getOrigin() != null && irrecoverableName != null) {
      originStr = moveLine.getMove().getOrigin() + ":" + irrecoverableName;
    } else if (moveLine.getMove().getOrigin() == null && irrecoverableName != null) {
      originStr = irrecoverableName;
    } else if (moveLine.getMove().getOrigin() != null && irrecoverableName == null) {
      originStr = moveLine.getMove().getOrigin();
    }

    // Move
    Move move =
        moveCreateService.createMove(
            accountConfig.getIrrecoverableJournal(),
            company,
            null,
            payerPartner,
            null,
            payerPartner != null ? payerPartner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_IRRECOVERABLE,
            originStr,
            moveLine.getDescription(),
            moveLine.getMove().getCompanyBankDetails());
    move.setOriginDate(moveLine.getMove().getDate());

    int seq = 1;

    // Credit MoveLine Customer account (411, 416, ...)
    MoveLine creditMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            payerPartner,
            moveLine.getAccount(),
            amount,
            false,
            appAccountService.getTodayDate(company),
            seq,
            originStr,
            moveLine.getDescription());
    move.getMoveLineList().add(creditMoveLine);

    Reconcile reconcile = reconcileService.createReconcile(moveLine, creditMoveLine, amount, false);
    if (reconcile != null) {
      reconcileService.confirmReconcile(reconcile, true, true);
    }

    Tax tax = accountConfig.getIrrecoverableStandardRateTax();

    BigDecimal taxRate = taxService.getTaxRate(tax, appAccountService.getTodayDate(company));

    // Debit MoveLine 654. (irrecoverable account)
    BigDecimal divid = taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE);
    BigDecimal irrecoverableAmount =
        amount
            .divide(divid, 6, RoundingMode.HALF_UP)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    MoveLine creditMoveLine1 =
        moveLineCreateService.createMoveLine(
            move,
            payerPartner,
            accountConfig.getIrrecoverableAccount(),
            irrecoverableAmount,
            true,
            appAccountService.getTodayDate(company),
            2,
            originStr,
            moveLine.getDescription());
    move.getMoveLineList().add(creditMoveLine1);

    if (moveLine.getAccount().getVatSystemSelect() == null
        || moveLine.getAccount().getVatSystemSelect() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_ACCOUNT),
          moveLine.getAccount().getCode());
    }

    // Debit MoveLine 445 (Tax account)
    Account taxAccount =
        taxAccountService.getAccount(
            tax,
            company,
            move.getJournal(),
            moveLine.getAccount().getVatSystemSelect(),
            false,
            move.getFunctionalOriginSelect());
    BigDecimal taxAmount = amount.subtract(irrecoverableAmount);
    MoveLine creditMoveLine2 =
        moveLineCreateService.createMoveLine(
            move,
            payerPartner,
            taxAccount,
            taxAmount,
            true,
            appAccountService.getTodayDate(company),
            3,
            moveLine.getMove().getOrigin() + ":" + irrecoverableName,
            moveLine.getDescription());
    move.getMoveLineList().add(creditMoveLine2);

    return move;
  }

  /**
   * Function to create a management object
   *
   * @param code
   * @param message
   * @return the management object
   */
  public ManagementObject createManagementObject(String code, String message) {
    ManagementObject managementObject =
        managementObjectRepo
            .all()
            .filter("self.code = ?1 AND self.name = ?2", code, message)
            .fetchOne();
    if (managementObject != null) {
      return managementObject;
    }

    managementObject = new ManagementObject();
    managementObject.setCode(code);
    managementObject.setName(message);
    return managementObject;
  }

  /**
   * Procédure permettant de vérifier les champs d'une société
   *
   * @param company Une société
   * @throws AxelorException
   */
  public void testCompanyField(Company company) throws AxelorException {

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    accountConfigService.getIrrecoverableAccount(accountConfig);
    accountConfigService.getIrrecoverableJournal(accountConfig);
    accountConfigService.getIrrecoverableStandardRateTax(accountConfig);
  }

  public String getSequence(Company company) throws AxelorException {

    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.IRRECOVERABLE, company, Irrecoverable.class, "name");
    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.IRRECOVERABLE_4),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return seq;
  }

  /**
   * Procédure permettant de passer une facture en irrécouvrable
   *
   * @param invoice Une facture
   * @param generateEvent Un évènement à t'il déjà été créé par un autre objet ?
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void passInIrrecoverable(Invoice invoice, boolean generateEvent) throws AxelorException {
    invoice.setIrrecoverableStatusSelect(
        InvoiceRepository.IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE);

    MoveLine moveLine = moveToolService.getCustomerMoveLineByQuery(invoice);

    if (generateEvent) {
      Company company = invoice.getCompany();

      ManagementObject managementObject =
          this.createManagementObject(
              "IRR",
              accountConfigService.getIrrecoverableReasonPassage(
                  accountConfigService.getAccountConfig(company)));
      invoice.setManagementObject(managementObject);

      if (moveLine == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.IRRECOVERABLE_3),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            invoice.getInvoiceId());
      }

      this.passInIrrecoverable(moveLine, managementObject, false);
    } else if (moveLine != null) {
      this.passInIrrecoverable(moveLine, false, false);
    }

    invoiceRepo.save(invoice);
  }

  /**
   * Procédure permettant de passer une facture en irrécouvrable
   *
   * @param invoice Une facture
   * @param managementObject Un objet de gestion (utilisé si procédure appelée depuis un autre
   *     objet)
   * @throws AxelorException
   */
  public void passInIrrecoverable(Invoice invoice, ManagementObject managementObject)
      throws AxelorException {
    this.passInIrrecoverable(invoice, false);
    invoice.setManagementObject(managementObject);
    MoveLine moveLine = moveToolService.getCustomerMoveLineByQuery(invoice);

    if (moveLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IRRECOVERABLE_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          invoice.getInvoiceId());
    }

    this.passInIrrecoverable(moveLine, managementObject, false);

    invoiceRepo.save(invoice);
  }

  /**
   * Procédure permettant d'annuler le passage en irrécouvrable d'une facture
   *
   * @param invoice Une facture
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void notPassInIrrecoverable(Invoice invoice) throws AxelorException {
    invoice.setIrrecoverableStatusSelect(InvoiceRepository.IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);

    MoveLine moveLine = moveToolService.getCustomerMoveLineByQuery(invoice);

    if (moveLine != null) {
      this.notPassInIrrecoverable(moveLine, false);
    }

    invoiceRepo.save(invoice);
  }

  /**
   * Procédure permettant de passer en irrécouvrable une ligne d'écriture
   *
   * @param moveLine Une ligne d'écriture
   * @param generateEvent Un évènement à t'il déjà été créé par un autre objet ?
   * @param passInvoice La procédure doit-elle passer aussi en irrécouvrable la facture ?
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void passInIrrecoverable(MoveLine moveLine, boolean generateEvent, boolean passInvoice)
      throws AxelorException {
    moveLine.setIrrecoverableStatusSelect(
        MoveLineRepository.IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE);
    ManagementObject managementObject = null;
    if (generateEvent) {
      Company company = moveLine.getMove().getCompany();

      managementObject =
          this.createManagementObject(
              "IRR",
              accountConfigService.getIrrecoverableReasonPassage(
                  accountConfigService.getAccountConfig(company)));
      moveLine.setManagementObject(managementObject);
    }

    if (moveLine.getMove().getInvoice() != null && passInvoice) {
      this.passInIrrecoverable(moveLine.getMove().getInvoice(), managementObject);
    }

    moveLineRepo.save(moveLine);
  }

  /**
   * Procédure permettant d'annuler le passage en irrrécouvrable d'une ligne d'écriture
   *
   * @param moveLine Une ligne d'écriture
   * @param managementObject Un objet de gestion (utilisé si procédure appelée depuis un autre
   *     objet)
   * @param passInvoice La procédure doit-elle passer aussi en irrécouvrable la facture ?
   * @throws AxelorException
   */
  public void passInIrrecoverable(
      MoveLine moveLine, ManagementObject managementObject, boolean passInvoice)
      throws AxelorException {
    this.passInIrrecoverable(moveLine, false, passInvoice);

    moveLine.setManagementObject(managementObject);

    moveLineRepo.save(moveLine);
  }

  /**
   * Procédure permettant d'annuler le passage en irrrécouvrable d'une ligne d'écriture
   *
   * @param moveLine Une ligne d'écriture
   * @param passInvoice La procédure doit-elle passer aussi en irrécouvrable la facture ?
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void notPassInIrrecoverable(MoveLine moveLine, boolean passInvoice)
      throws AxelorException {
    moveLine.setIrrecoverableStatusSelect(
        MoveLineRepository.IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);

    if (moveLine.getMove().getInvoice() != null && passInvoice) {
      this.notPassInIrrecoverable(moveLine.getMove().getInvoice());
    }

    moveLineRepo.save(moveLine);
  }

  /**
   * Procédure permettant de passer un échéancier de lissage de paiement en irrécouvrable La
   * procédure passera aussi les lignes d'écriture de rejet d'échéance en irrécouvrable, ainsi que
   * les factures pas complètement payée selectionnées sur l'échéancier
   *
   * @param paymentSchedule Un échéancier de paiement
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void passInIrrecoverable(PaymentSchedule paymentSchedule) throws AxelorException {
    Company company = paymentSchedule.getCompany();

    paymentSchedule.setIrrecoverableStatusSelect(
        PaymentScheduleRepository.IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE);

    ManagementObject managementObject =
        this.createManagementObject(
            "IRR",
            accountConfigService.getIrrecoverableReasonPassage(
                accountConfigService.getAccountConfig(company)));
    paymentSchedule.setManagementObject(managementObject);

    List<MoveLine> paymentScheduleLineRejectMoveLineList = new ArrayList<MoveLine>();

    for (PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList()) {
      if (paymentScheduleLine.getRejectMoveLine() != null
          && paymentScheduleLine.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO)
              > 0) {
        paymentScheduleLineRejectMoveLineList.add(paymentScheduleLine.getRejectMoveLine());
      }
    }

    for (MoveLine moveLine : paymentScheduleLineRejectMoveLineList) {
      this.passInIrrecoverable(moveLine, managementObject, true);
    }

    for (Invoice invoice : paymentSchedule.getInvoiceSet()) {
      if (invoice.getCompanyInTaxTotalRemaining().compareTo(BigDecimal.ZERO) > 0) {
        this.passInIrrecoverable(invoice, managementObject);
      }
    }

    paymentScheduleService.cancelPaymentSchedule(paymentSchedule);

    paymentScheduleRepo.save(paymentSchedule);
  }

  /**
   * Procédure permettant d'annuler le passage en irrécouvrable d'une échéancier de lissage de
   * paiement La procédure annulera aussi le passage en irrécouvrable des lignes d'écriture de rejet
   * d'échéance en irrécouvrable, ainsi que des factures pas complètement payée selectionnées sur
   * l'échéancier
   *
   * @param paymentSchedule Un échéancier de paiement
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void notPassInIrrecoverable(PaymentSchedule paymentSchedule) throws AxelorException {
    paymentSchedule.setIrrecoverableStatusSelect(
        PaymentScheduleRepository.IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);

    List<MoveLine> paymentScheduleLineRejectMoveLineList = new ArrayList<MoveLine>();

    for (PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList()) {
      if (paymentScheduleLine.getRejectMoveLine() != null
          && paymentScheduleLine.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO)
              > 0) {
        paymentScheduleLineRejectMoveLineList.add(paymentScheduleLine.getRejectMoveLine());
      }
    }

    for (MoveLine moveLine : paymentScheduleLineRejectMoveLineList) {
      this.notPassInIrrecoverable(moveLine, false);
    }

    for (Invoice invoice : paymentSchedule.getInvoiceSet()) {
      this.notPassInIrrecoverable(invoice);
    }
    paymentScheduleRepo.save(paymentSchedule);
  }
}
