/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
