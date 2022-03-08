package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.service.PartnerSaleServiceImpl;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class PartnerSupplychainServiceImpl extends PartnerSaleServiceImpl
    implements PartnerSupplychainService {

  private InvoiceRepository invoiceRepository;
  private AccountConfigService accountConfigService;

  @Inject
  public PartnerSupplychainServiceImpl(
      PartnerRepository partnerRepo,
      AppBaseService appBaseService,
      InvoiceRepository invoiceRepository,
      AccountConfigService accountConfigService) {
    super(partnerRepo, appBaseService);
    this.invoiceRepository = invoiceRepository;
    this.accountConfigService = accountConfigService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateBlockedAccount(Partner partner) throws AxelorException {
    List<Invoice> partnerInvoice;
    int FETCH_LIMIT = 10;
    int offset = 0;
    Query<Invoice> query =
        invoiceRepository
            .all()
            .filter(
                "self.operationTypeSelect = :operationTypeSelect "
                    + "AND self.amountRemaining > 0 "
                    + "AND self.partner = :partner")
            .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
            .bind("partner", partner.getId());
    while (!(partnerInvoice = query.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      for (Invoice invoice : partnerInvoice) {
        AccountConfig config = accountConfigService.getAccountConfig(invoice.getCompany());
        if (invoice
                .getDueDate()
                .compareTo(
                    appBaseService
                        .getTodayDate(invoice.getCompany())
                        .plusDays(config.getNumberOfDaysBeforeAccountBlocking()))
            < 0) {
          partner.setHasBlockedAccount(true);
          partnerRepo.save(partner);
          return;
        }
      }
      JPA.clear();
    }
    partner.setHasBlockedAccount(false);
    partnerRepo.save(partner);
    return;
  }

  @Override
  public boolean isBlockedPartnerOrParent(Partner partner) {
    if (partner.getHasBlockedAccount()) {
      return true;
    }
    if (partner.getParentPartner() != null) {
      return isBlockedPartnerOrParent(partner);
    }
    return false;
  }
}
