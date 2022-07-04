package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.google.inject.Inject;

public class InvoiceDomainServiceImpl implements InvoiceDomainService {

  protected InvoiceService invoiceService;

  @Inject
  public InvoiceDomainServiceImpl(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @Override
  public String getPartnerBaseDomain(Company company, Invoice invoice, int invoiceTypeSelect) {
    long companyId = company.getPartner() == null ? 0 : company.getPartner().getId();
    String domain =
        String.format(
            "self.id != %d "
                + "AND self.isContact = false "
                + "AND :company member of self.companySet",
            companyId);

    if (invoiceTypeSelect == PriceListRepository.TYPE_SALE) {
      domain += " AND self.isCustomer = true ";
    } else {
      domain += " AND self.isSupplier = true ";
    }
    return domain;
  }
}
