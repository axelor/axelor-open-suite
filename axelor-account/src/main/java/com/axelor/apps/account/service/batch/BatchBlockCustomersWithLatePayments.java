package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBlockCustomersWithLatePayments extends BatchStrategy {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  private AccountConfigService accountConfigService;
  private InvoiceRepository invoiceRepository;
  private AppBaseService appBaseService;

  @Inject
  public BatchBlockCustomersWithLatePayments(
      AccountConfigService accountConfigService,
      InvoiceRepository invoiceRepository,
      AppBaseService appBaseService) {
    this.accountConfigService = accountConfigService;
    this.invoiceRepository = invoiceRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  protected void process() {
    try {
      String result = blockCustomersWithLatePayments();
      addComment(result);
    } catch (Exception e) {
      TraceBackService.trace(e, ExceptionOriginRepository.IMPORT, batch.getId());
      incrementAnomaly();
    }
  }

  protected String blockCustomersWithLatePayments() throws AxelorException {
    String result = "";
    List<Invoice> invoices;
    List<Long> customersToBlock = new ArrayList<Long>();
    int offset = 0;
    Query<Invoice> query =
        invoiceRepository
            .all()
            .filter(
                "self.operationTypeSelect = :operationTypeSelect "
                    + "AND self.amountRemaining > 0");
    while (!(invoices =
            query
                .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
                .fetch(FETCH_LIMIT, offset))
        .isEmpty()) {
      for (Invoice invoice : invoices) {
        ++offset;
        Partner partner = processInvoice(invoice);
        if (partner != null && !customersToBlock.contains(partner.getId())) {
          log.debug("Blocking {}", partner.getFullName());
          result += I18n.get("Blocking") + " " + partner.getFullName() + "</br>";
          customersToBlock.add(partner.getId());
        }
      }
      JPA.clear();
    }
    blockCustomers(customersToBlock);
    return result;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void blockCustomers(List<Long> customersToBlock) {
    if (CollectionUtils.isNotEmpty(customersToBlock)) {
      Query.of(Partner.class)
          .filter("self.id in :ids")
          .bind("ids", customersToBlock)
          .update("hasBlockedAccount", true);
    }
  }

  protected Partner processInvoice(Invoice invoice) {
    try {
      AccountConfig config = accountConfigService.getAccountConfig(invoice.getCompany());
      if (!config.getHasLatePaymentAccountBlocking()) {
        return null;
      }
      if (invoice
              .getDueDate()
              .compareTo(
                  appBaseService
                      .getTodayDate(invoice.getCompany())
                      .plusDays(config.getNumberOfDaysBeforeAccountBLocking()))
          < 0) {
        incrementDone();
        return invoice.getPartner();
      }
    } catch (Exception e) {
      TraceBackService.trace(
          new Exception(String.format(("Invoice") + " %s", invoice.getInvoiceId()), e),
          null,
          batch.getId());
      log.error("Error for invoice {}", invoice.getInvoiceId());
      incrementAnomaly();
    }
    return null;
  }
}
