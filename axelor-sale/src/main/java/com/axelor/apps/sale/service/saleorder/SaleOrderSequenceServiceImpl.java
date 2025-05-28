package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class SaleOrderSequenceServiceImpl implements SaleOrderSequenceService {

  protected final SequenceService sequenceService;
  protected final SaleOrderRepository saleOrderRepository;
  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderSequenceServiceImpl(
      SequenceService sequenceService,
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService) {
    this.sequenceService = sequenceService;
    this.saleOrderRepository = saleOrderRepository;
    this.appSaleService = appSaleService;
  }

  @Override
  public String getQuotationSequence(SaleOrder saleOrder) throws AxelorException {
    Company company = saleOrder.getCompany();
    String seq;
    if (!appSaleService.getAppSale().getIsQuotationAndOrderSplitEnabled()) {
      seq = getSequence(saleOrder);
    } else {
      SaleOrder originSaleQuotation = saleOrder.getOriginSaleQuotation();
      if (originSaleQuotation != null) {
        long childrenCounter =
            saleOrderRepository
                .all()
                .filter("self.originSaleQuotation = :originSaleQuotation AND self.id != :id")
                .bind("originSaleQuotation", originSaleQuotation)
                .bind("id", saleOrder.getId())
                .count();
        seq = computeChildrenOrderSequence(saleOrder, childrenCounter);
      } else {
        seq = getSequence(saleOrder);
      }
    }

    if (seq == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SaleExceptionMessage.SALES_ORDER_1),
          company.getName());
    }
    return seq;
  }

  protected String computeChildrenOrderSequence(SaleOrder saleOrder, long counter) {
    SaleOrder originSaleQuotation = saleOrder.getOriginSaleQuotation();
    counter++;
    return originSaleQuotation.getSaleOrderSeq() + "_" + counter;
  }

  protected String getSequence(SaleOrder saleOrder) throws AxelorException {
    return sequenceService.getSequenceNumber(
        SequenceRepository.SALES_ORDER,
        saleOrder.getCompany(),
        SaleOrder.class,
        "saleOrderSeq",
        saleOrder);
  }
}
