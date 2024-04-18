package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconcileToolServiceImpl implements ReconcileToolService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountCustomerService accountCustomerService;
  protected MoveToolService moveToolService;
  protected InvoiceTermService invoiceTermService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoiceTermPaymentRepository invoiceTermPaymentRepository;

  @Inject
  public ReconcileToolServiceImpl(
      AccountCustomerService accountCustomerService,
      MoveToolService moveToolService,
      InvoiceTermService invoiceTermService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoiceTermPaymentRepository invoiceTermPaymentRepository) {
    this.accountCustomerService = accountCustomerService;
    this.moveToolService = moveToolService;
    this.invoiceTermService = invoiceTermService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoiceTermPaymentRepository = invoiceTermPaymentRepository;
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

  @Override
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

  @Override
  public void updateInvoiceTermsAmountRemaining(Reconcile reconcile) throws AxelorException {

    log.debug("updateInvoiceTermsAmountRemaining : reconcile : {}", reconcile);

    List<InvoicePayment> invoicePaymentList =
        invoicePaymentRepository.findByReconcile(reconcile).fetch();

    if (!invoicePaymentList.isEmpty()) {
      for (InvoicePayment invoicePayment : invoicePaymentList) {
        invoiceTermService.updateInvoiceTermsAmountRemaining(invoicePayment);
      }
    }

    List<InvoiceTermPayment> invoiceTermPaymentList =
        invoiceTermPaymentRepository.findByReconcileId(reconcile.getId()).fetch();

    if (CollectionUtils.isNotEmpty(invoiceTermPaymentList)) {
      invoiceTermService.updateInvoiceTermsAmountRemaining(invoiceTermPaymentList);
    }
  }
}
