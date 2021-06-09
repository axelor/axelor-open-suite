package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
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
  protected static final int FETCH_LIMIT = 10;

  private AccountConfigService accountConfigService;
  private InvoiceRepository invoiceRepository;
  private AppBaseService appBaseService;

  private String result = "";

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
      bockCustomersWithLatePayments();
      batch.setComments(result);
    } catch (Exception e) {
      TraceBackService.trace(e, ExceptionOriginRepository.IMPORT, batch.getId());
      incrementAnomaly();
    }
  }

  private void bockCustomersWithLatePayments() throws AxelorException {
    List<Long> idsDone = new ArrayList<Long>();
    List<Invoice> invoices;
    List<Long> customersToBlock = new ArrayList<Long>();
    idsDone.add(0L);
    Query<Invoice> query =
        invoiceRepository
            .all()
            .filter(
                "self.operationTypeSelect = 3"
                    + "AND self.amountRemaining > 0"
                    + "AND self.id NOT IN :ids");
    while (!(invoices = query.bind("ids", idsDone).fetch(FETCH_LIMIT)).isEmpty()) {
      for (Invoice invoice : invoices) {
        List<Partner> partners = processInvoice(invoice);
        if (CollectionUtils.isNotEmpty(partners)) {
          for (Partner partner : partners) {
            if (!customersToBlock.contains(partner.getId())) {
              log.debug("Blocking " + partner.getFullName());
              result += I18n.get("Blocking") + " " + partner.getFullName() + "</br>";
              customersToBlock.add(partner.getId());
            }
          }
        }
        idsDone.add(invoice.getId());
      }
    }
    blockCustomers(customersToBlock);
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

  private List<Partner> processInvoice(Invoice invoice) throws AxelorException {
    List<Partner> customersToBlock = new ArrayList<Partner>();
    AccountConfig config = accountConfigService.getAccountConfig(invoice.getCompany());
    if (!config.getHasLatePaymentAccountBlocking()) {
      return customersToBlock;
    }
    if (invoice
            .getDueDate()
            .compareTo(
                appBaseService
                    .getTodayDate(invoice.getCompany())
                    .plusDays(config.getNumberOfDaysBeforeAccountBLocking()))
        < 0) {
      customersToBlock.add(invoice.getPartner());
    }
    incrementDone();
    return customersToBlock;
  }
}
