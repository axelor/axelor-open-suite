package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchBlockCustomersWithLatePayments extends BatchStrategy {
  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected  static final int FETCH_LIMIT = 10;
  
  private AccountConfigService accountConfigService;
  private DebtRecoveryRepository debtRecoveryRepository;
  private AppBaseService appBaseService;

  @Inject
  public BatchBlockCustomersWithLatePayments(AccountConfigService accountConfigService,
      DebtRecoveryRepository debtRecoveryRepository,
      AppBaseService appBaseService) {
    this.accountConfigService = accountConfigService;
    this.debtRecoveryRepository = debtRecoveryRepository;
    this.appBaseService = appBaseService;
  }
  
  @Override
  protected void process() {
    try {
      bockCustomersWithLatePayments();
    }catch(Exception e) {
      TraceBackService.trace(e, ExceptionOriginRepository.IMPORT, batch.getId());
      incrementAnomaly();
    }
  }

  private void bockCustomersWithLatePayments() throws AxelorException {
    List<Long> idsDone = new ArrayList<Long>();
    List<DebtRecovery> debtRecoveries;
    List<Long> customersToBlock = new ArrayList<Long>();
    idsDone.add(0L);
    Query<DebtRecovery> query = 
        debtRecoveryRepository.all()
        .filter("self.waitDebtRecoveryMethodLine = true"
            + "AND self.company."
            + "AND self.id NOT IN :ids");
    while(!(debtRecoveries = query.bind("ids",idsDone).fetch(FETCH_LIMIT)).isEmpty()) {
      for (DebtRecovery debtRecovery : debtRecoveries) {
        List<Partner> partners = processDebtRecovery(debtRecovery);
        if(CollectionUtils.isNotEmpty(partners)) {
          for (Partner partner : partners) {
            if(!customersToBlock.contains(partner.getId())) {
              customersToBlock.add(partner.getId());
            }
          }
        }
        idsDone.add(debtRecovery.getId());
      }
    }
    blockCustomers(customersToBlock);
    
  }

  @Transactional(rollbackOn = Exception.class)
  private void blockCustomers(List<Long> customersToBlock) {
    Query.of(Partner.class).filter("self.id in :ids").bind("ids", customersToBlock).update("hasBlockedAccount", true);
  }

  private List<Partner> processDebtRecovery(DebtRecovery debtRecovery) throws AxelorException {
    List<Partner> customersToBlock = new ArrayList<Partner>();
    AccountConfig config = accountConfigService.getAccountConfig(debtRecovery.getCompany());
    if(!config.getHasLatePaymentAccountBlocking()) {
      return customersToBlock;
    }
    if(CollectionUtils.isNotEmpty(debtRecovery.getInvoiceDebtRecoverySet())) {
      for (Invoice invoice : debtRecovery.getInvoiceDebtRecoverySet()) {
        if(invoice.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
          if(invoice.getDueDate().compareTo(appBaseService.getTodayDate(debtRecovery.getCompany()).plusDays(config.getNumberOfDaysBeforeAccountBLocking())) < 0) {
            customersToBlock.add(invoice.getPartner());
          }
        }
      }
    }
    incrementDone();
    return customersToBlock;
  }
}
