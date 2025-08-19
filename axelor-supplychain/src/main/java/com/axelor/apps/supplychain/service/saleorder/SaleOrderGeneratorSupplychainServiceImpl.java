package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.PartnerLinkService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderDomainService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderInitValueService;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnChangeService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderGeneratorSupplychainServiceImpl extends SaleOrderGeneratorServiceImpl
    implements SaleOrderGeneratorSupplychainService {

  protected final PartnerLinkService partnerLinkService;

  @Inject
  public SaleOrderGeneratorSupplychainServiceImpl(
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService,
      CompanyService companyService,
      SaleOrderInitValueService saleOrderInitValueService,
      SaleOrderOnChangeService saleOrderOnChangeService,
      SaleOrderDomainService saleOrderDomainService,
      PartnerRepository partnerRepository,
      SaleConfigService saleConfigService,
      PartnerLinkService partnerLinkService) {
    super(
        saleOrderRepository,
        appSaleService,
        companyService,
        saleOrderInitValueService,
        saleOrderOnChangeService,
        saleOrderDomainService,
        partnerRepository,
        saleConfigService);
    this.partnerLinkService = partnerLinkService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public SaleOrder createSaleOrder(
      Partner clientPartner,
      Partner deliveredPartner,
      Company company,
      Partner contactPartner,
      Currency currency,
      Boolean inAti)
      throws AxelorException {
    SaleOrder saleOrder =
        super.createSaleOrder(
            clientPartner, deliveredPartner, company, contactPartner, currency, inAti);
    setDeliveredPartner(deliveredPartner, clientPartner, saleOrder);
    return saleOrder;
  }

  protected void setDeliveredPartner(
      Partner deliveredPartner, Partner clientPartner, SaleOrder saleOrder) {
    if (deliveredPartner != null
        && partnerLinkService.isDeliveredPartnerCompatible(
            deliveredPartner, clientPartner, PartnerLinkTypeRepository.TYPE_SELECT_DELIVERED_TO)) {
      saleOrder.setDeliveredPartner(deliveredPartner);
    } else {
      saleOrder.setDeliveredPartner(clientPartner);
    }
  }

  @Override
  public SaleOrder createSaleOrder(
      Partner clientPartner,
      Partner deliveredPartner,
      Company company,
      Partner contact,
      Currency currency,
      Boolean inAti,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition)
      throws AxelorException {

    SaleOrder saleOrder =
        createSaleOrder(clientPartner, deliveredPartner, company, contact, currency, inAti);
    if (paymentMode != null) {
      saleOrder.setPaymentMode(paymentMode);
    }
    if (paymentCondition != null) {
      saleOrder.setPaymentCondition(paymentCondition);
    }
    return saleOrder;
  }
}
